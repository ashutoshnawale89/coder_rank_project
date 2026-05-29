package com.code.rank.dto.request;

import com.code.rank.entity.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "SolutionRequest", description = "A user's code submission for grading against a question's test cases")
public class SolutionRequest {

    @Schema(description = "Programming language", example = "PYTHON", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Language language;

    @Schema(description = "Source code (max 100,000 chars). Will be run against every test case.",
            example = "a,b=map(int,input().split()); print(a+b)",
            requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100_000)
    @NotBlank
    @Size(max = 100_000)
    private String code;
}
