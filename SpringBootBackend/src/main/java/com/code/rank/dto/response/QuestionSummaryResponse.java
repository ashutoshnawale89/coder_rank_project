package com.code.rank.dto.response;

import com.code.rank.entity.Difficulty;
import com.code.rank.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "QuestionSummaryResponse", description = "Lightweight question entry used for list views (no description, no test cases)")
public class QuestionSummaryResponse {

    @Schema(description = "Question id", example = "1")
    private Long id;

    @Schema(description = "Title", example = "Sum two numbers")
    private String title;

    @Schema(description = "Difficulty rating", example = "EASY")
    private Difficulty difficulty;

    @Schema(description = "Created timestamp (UTC)", example = "2026-05-23T10:15:30Z")
    private Instant createdAt;

    public static QuestionSummaryResponse from(Question q) {
        return QuestionSummaryResponse.builder()
                .id(q.getId())
                .title(q.getTitle())
                .difficulty(q.getDifficulty())
                .createdAt(q.getCreatedAt())
                .build();
    }
}
