 package com.vanexa.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BlogRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,

        @NotBlank(message = "Slug is required")
        @Size(max = 250)
        String slug,

        String excerpt,

        String featuredImageUrl,

        @NotBlank(message = "Content is required")
        String content,

        Long authorId,

        Long categoryId,

        String status,

        String language,

        String seoTitle,

        String seoDescription,

        String seoKeywords,

        Boolean commentsEnabled,

        List<Long> tagIds,

        List<Long> visibilityTypeIds

) {}