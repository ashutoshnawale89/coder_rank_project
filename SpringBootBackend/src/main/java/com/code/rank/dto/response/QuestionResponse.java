package com.code.rank.dto.response;

import com.code.rank.entity.Difficulty;
import com.code.rank.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "QuestionResponse", description = """
        Full question detail. When returned to a regular user, `sampleTestCases` contains
        only the sample test cases. When returned to an admin, it contains every test case.
        """)
public class QuestionResponse {

    @Schema(description = "Question id", example = "1")
    private Long id;

    @Schema(description = "Title", example = "Sum two numbers")
    private String title;

    @Schema(description = "Problem statement", example = "Read two integers from stdin and print their sum.")
    private String description;

    @Schema(description = "Difficulty rating", example = "EASY")
    private Difficulty difficulty;

    @Schema(description = "Number of sample (visible) test cases", example = "2")
    private int sampleTestCaseCount;

    @Schema(description = "Number of hidden test cases (run during grading, not exposed to users)", example = "100")
    private int hiddenTestCaseCount;

    @Schema(description = "Sample test cases (full list for admins, samples only for users)")
    private List<TestCaseResponse> sampleTestCases;

    @Schema(description = "Created timestamp (UTC)", example = "2026-05-23T10:15:30Z")
    private Instant createdAt;

    @Schema(description = "Last updated timestamp (UTC)", example = "2026-05-23T10:15:30Z")
    private Instant updatedAt;

    public static QuestionResponse forUser(Question q) {
        List<TestCaseResponse> samples = q.getTestCases().stream()
                .filter(t -> t.isSample())
                .map(TestCaseResponse::sampleOnly)
                .toList();
        long hidden = q.getTestCases().stream().filter(t -> !t.isSample()).count();
        return QuestionResponse.builder()
                .id(q.getId())
                .title(q.getTitle())
                .description(q.getDescription())
                .difficulty(q.getDifficulty())
                .sampleTestCaseCount(samples.size())
                .hiddenTestCaseCount((int) hidden)
                .sampleTestCases(samples)
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }

    public static QuestionResponse forAdmin(Question q) {
        List<TestCaseResponse> all = q.getTestCases().stream()
                .map(TestCaseResponse::from).toList();
        long samples = q.getTestCases().stream().filter(t -> t.isSample()).count();
        long hidden = q.getTestCases().size() - samples;
        return QuestionResponse.builder()
                .id(q.getId())
                .title(q.getTitle())
                .description(q.getDescription())
                .difficulty(q.getDifficulty())
                .sampleTestCaseCount((int) samples)
                .hiddenTestCaseCount((int) hidden)
                .sampleTestCases(all)
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }
}
