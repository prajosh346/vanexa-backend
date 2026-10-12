
package com.vanexa.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BlogResponse(
        Long id,
        String title,
        String slug,
        String excerpt,
        String featuredImageUrl,
        String content,
        Long authorId,
        Long categoryId,
        String status,
        String language,
        String seoTitle,
        String seoDescription,
        String seoKeywords,
        Boolean commentsEnabled,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt,
        List<BlogTagResponse> tags
) {
}
