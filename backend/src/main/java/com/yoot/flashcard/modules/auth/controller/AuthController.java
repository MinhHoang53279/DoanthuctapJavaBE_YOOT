package com.yoot.flashcard.modules.auth.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import com.yoot.flashcard.modules.auth.dto.AuthTokenResponse;
import com.yoot.flashcard.modules.auth.dto.AuthUserResponse;
import com.yoot.flashcard.modules.auth.dto.LoginRequest;
import com.yoot.flashcard.modules.auth.dto.LogoutRequest;
import com.yoot.flashcard.modules.auth.dto.RefreshTokenRequest;
import com.yoot.flashcard.modules.auth.dto.RegisterRequest;
import com.yoot.flashcard.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success("Auth module is ready", Map.of("module", "auth"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthUserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered", authService.register(request)));
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Token refreshed", authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserResponse> currentUser() {
        return ApiResponse.success("Current user retrieved", authService.currentUser());
    }
}
