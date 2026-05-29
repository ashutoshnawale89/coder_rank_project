package com.code.rank.dto.response;

import com.code.rank.entity.Language;
import com.code.rank.entity.Solution;
import com.code.rank.entity.SolutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "SolutionResponse", description = """
        Result of grading a user's solution against a question's test cases.
        Per-case details are included for SAMPLE cases only; hidden cases are aggregated
        into `hiddenPassedCount` / `hiddenTotalCount`.
        """)
public class SolutionResponse {

    @Schema(description = "Persisted Solution id", example = "7")
    private Long solutionId;

    @Schema(description = "The question that was solved", example = "1")
    private Long questionId;

    @Schema(description = "Language of the submitted code", example = "PYTHON")
    private Language language;

    @Schema(description = "Aggregate verdict", example = "ACCEPTED")
    private SolutionStatus status;

    @Schema(description = "Number of test cases (sample + hidden) that passed", example = "3")
    private int passedCount;

    @Schema(description = "Total test cases that ran", example = "3")
    private int totalCount;

    @Schema(description = "Sum of wall-clock execution time across all cases (ms)", example = "921")
    private long totalExecutionTimeMs;

    @Schema(description = "Per-case results for SAMPLE cases (empty for list endpoints)")
    private List<TestCaseResultResponse> sampleResults;

    @Schema(description = "How many hidden test cases passed", example = "1")
    private int hiddenPassedCount;

    @Schema(description = "How many hidden test cases ran", example = "1")
    private int hiddenTotalCount;

    @Schema(description = "When the solution was graded (UTC)", example = "2026-05-23T10:35:00Z")
    private Instant createdAt;

    public static SolutionResponse from(Solution s, List<TestCaseResultResponse> sampleResults,
                                        int hiddenPassed, int hiddenTotal) {
        return SolutionResponse.builder()
                .solutionId(s.getId())
                .questionId(s.getQuestion().getId())
                .language(s.getLanguage())
                .status(s.getStatus())
                .passedCount(s.getPassedCount())
                .totalCount(s.getTotalCount())
                .totalExecutionTimeMs(s.getTotalExecutionTimeMs())
                .sampleResults(sampleResults)
                .hiddenPassedCount(hiddenPassed)
                .hiddenTotalCount(hiddenTotal)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
