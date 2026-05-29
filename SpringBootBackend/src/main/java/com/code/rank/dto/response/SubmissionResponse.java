package com.code.rank.dto.response;

import com.code.rank.entity.ExecutionStatus;
import com.code.rank.entity.Language;
import com.code.rank.entity.Submission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "SubmissionResponse", description = "Persisted record of one /api/execute run")
public class SubmissionResponse {

    @Schema(description = "Submission id", example = "42")
    private Long id;

    @Schema(description = "Language", example = "PYTHON")
    private Language language;

    @Schema(description = "Source code that was run")
    private String code;

    @Schema(description = "Stdin supplied at submission time")
    private String stdin;

    @Schema(description = "Captured standard output")
    private String stdout;

    @Schema(description = "Captured standard error")
    private String stderr;

    @Schema(description = "Process exit code", example = "0")
    private int exitCode;

    @Schema(description = "Outcome of the run", example = "SUCCESS")
    private ExecutionStatus status;

    @Schema(description = "Wall-clock execution time in ms", example = "312")
    private long executionTimeMs;

    @Schema(description = "When the run was recorded (UTC)", example = "2026-05-23T10:15:30Z")
    private Instant createdAt;

    public static SubmissionResponse from(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .language(s.getLanguage())
                .code(s.getCode())
                .stdin(s.getStdin())
                .stdout(s.getStdout())
                .stderr(s.getStderr())
                .exitCode(s.getExitCode())
                .status(s.getStatus())
                .executionTimeMs(s.getExecutionTimeMs())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
