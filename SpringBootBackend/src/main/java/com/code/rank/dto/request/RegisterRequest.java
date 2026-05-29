package com.code.rank.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "RegisterRequest", description = "Payload to register a new user account")
public class RegisterRequest {

    @Schema(description = "Unique username (3-64 chars)", example = "alice", minLength = 3, maxLength = 64)
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @Schema(description = "Unique email address", example = "alice@example.com", maxLength = 128)
    @NotBlank
    @Email
    @Size(max = 128)
    private String email;

    @Schema(description = "Plain-text password (BCrypt-hashed before storage). 8-128 chars.",
            example = "secret123", minLength = 8, maxLength = 128)
    @NotBlank
    @Size(min = 8, max = 128)
    private String password;
}
