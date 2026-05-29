package com.code.rank.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "AuthResponse", description = "Returned by /api/auth/register and /api/auth/login")
public class AuthResponse {

    @Schema(description = "Signed JWT — use as `Authorization: Bearer <token>`",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.signature")
    private String token;

    @Schema(description = "Always `Bearer`", example = "Bearer")
    private String tokenType;

    @Schema(description = "Token lifetime in milliseconds", example = "86400000")
    private long expiresInMs;

    @Schema(description = "Authenticated user id", example = "1")
    private Long userId;

    @Schema(description = "Authenticated username", example = "alice")
    private String username;
}
