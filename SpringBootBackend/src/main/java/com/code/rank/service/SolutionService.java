package com.code.rank.service;

import com.code.rank.dto.request.SolutionRequest;
import com.code.rank.dto.response.SolutionResponse;
import com.code.rank.dto.response.TestCaseResultResponse;
import com.code.rank.entity.*;
import com.code.rank.exception.BadRequestException;
import com.code.rank.exception.ExecutionFailedException;
import com.code.rank.exception.ResourceNotFoundException;
import com.code.rank.executor.DockerExecutor;
import com.code.rank.executor.ExecutionResult;
import com.code.rank.repository.QuestionRepository;
import com.code.rank.repository.SolutionRepository;
import com.code.rank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolutionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final SolutionRepository solutionRepository;
    private final DockerExecutor dockerExecutor;
    private final RateLimiterService rateLimiter;

    @Qualifier("executionExecutor")
    private final ThreadPoolExecutor executor;

    @Transactional(readOnly = true)
    public Page<SolutionResponse> list(Long userId, Long questionId, Pageable pageable) {
        Page<Solution> page = (questionId == null)
                ? solutionRepository.findByUserId(userId, pageable)
                : solutionRepository.findByUserIdAndQuestionId(userId, questionId, pageable);
        return page.map(s -> SolutionResponse.from(s, List.of(), 0, 0));
    }

    @Transactional
    public SolutionResponse solve(Long userId, Long questionId, SolutionRequest req) {
        rateLimiter.check(userId);

        Question question = questionRepository.findByIdWithTestCases(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        if (question.getTestCases().isEmpty()) {
            throw new BadRequestException("This question has no test cases yet");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<TestCase> orderedCases = new ArrayList<>(question.getTestCases());
        orderedCases.sort(Comparator.comparingInt(TestCase::getOrderIndex));

        GradingOutcome outcome = runAllTestCases(req.getLanguage(), req.getCode(), orderedCases);

        Solution solution = solutionRepository.save(Solution.builder()
                .user(user)
                .question(question)
                .language(req.getLanguage())
                .code(req.getCode())
                .passedCount(outcome.totalPassed)
                .totalCount(orderedCases.size())
                .status(outcome.status)
                .totalExecutionTimeMs(outcome.totalTimeMs)
                .build());

        return SolutionResponse.from(solution, outcome.sampleResults, outcome.hiddenPassed, outcome.hiddenTotal);
    }

    private GradingOutcome runAllTestCases(Language language, String code, List<TestCase> cases) {
        Future<GradingOutcome> future = executor.submit(() -> grade(language, code, cases));
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ExecutionFailedException("Grading interrupted", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            log.error("Grading failed", cause);
            throw new ExecutionFailedException(cause.getMessage(), cause);
        }
    }

    private GradingOutcome grade(Language language, String code, List<TestCase> cases) {
        List<TestCaseResultResponse> sampleResults = new ArrayList<>();
        int totalPassed = 0;
        int hiddenPassed = 0;
        int hiddenTotal = 0;
        long totalTime = 0;
        SolutionStatus aggregate = SolutionStatus.ACCEPTED;
        boolean anyCompileError = false;
        boolean anyTimeout = false;
        boolean anyRuntime = false;
        boolean anyInternal = false;

        List<String> inputs = new ArrayList<>(cases.size());
        for (TestCase tc : cases) {
            inputs.add(tc.getInput() == null ? "" : tc.getInput());
        }
        List<ExecutionResult> results;
        try {
            results = dockerExecutor.executeBatch(language, code, inputs);
        } catch (Exception ex) {
            log.warn("Sandbox batch error: {}", ex.getMessage());
            ExecutionResult err = ExecutionResult.builder()
                    .stdout("").stderr(ex.getMessage())
                    .exitCode(-1).executionTimeMs(0)
                    .status(ExecutionStatus.INTERNAL_ERROR).build();
            results = new ArrayList<>(cases.size());
            for (int i = 0; i < cases.size(); i++) results.add(err);
        }

        for (int i = 0; i < cases.size(); i++) {
            TestCase tc = cases.get(i);
            ExecutionResult res = results.get(i);
            totalTime += res.getExecutionTimeMs();

            boolean passed = res.getStatus() == ExecutionStatus.SUCCESS
                    && outputsMatch(res.getStdout(), tc.getExpectedOutput());
            if (passed) totalPassed++;
            if (!tc.isSample()) {
                hiddenTotal++;
                if (passed) hiddenPassed++;
            }
            if (res.getStatus() == ExecutionStatus.COMPILE_ERROR) anyCompileError = true;
            else if (res.getStatus() == ExecutionStatus.TIMEOUT) anyTimeout = true;
            else if (res.getStatus() == ExecutionStatus.RUNTIME_ERROR) anyRuntime = true;
            else if (res.getStatus() == ExecutionStatus.INTERNAL_ERROR) anyInternal = true;

            if (tc.isSample()) {
                sampleResults.add(TestCaseResultResponse.builder()
                        .testCaseId(tc.getId())
                        .orderIndex(tc.getOrderIndex())
                        .sample(true)
                        .passed(passed)
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .actualOutput(res.getStdout())
                        .stderr(res.getStderr())
                        .exitCode(res.getExitCode())
                        .executionTimeMs(res.getExecutionTimeMs())
                        .build());
            }

            if (anyCompileError) break;
        }

        if (anyCompileError)        aggregate = SolutionStatus.COMPILE_ERROR;
        else if (totalPassed == cases.size()) aggregate = SolutionStatus.ACCEPTED;
        else if (totalPassed == 0 && anyTimeout)   aggregate = SolutionStatus.TIMEOUT;
        else if (totalPassed == 0 && anyRuntime)   aggregate = SolutionStatus.RUNTIME_ERROR;
        else if (totalPassed == 0 && anyInternal)  aggregate = SolutionStatus.INTERNAL_ERROR;
        else if (totalPassed == 0)                 aggregate = SolutionStatus.WRONG_ANSWER;
        else                                       aggregate = SolutionStatus.PARTIAL;

        GradingOutcome out = new GradingOutcome();
        out.sampleResults = sampleResults;
        out.totalPassed = totalPassed;
        out.hiddenPassed = hiddenPassed;
        out.hiddenTotal = hiddenTotal;
        out.totalTimeMs = totalTime;
        out.status = aggregate;
        return out;
    }

    private boolean outputsMatch(String actual, String expected) {
        if (actual == null) actual = "";
        if (expected == null) expected = "";
        return normalize(actual).equals(normalize(expected));
    }

    private String normalize(String s) {
        return s.replace("\r\n", "\n").stripTrailing();
    }

    private static class GradingOutcome {
        List<TestCaseResultResponse> sampleResults;
        int totalPassed;
        int hiddenPassed;
        int hiddenTotal;
        long totalTimeMs;
        SolutionStatus status;
    }
}
