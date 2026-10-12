
package com.vanexa.backend.controller;

import com.vanexa.backend.service.JwtService;
import com.vanexa.backend.service.TokenRevocationService;

import io.jsonwebtoken.JwtException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class LogoutController {

    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    public LogoutController(
            JwtService jwtService,
            TokenRevocationService tokenRevocationService) {

        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            ) String authorizationHeader) {

        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(
                true, 0, "Bearer ", 0, 7)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Bearer token is required");
        }

        String token = authorizationHeader.substring(7).trim();

        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Bearer token is required");
        }

        try {
            // Validate the JWT signature and expiration.
            jwtService.extractEmail(token);

            Instant expiresAt = jwtService.extractExpiration(token);

            // Store the token hash so future requests reject this JWT.
            tokenRevocationService.revokeToken(token, expiresAt);

            return ResponseEntity.noContent().build();

        } catch (JwtException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }
    }
}
