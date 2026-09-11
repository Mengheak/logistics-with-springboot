package com.chheang.mengheak.logisticsapis.controllers;

import com.chheang.mengheak.logisticsapis.dto.auth.request.LoginRequest;
import com.chheang.mengheak.logisticsapis.dto.auth.request.RefreshTokenRequest;
import com.chheang.mengheak.logisticsapis.dto.auth.request.RegisterRequest;
import com.chheang.mengheak.logisticsapis.dto.auth.response.LoginResponse;
import com.chheang.mengheak.logisticsapis.dto.auth.response.UserAccountResponse;
import com.chheang.mengheak.logisticsapis.dto.base.Response;
import com.chheang.mengheak.logisticsapis.services.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public endpoints. Everything else in the API needs the access token issued here; the refresh
 * token is what lets a client get a new one without asking for the password again.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response> register(@Valid @RequestBody RegisterRequest request) {
        UserAccountResponse created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.created("account registered successfully", created));
    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        LoginResponse login = authService.login(request, userAgent);
        return ResponseEntity.ok(Response.ok("logged in successfully", login));
    }

    /**
     * Rotates the pair: the presented token is consumed and a replacement comes back, so a
     * leaked token is only useful until the real client next refreshes.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Response> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        LoginResponse refreshed = authService.refresh(request, userAgent);
        return ResponseEntity.ok(Response.ok("refreshed token successfully", refreshed));
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(Response.success("200", "success", "logged out successfully"));
    }

    /** Ends every session for the account, for use after a suspected token leak. */
    @PostMapping("/logout-all")
    public ResponseEntity<Response> logoutAll(@Valid @RequestBody RefreshTokenRequest request) {
        int revoked = authService.logoutAll(request);
        return ResponseEntity.ok(Response.ok("logged out of all sessions successfully",
                Map.of("revoked_sessions", revoked)));
    }
}
