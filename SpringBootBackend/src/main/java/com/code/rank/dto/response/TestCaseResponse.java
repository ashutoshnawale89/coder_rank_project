package com.code.rank.dto.response;

import com.code.rank.entity.TestCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "TestCaseResponse",
        description = "A single test case. Returned with full input/output only when caller has access (admin view, or sample case).")
public class TestCaseResponse {

    @Schema(description = "Test case id", example = "1")
    private Long id;

    @Schema(description = "Stdin piped to the user's program", example = "1 2\n")
    private String input;

    @Schema(description = "Expected stdout (normalized comparison)", example = "3")
    private String expectedOutput;

    @Schema(description = "true = visible to users; false = hidden (admin view only)", example = "true")
    private boolean sample;

    @Schema(description = "Ordering within the question (0-based)", example = "0")
    private int orderIndex;

    public static TestCaseResponse from(TestCase tc) {
        return TestCaseResponse.builder()
                .id(tc.getId())
                .input(tc.getInput())
                .expectedOutput(tc.getExpectedOutput())
                .sample(tc.isSample())
                .orderIndex(tc.getOrderIndex())
                .build();
    }

    public static TestCaseResponse sampleOnly(TestCase tc) {
        return TestCaseResponse.builder()
                .id(tc.getId())
                .input(tc.getInput())
                .expectedOutput(tc.getExpectedOutput())
                .sample(true)
                .orderIndex(tc.getOrderIndex())
                .build();
    }
}
