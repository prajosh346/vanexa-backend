package com.vanexa.backend.repository;

import com.vanexa.backend.entity.AuthorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorProfileRepository
        extends JpaRepository<AuthorProfile, Long> {

    Optional<AuthorProfile> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);
}