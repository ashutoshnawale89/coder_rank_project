package com.code.rank.service;

import com.code.rank.dto.request.ExecuteRequest;
import com.code.rank.dto.response.ExecuteResponse;
import com.code.rank.entity.Submission;
import com.code.rank.entity.User;
import com.code.rank.exception.ExecutionFailedException;
import com.code.rank.exception.ResourceNotFoundException;
import com.code.rank.executor.DockerExecutor;
import com.code.rank.executor.ExecutionResult;
import com.code.rank.repository.SubmissionRepository;
import com.code.rank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionService {

    private final DockerExecutor dockerExecutor;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final RateLimiterService rateLimiter;

    @Qualifier("executionExecutor")
    private final ThreadPoolExecutor executor;

    public ExecuteResponse execute(Long userId, ExecuteRequest req) {
        rateLimiter.check(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Future<ExecutionResult> future = executor.submit(() ->
                dockerExecutor.execute(req.getLanguage(), req.getCode(), req.getStdin()));

        ExecutionResult result;
        try {
            result = future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ExecutionFailedException("Execution interrupted", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            log.error("Execution failed", cause);
            throw new ExecutionFailedException(cause.getMessage(), cause);
        }

        Submission saved = submissionRepository.save(Submission.builder()
                .user(user)
                .language(req.getLanguage())
                .code(req.getCode())
                .stdin(req.getStdin())
                .stdout(result.getStdout())
                .stderr(result.getStderr())
                .exitCode(result.getExitCode())
                .status(result.getStatus())
                .executionTimeMs(result.getExecutionTimeMs())
                .build());

        return ExecuteResponse.builder()
                .submissionId(saved.getId())
                .language(saved.getLanguage())
                .status(saved.getStatus())
                .stdout(saved.getStdout())
                .stderr(saved.getStderr())
                .exitCode(saved.getExitCode())
                .executionTimeMs(saved.getExecutionTimeMs())
                .build();
    }
}
