package com.code.rank.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Uniform error envelope returned by GlobalExceptionHandler")
public class ErrorResponse {

    @Schema(description = "Always false for error envelopes", example = "false")
    private boolean success;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Short error label", example = "Bad Request")
    private String error;

    @Schema(description = "Human-readable explanation", example = "Username already taken")
    private String message;

    @Schema(description = "Per-field validation messages (omitted for non-validation errors)",
            example = "[\"username: must not be blank\"]")
    private List<String> details;

    @Schema(description = "Request path that produced the error", example = "/api/auth/register")
    private String path;

    @Schema(description = "Server-side error timestamp (UTC)", example = "2026-05-23T10:15:30Z")
    private Instant timestamp;

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(false, status, error, message, null, path, Instant.now());
    }

    public static ErrorResponse of(int status, String error, String message, List<String> details, String path) {
        return new ErrorResponse(false, status, error, message, details, path, Instant.now());
    }
}
