package com.code.rank.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "TestCaseResultResponse",
        description = "Per-test-case outcome of a graded solution. Only returned for sample cases.")
public class TestCaseResultResponse {

    @Schema(description = "Test case id", example = "1")
    private Long testCaseId;

    @Schema(description = "Ordering within the question (0-based)", example = "0")
    private int orderIndex;

    @Schema(description = "Always true here — hidden test case results are not included individually", example = "true")
    private boolean sample;

    @Schema(description = "true if actual stdout matched expectedOutput (normalized)", example = "true")
    private boolean passed;

    @Schema(description = "Stdin used for this case", example = "1 2\n")
    private String input;

    @Schema(description = "Expected stdout", example = "3")
    private String expectedOutput;

    @Schema(description = "Stdout the user's code actually produced", example = "3\n")
    private String actualOutput;

    @Schema(description = "Stderr captured (compile/runtime errors land here)", example = "")
    private String stderr;

    @Schema(description = "Exit code from the sandbox", example = "0")
    private int exitCode;

    @Schema(description = "Wall-clock execution time for this case in ms", example = "305")
    private long executionTimeMs;
}
