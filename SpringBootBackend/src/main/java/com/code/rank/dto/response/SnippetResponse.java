package com.code.rank.dto.response;

import com.code.rank.entity.Language;
import com.code.rank.entity.Snippet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "SnippetResponse", description = "A saved code snippet owned by the caller")
public class SnippetResponse {

    @Schema(description = "Snippet id", example = "1")
    private Long id;

    @Schema(description = "User-supplied title", example = "FizzBuzz")
    private String title;

    @Schema(description = "Language", example = "PYTHON")
    private Language language;

    @Schema(description = "Saved source code")
    private String code;

    @Schema(description = "Creation timestamp (UTC)", example = "2026-05-23T10:15:30Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp (UTC)", example = "2026-05-23T10:15:30Z")
    private Instant updatedAt;

    public static SnippetResponse from(Snippet s) {
        return SnippetResponse.builder()
                .id(s.getId())
                .title(s.getTitle())
                .language(s.getLanguage())
                .code(s.getCode())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
