package com.vanexa.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorProfileRequest(

        @NotBlank(message = "Display name is required")
        @Size(max = 150, message = "Display name cannot exceed 150 characters")
        String displayName,

        String bio,

        @Size(max = 1000)
        String profileImageUrl,

        @Size(max = 500)
        String websiteUrl,

        @Size(max = 500)
        String facebookUrl,

        @Size(max = 500)
        String instagramUrl,

        @Size(max = 500)
        String linkedinUrl

) {}