package com.code.rank.controller;

import com.code.rank.dto.request.LoginRequest;
import com.code.rank.dto.request.RegisterRequest;
import com.code.rank.dto.response.ApiResponse;
import com.code.rank.dto.response.AuthResponse;
import com.code.rank.dto.response.ErrorResponse;
import com.code.rank.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Public endpoints for user registration and login. No JWT required.")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new account with role USER. Returns a JWT immediately so the
                    client can call protected endpoints without a second round-trip.

                    Validation:
                    - username: 3-64 chars, must be unique
                    - email: valid email, must be unique
                    - password: 8-128 chars (hashed with BCrypt before storage)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "User created and JWT issued",
                    content = @Content(schema = @Schema(implementation = AuthResponseEnvelope.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "User registered",
                                      "data": {
                                        "token": "eyJhbGciOiJIUzI1NiJ9...",
                                        "tokenType": "Bearer",
                                        "expiresInMs": 86400000,
                                        "userId": 1,
                                        "username": "alice"
                                      },
                                      "timestamp": "2026-05-23T10:15:30Z"
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed or username/email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok("User registered", authService.register(request)));
    }

    @Operation(
            summary = "Login with username and password",
            description = "Verifies credentials and returns a signed JWT. " +
                    "Pass it on subsequent calls as `Authorization: Bearer <token>`."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Authenticated, JWT returned",
                    content = @Content(schema = @Schema(implementation = AuthResponseEnvelope.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Invalid username or password",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(request)));
    }

    @Schema(name = "AuthResponseEnvelope", description = "Envelope wrapping AuthResponse data")
    private static class AuthResponseEnvelope extends ApiResponse<AuthResponse> {
        public AuthResponseEnvelope() { super(true, null, null, null); }
    }
}
