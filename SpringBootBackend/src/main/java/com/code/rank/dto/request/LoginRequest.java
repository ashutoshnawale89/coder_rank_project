package com.code.rank.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "LoginRequest", description = "Credentials for username + password login")
public class LoginRequest {

    @Schema(description = "Registered username", example = "alice")
    @NotBlank
    private String username;

    @Schema(description = "Account password", example = "secret123")
    @NotBlank
    private String password;
}
