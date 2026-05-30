package com.code.rank.service;

import com.code.rank.dto.request.ExecuteRequest;
import com.code.rank.dto.response.ExecuteResponse;
import com.code.rank.entity.Submission;
import com.code.rank.entity.User;
import com.code.rank.exception.ExecutionFailedException;
import com.code.rank.exception.ResourceNotFoundException;
import com.code.rank.entity.ExecutionStatus;
import com.code.rank.executor.DockerExecutor;
import com.code.rank.executor.ExecutionResult;
import com.code.rank.repository.SubmissionRepository;
import com.code.rank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    @Value("${app.docker.timeout-seconds}")
    private long dockerTimeoutSeconds;

    // Extra head-room over the per-program docker timeout to absorb container
    // start-up and compilation before we declare a hard failure.
    private static final long OVERHEAD_SECONDS = 20;

    public ExecuteResponse execute(Long userId, ExecuteRequest req) {
        rateLimiter.check(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Future<ExecutionResult> future = executor.submit(() ->
                dockerExecutor.execute(req.getLanguage(), req.getCode(), req.getStdin()));

        // Hard wall-clock cap so a submission whose execution exceeds the limit
        // (e.g. while(true)) always returns a TIMEOUT/failed verdict instead of
        // hanging the request, even if the docker layer itself stalls.
        long hardCapSeconds = dockerTimeoutSeconds + OVERHEAD_SECONDS;

        ExecutionResult result;
        try {
            result = future.get(hardCapSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            log.warn("Execution exceeded hard cap of {}s for user {}", hardCapSeconds, userId);
            result = ExecutionResult.builder()
                    .stdout("")
                    .stderr("Execution timed out after " + dockerTimeoutSeconds + "s")
                    .exitCode(124)
                    .executionTimeMs(hardCapSeconds * 1000)
                    .status(ExecutionStatus.TIMEOUT)
                    .build();
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
