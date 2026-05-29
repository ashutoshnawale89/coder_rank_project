package com.code.rank.controller;

import com.code.rank.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Public liveness check. No authentication required.")
@SecurityRequirements
public class HealthController {

    @Operation(
            summary = "Service liveness",
            description = "Returns `UP` when the API process is reachable. Used by Docker / Kubernetes / uptime monitors."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Service is up",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "OK",
                              "data": { "status": "UP" },
                              "timestamp": "2026-05-23T10:15:30Z"
                            }
                            """)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("status", "UP")));
    }
}
