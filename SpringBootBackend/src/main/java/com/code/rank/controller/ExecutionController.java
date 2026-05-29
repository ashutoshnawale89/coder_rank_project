package com.code.rank.controller;

import com.code.rank.dto.request.ExecuteRequest;
import com.code.rank.dto.response.ApiResponse;
import com.code.rank.dto.response.ErrorResponse;
import com.code.rank.dto.response.ExecuteResponse;
import com.code.rank.security.CustomUserDetails;
import com.code.rank.service.ExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/execute")
@RequiredArgsConstructor
@Tag(name = "Execution", description = "Submit and run arbitrary code in a sandboxed Docker container.")
public class ExecutionController {

    private final ExecutionService executionService;

    @Operation(
            summary = "Execute a single code snippet",
            description = """
                    Runs the given code inside an isolated Docker container with no network,
                    capped memory and CPU, dropped capabilities, and a non-root user. The
                    container is destroyed after each run.

                    Per-user rate limit applies (HTTP 429). When the execution queue is
                    saturated, requests are rejected with HTTP 503.

                    A persistent `Submission` record is created so the result is retrievable
                    via `/api/submissions/{id}`.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Code ran; check `status` for the outcome",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "OK",
                              "data": {
                                "submissionId": 42,
                                "language": "PYTHON",
                                "status": "SUCCESS",
                                "stdout": "45\\n",
                                "stderr": "",
                                "exitCode": 0,
                                "executionTimeMs": 312
                              },
                              "timestamp": "2026-05-23T10:16:00Z"
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429", description = "Per-user rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503", description = "Execution queue is full; retry shortly",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500", description = "Docker engine error or unexpected failure",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ExecuteResponse>> execute(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ExecuteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(executionService.execute(principal.getId(), request)));
    }
}
