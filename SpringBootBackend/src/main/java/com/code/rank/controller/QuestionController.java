package com.code.rank.controller;

import com.code.rank.dto.request.SolutionRequest;
import com.code.rank.dto.response.ApiResponse;
import com.code.rank.dto.response.ErrorResponse;
import com.code.rank.dto.response.QuestionResponse;
import com.code.rank.dto.response.QuestionSummaryResponse;
import com.code.rank.dto.response.SolutionResponse;
import com.code.rank.security.CustomUserDetails;
import com.code.rank.service.QuestionService;
import com.code.rank.service.SolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = """
        Browse published questions and submit solutions.
        Listing/viewing returns sample test cases only; hidden cases stay on the backend.
        """)
public class QuestionController {

    private final QuestionService questionService;
    private final SolutionService solutionService;

    @Operation(
            summary = "List published questions",
            description = "Returns a paged list of question summaries (id, title, difficulty, createdAt)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Page of question summaries"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<QuestionSummaryResponse>>> list(
            @Parameter(description = "Pageable: page (0-based), size, sort", example = "page=0&size=20")
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.list(pageable)));
    }

    @Operation(
            summary = "View a question",
            description = """
                    Returns the question detail with only sample test cases populated.
                    `hiddenTestCaseCount` is exposed so users know how many hidden cases will be
                    graded, but their inputs/outputs are NOT included.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Question detail (samples only)",
                    content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Question not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> get(
            @Parameter(description = "Question id", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(questionService.get(id)));
    }

    @Operation(
            summary = "Submit a solution",
            description = """
                    Runs the supplied code against every test case for the question
                    (samples + hidden, up to 105 cases). Each case runs in its own
                    sandboxed Docker container with the test case's `input` piped as stdin
                    and `stdout` compared against `expectedOutput`.

                    The response contains per-case results for SAMPLE cases only. Hidden cases
                    are aggregated into `hiddenPassedCount` / `hiddenTotalCount`.

                    Per-user rate limit applies (HTTP 429). When the execution queue is
                    saturated, requests are rejected with HTTP 503.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Graded — inspect `status` and `passedCount`",
                    content = @Content(schema = @Schema(implementation = SolutionResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "OK",
                                      "data": {
                                        "solutionId": 7,
                                        "questionId": 1,
                                        "language": "PYTHON",
                                        "status": "ACCEPTED",
                                        "passedCount": 3,
                                        "totalCount": 3,
                                        "totalExecutionTimeMs": 921,
                                        "sampleResults": [
                                          { "testCaseId": 1, "orderIndex": 0, "sample": true, "passed": true,
                                            "input": "1 2\\n", "expectedOutput": "3", "actualOutput": "3\\n",
                                            "stderr": "", "exitCode": 0, "executionTimeMs": 305 }
                                        ],
                                        "hiddenPassedCount": 1,
                                        "hiddenTotalCount": 1,
                                        "createdAt": "2026-05-23T10:35:00Z"
                                      },
                                      "timestamp": "2026-05-23T10:35:00Z"
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed or question has no test cases",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Question not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429", description = "Per-user rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503", description = "Execution queue is full; retry shortly",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/solve")
    public ResponseEntity<ApiResponse<SolutionResponse>> solve(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "Question id", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody SolutionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(solutionService.solve(user.getId(), id, request)));
    }
}
