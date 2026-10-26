package com.uniassist.controller;

import com.uniassist.dto.AuthDtos.*;
import com.uniassist.security.UserPrincipal;
import com.uniassist.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Student & Admin registration, login, profile, and session management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new student account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return new ResponseEntity<>(authService.register(req), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user with email or student ID and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated student or admin profile")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.getCurrentUser(currentUser.getId()));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for the authenticated user")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody PasswordChangeRequest req) {
        authService.changePassword(currentUser.getId(), req);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }
}
