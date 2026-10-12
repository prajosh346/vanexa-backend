
package com.vanexa.backend.repository;

import com.vanexa.backend.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    Optional<Blog> findBySlugAndDeletedFalse(String slug);

    boolean existsBySlug(String slug);

    Page<Blog> findAllByDeletedFalse(Pageable pageable);

    Page<Blog> findAllByStatusAndDeletedFalse(
            String status,
            Pageable pageable
    );

    Optional<Blog> findBySlugAndStatusAndDeletedFalse(
            String slug,
            String status
    );
    Page<Blog> findAllByAuthorIdAndDeletedFalse(
            Long authorId,
            Pageable pageable
    );
    Page<Blog> findAllByCategoryIdAndStatusAndDeletedFalse(
            Long categoryId,
            String status,
            Pageable pageable
    );

    Page<Blog> findAllByDeletedFalseAndCreatedAtBetween(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );


    @Modifying
    @Query("""
                UPDATE Blog b
                SET b.viewCount = COALESCE(b.viewCount, 0) + 1
                WHERE b.slug = :slug
                  AND b.status = 'PUBLISHED'
                  AND b.deleted = false
            """)
    int incrementPublishedViewCountBySlug(@Param("slug") String slug);

    @Query("""
            SELECT b FROM Blog b
            WHERE b.status = 'PUBLISHED'
              AND b.deleted = false
              AND (
                  LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Blog> searchPublishedBlogs(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
                SELECT DISTINCT b
                FROM Blog b
                JOIN BlogTag bt ON bt.blog.id = b.id
                WHERE bt.tag.id = :tagId
                  AND b.status = 'PUBLISHED'
                  AND b.deleted = false
            """)
    Page<Blog> findPublishedBlogsByTagId(
            @Param("tagId") Long tagId,
            Pageable pageable
    );
}
