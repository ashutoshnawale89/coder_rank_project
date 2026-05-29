package com.code.rank.controller;

import com.code.rank.dto.request.SnippetRequest;
import com.code.rank.dto.response.ApiResponse;
import com.code.rank.dto.response.ErrorResponse;
import com.code.rank.dto.response.SnippetResponse;
import com.code.rank.security.CustomUserDetails;
import com.code.rank.service.SnippetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/snippets")
@RequiredArgsConstructor
@Tag(name = "Snippets", description = "Create, list, view, and delete the caller's personal code snippets.")
public class SnippetController {

    private final SnippetService snippetService;

    @Operation(
            summary = "Create a new snippet",
            description = "Saves a code snippet owned by the caller. Other users cannot see or modify it."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Snippet created",
                    content = @Content(schema = @Schema(implementation = SnippetResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<SnippetResponse>> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody SnippetRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Snippet created",
                snippetService.create(principal.getId(), request)));
    }

    @Operation(
            summary = "List the caller's snippets",
            description = "Returns a paged list of snippets owned by the authenticated user. " +
                    "Use the standard Pageable query params: `page`, `size`, `sort`."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Page of snippets"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SnippetResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "Pageable: page (0-based), size, sort", example = "page=0&size=20&sort=createdAt,desc")
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(snippetService.list(principal.getId(), pageable)));
    }

    @Operation(
            summary = "Get a snippet by id",
            description = "Returns the snippet only if it is owned by the caller; otherwise 403 or 404."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Snippet found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Snippet belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Snippet not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SnippetResponse>> get(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "Snippet id", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(snippetService.get(principal.getId(), id)));
    }

    @Operation(
            summary = "Delete a snippet",
            description = "Permanently deletes a snippet owned by the caller."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Snippet deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Snippet belongs to another user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Snippet not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "Snippet id", example = "1", required = true)
            @PathVariable Long id) {
        snippetService.delete(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Snippet deleted", null));
    }
}
