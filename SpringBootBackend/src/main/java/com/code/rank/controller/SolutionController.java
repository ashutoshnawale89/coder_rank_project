package com.code.rank.controller;

import com.code.rank.dto.response.ApiResponse;
import com.code.rank.dto.response.ErrorResponse;
import com.code.rank.dto.response.SolutionResponse;
import com.code.rank.security.CustomUserDetails;
import com.code.rank.service.SolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solutions")
@RequiredArgsConstructor
@Tag(name = "Solutions", description = "Read the caller's past solution attempts (grading history).")
public class SolutionController {

    private final SolutionService solutionService;

    @Operation(
            summary = "List the caller's solutions",
            description = """
                    Returns a paged list of grading attempts for the authenticated user.
                    Filter by question via the optional `questionId` query parameter.
                    Per-case details are NOT included here — fetch individual entries (or
                    the original `/solve` response) for the full breakdown.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Page of solution summaries"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SolutionResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "Optional filter: only solutions for this question id", example = "1")
            @RequestParam(required = false) Long questionId,
            @Parameter(description = "Pageable: page (0-based), size, sort", example = "page=0&size=20&sort=createdAt,desc")
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                solutionService.list(user.getId(), questionId, pageable)));
    }
}
