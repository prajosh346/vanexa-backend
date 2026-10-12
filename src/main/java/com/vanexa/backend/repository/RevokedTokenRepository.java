
        package com.vanexa.backend.repository;

import com.vanexa.backend.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, Long> {

    boolean existsByTokenHash(String tokenHash);

    Optional<RevokedToken> findByTokenHash(String tokenHash);

    long deleteAllByExpiresAtBefore(Instant cutoff);
}
