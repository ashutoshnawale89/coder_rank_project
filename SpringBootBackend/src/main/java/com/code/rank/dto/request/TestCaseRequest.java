package com.code.rank.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "TestCaseRequest", description = "Single test case associated with a question")
public class TestCaseRequest {

    @Schema(description = "Input piped to the program's stdin (may be empty)", example = "1 2\n", maxLength = 50_000)
    @Size(max = 50_000)
    private String input;

    @Schema(description = "Expected stdout (normalized: trailing whitespace and CRLF are ignored)",
            example = "3", maxLength = 50_000, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50_000)
    private String expectedOutput;

    @Schema(description = "true = visible to the user (sample); false = hidden, only used for grading",
            example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Boolean sample;
}
