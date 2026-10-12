package com.vanexa.backend.service;

import com.vanexa.backend.entity.RevokedToken;
import com.vanexa.backend.repository.RevokedTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenRevocationService(
            RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    /**
     * Revoke a JWT by storing its SHA-256 hash.
     * The original JWT is never stored in the database.
     */
    @Transactional
    public void revokeToken(String token, Instant expiresAt) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT token is required");
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "Token expiration time is required");
        }

        // Already-expired tokens do not need a revocation record.
        if (!expiresAt.isAfter(Instant.now())) {
            return;
        }

        String tokenHash = hashToken(token);

        // Make repeated logout requests idempotent.
        if (revokedTokenRepository.existsByTokenHash(tokenHash)) {
            return;
        }

        RevokedToken revokedToken =
                new RevokedToken(tokenHash, expiresAt);

        revokedTokenRepository.save(revokedToken);
    }

    /**
     * Check whether a JWT has been revoked.
     */
    @Transactional(readOnly = true)
    public boolean isTokenRevoked(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        String tokenHash = hashToken(token);

        return revokedTokenRepository.existsByTokenHash(tokenHash);
    }

    /**
     * Produce a SHA-256 hexadecimal hash of the JWT.
     */
    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception);
        }
    }
}
