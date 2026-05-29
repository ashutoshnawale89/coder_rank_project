package com.code.rank.dto.request;

import com.code.rank.entity.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "SnippetRequest", description = "Payload to create or replace a personal code snippet")
public class SnippetRequest {

    @Schema(description = "Title shown in the user's snippet list", example = "FizzBuzz", maxLength = 200,
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 200)
    private String title;

    @Schema(description = "Programming language", example = "PYTHON", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Language language;

    @Schema(description = "Source code (max 100,000 chars)", maxLength = 100_000,
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "for i in range(1, 16): print('FizzBuzz' if i%15==0 else 'Fizz' if i%3==0 else 'Buzz' if i%5==0 else i)")
    @NotBlank
    @Size(max = 100_000)
    private String code;
}
