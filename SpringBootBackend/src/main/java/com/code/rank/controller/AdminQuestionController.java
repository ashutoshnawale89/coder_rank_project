package com.code.rank.controller;

import com.code.rank.dto.request.QuestionRequest;
import com.code.rank.dto.response.ApiResponse;
import com.code.rank.dto.response.ErrorResponse;
import com.code.rank.dto.response.QuestionResponse;
import com.code.rank.security.CustomUserDetails;
import com.code.rank.service.AdminQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/questions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = """
                Admin-only endpoints. Caller must have ROLE_ADMIN.
                Use these to author the coding questions that users solve.
                Each question can have up to 5 sample test cases (visible to users)
                and up to 100 hidden test cases (run on the backend during grading).
                """)
public class AdminQuestionController {

        private final AdminQuestionService adminQuestionService;

        @Operation(summary = "Create a question", description = """
                        Creates a new question with its full set of test cases.

                        Constraints:
                        - At least one test case is required.
                        - At most 5 test cases may have `sample = true`.
                        - At most 100 test cases may have `sample = false` (hidden).
                        - Each test case must include `expectedOutput`.
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Question created", content = @Content(schema = @Schema(implementation = QuestionResponse.class), examples = @ExampleObject(value = """
                                                {
                                                  "success": true,
                                                  "message": "Question created",
                                                  "data": [{
                                                    "id": 1,
                                                    "title": "Sum two numbers",
                                                    "description": "Read two integers from stdin and print their sum.",
                                                    "difficulty": "EASY",
                                                    "sampleTestCaseCount": 2,
                                                    "hiddenTestCaseCount": 1,
                                                    "sampleTestCases": [
                                                      { "id": 1, "input": "1 2\\n", "expectedOutput": "3", "sample": true, "orderIndex": 0 },
                                                      { "id": 2, "input": "10 20\\n", "expectedOutput": "30", "sample": true, "orderIndex": 1 },
                                                      { "id": 3, "input": "100 200\\n", "expectedOutput": "300", "sample": false, "orderIndex": 2 }
                                                    ],
                                                    "createdAt": "2026-05-23T10:30:00Z",
                                                    "updatedAt": "2026-05-23T10:30:00Z"
                                        }],
                                                  "timestamp": "2026-05-23T10:30:00Z"
                                                }
                                                """))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or test case limits exceeded", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Caller is not an admin", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PostMapping
        public ResponseEntity<ApiResponse<List<QuestionResponse>>> create(
                        @AuthenticationPrincipal CustomUserDetails admin,
                        @Valid @RequestBody List<QuestionRequest> request) {
                return ResponseEntity.ok(ApiResponse.ok("Question created",
                                adminQuestionService.create(admin.getId(), request)));
        }

        @Operation(summary = "Get a question (admin view)", description = "Returns the full question including every test case (sample and hidden).")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Question found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Question not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Caller is not an admin", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<QuestionResponse>> get(
                        @Parameter(description = "Question id", example = "1", required = true) @PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.ok(adminQuestionService.get(id)));
        }

        @Operation(summary = "Replace a question", description = "Updates the title/description/difficulty and replaces ALL test cases with the supplied list.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Question updated"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or test case limits exceeded", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Question not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Caller is not an admin", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<QuestionResponse>> update(
                        @Parameter(description = "Question id", example = "1", required = true) @PathVariable Long id,
                        @Valid @RequestBody QuestionRequest request) {
                return ResponseEntity.ok(ApiResponse.ok("Question updated",
                                adminQuestionService.update(id, request)));
        }

        @Operation(summary = "Delete a question", description = "Permanently deletes the question and all its test cases.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Question deleted"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Question not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Caller is not an admin", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @Parameter(description = "Question id", example = "1", required = true) @PathVariable Long id) {
                adminQuestionService.delete(id);
                return ResponseEntity.ok(ApiResponse.ok("Question deleted", null));
        }
}
