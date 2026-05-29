package com.code.rank.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiResponse",
        description = "Envelope for every successful response. The `data` field carries the endpoint-specific payload.")
public class ApiResponse<T> {

    @Schema(description = "Always true for success envelopes", example = "true")
    private boolean success;

    @Schema(description = "Human-readable result message", example = "OK")
    private String message;

    @Schema(description = "Endpoint-specific payload")
    private T data;

    @Schema(description = "Server-side response timestamp (UTC)", example = "2026-05-23T10:15:30Z")
    private Instant timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, Instant.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }
}
