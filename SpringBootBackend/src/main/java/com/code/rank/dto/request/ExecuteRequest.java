package com.code.rank.dto.request;

import com.code.rank.entity.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "ExecuteRequest", description = "Code submission for one-off execution in a sandbox container")
public class ExecuteRequest {

    @Schema(description = "Programming language", example = "PYTHON", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Language language;

    @Schema(description = "Source code to run (max 100,000 chars)",
            example = "print(sum(range(10)))",
            requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100_000)
    @NotBlank
    @Size(max = 100_000)
    private String code;

    @Schema(description = "Optional stdin piped into the program (max 100,000 chars)", example = "")
    @Size(max = 100_000)
    private String stdin;

    @Schema(description = "Optional snippet id this execution belongs to", example = "1", nullable = true)
    private Long snippetId;
}
