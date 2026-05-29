package com.code.rank.dto.response;

import com.code.rank.entity.ExecutionStatus;
import com.code.rank.entity.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "ExecuteResponse", description = "Result of a single /api/execute run")
public class ExecuteResponse {

    @Schema(description = "Persisted Submission id (lookup via /api/submissions/{id})", example = "42")
    private Long submissionId;

    @Schema(description = "Language that was executed", example = "PYTHON")
    private Language language;

    @Schema(description = "Execution outcome", example = "SUCCESS")
    private ExecutionStatus status;

    @Schema(description = "Captured standard output (truncated at 256 KB)", example = "45\n")
    private String stdout;

    @Schema(description = "Captured standard error", example = "")
    private String stderr;

    @Schema(description = "Process exit code from the sandbox", example = "0")
    private int exitCode;

    @Schema(description = "Wall-clock execution time in milliseconds", example = "312")
    private long executionTimeMs;
}
