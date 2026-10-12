package com.vanexa.backend.dto;

import java.time.LocalDateTime;

public record AuthorProfileResponse(

        Long id,
        Long userId,
        String displayName,
        String bio,
        String profileImageUrl,
        String websiteUrl,
        String facebookUrl,
        String instagramUrl,
        String linkedinUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}