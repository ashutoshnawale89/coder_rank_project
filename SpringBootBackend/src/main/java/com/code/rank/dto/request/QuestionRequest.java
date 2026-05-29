package com.code.rank.dto.request;

import com.code.rank.entity.Difficulty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "QuestionRequest", description = """
        Payload to create or replace a coding question. Test cases are passed inline:
        up to 5 may be marked `sample=true` (visible to users) and up to 100 may be
        `sample=false` (hidden, run on the backend during grading).
        """)
public class QuestionRequest {

    @Schema(description = "Question title", example = "Sum two numbers",
            maxLength = 200, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 200)
    private String title;

    @Schema(description = "Markdown-friendly problem statement",
            example = "Read two integers from stdin and print their sum on a single line.",
            maxLength = 50_000, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 50_000)
    private String description;

    @Schema(description = "Difficulty rating", example = "EASY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Difficulty difficulty;

    @Schema(description = "Test cases. At most 5 with sample=true, at most 100 with sample=false.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Valid
    @Size(max = 105, message = "A question supports at most 5 sample + 100 hidden test cases")
    private List<TestCaseRequest> testCases;
}
