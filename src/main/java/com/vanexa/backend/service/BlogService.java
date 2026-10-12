
package com.vanexa.backend.service;

import com.vanexa.backend.dto.BlogRequest;
import com.vanexa.backend.entity.Blog;
import com.vanexa.backend.entity.BlogTag;
import com.vanexa.backend.entity.Tag;
import com.vanexa.backend.repository.BlogRepository;
import com.vanexa.backend.repository.BlogTagRepository;
import com.vanexa.backend.repository.TagRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

import java.time.LocalTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vanexa.backend.dto.BlogResponse;
import com.vanexa.backend.dto.BlogTagResponse;
import com.vanexa.backend.entity.BlogVisibility;
import com.vanexa.backend.entity.VisibilityType;
import com.vanexa.backend.repository.BlogVisibilityRepository;
import com.vanexa.backend.repository.VisibilityTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final TagRepository tagRepository;
    private final BlogTagRepository blogTagRepository;
    private final BlogVisibilityRepository blogVisibilityRepository;
    private final VisibilityTypeRepository visibilityTypeRepository;

    public BlogService(
            BlogRepository blogRepository,
            TagRepository tagRepository,
            BlogTagRepository blogTagRepository,
            BlogVisibilityRepository blogVisibilityRepository,
            VisibilityTypeRepository visibilityTypeRepository) {

        this.blogRepository = blogRepository;
        this.tagRepository = tagRepository;
        this.blogTagRepository = blogTagRepository;
        this.blogVisibilityRepository = blogVisibilityRepository;
        this.visibilityTypeRepository = visibilityTypeRepository;
    }

    private void updateBlogVisibility(Long blogId, List<Long> visibilityTypeIds) {

        blogVisibilityRepository.deleteByBlog_Id(blogId);

        if (visibilityTypeIds == null || visibilityTypeIds.isEmpty()) {
            return;
        }

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found"));

        for (Long visibilityTypeId : visibilityTypeIds) {

            VisibilityType visibilityType =
                    visibilityTypeRepository.findById(visibilityTypeId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Visibility type not found: " + visibilityTypeId));

            BlogVisibility blogVisibility =
                    new BlogVisibility(blog, visibilityType);

            blogVisibilityRepository.save(blogVisibility);
        }
    }

    @Transactional
    public Blog createBlog(BlogRequest request) {

        if (blogRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException(
                    "Blog slug already exists: " + request.slug()
            );
        }

        Blog blog = new Blog();
        blog.setTitle(request.title());
        blog.setSlug(request.slug());
        blog.setExcerpt(request.excerpt());
        blog.setFeaturedImageUrl(request.featuredImageUrl());
        blog.setContent(request.content());
        blog.setAuthorId(request.authorId());
        blog.setCategoryId(request.categoryId());

        // New blogs are created as drafts
        blog.setStatus("DRAFT");

        blog.setLanguage(
                request.language() != null
                        ? request.language()
                        : "en"
        );

        blog.setSeoTitle(request.seoTitle());
        blog.setSeoDescription(request.seoDescription());
        blog.setSeoKeywords(request.seoKeywords());

        blog.setCommentsEnabled(
                request.commentsEnabled() != null
                        ? request.commentsEnabled()
                        : true
        );

        blog.setViewCount(0L);
        blog.setDeleted(false);

        Blog savedBlog = blogRepository.save(blog);

        // Save blog tags
        if (request.tagIds() != null) {
            for (Long tagId : request.tagIds()) {

                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Tag not found: " + tagId
                                )
                        );

                BlogTag blogTag = new BlogTag(savedBlog, tag);
                blogTagRepository.save(blogTag);
            }
        }

        // Save visibility labels
        updateBlogVisibility(
                savedBlog.getId(),
                request.visibilityTypeIds()
        );

        return savedBlog;
    }

    @Transactional(readOnly = true)
    public Blog getBlogById(Long id) {
        return blogRepository.findById(id)
                .filter(blog -> !Boolean.TRUE.equals(blog.getDeleted()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Blog not found"));
    }
    @Transactional
    public void softDeleteBlog(Long id) {
        Blog blog = getBlogById(id);
        blog.setDeleted(true);
        blogRepository.save(blog);
    }

    @Transactional
    public Blog restoreBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Blog not found"));

        if (!Boolean.TRUE.equals(blog.getDeleted())) {
            throw new IllegalArgumentException("Blog is not deleted");
        }

        blog.setDeleted(false);
        return blogRepository.save(blog);
    }

    @Transactional
    public void permanentlyDeleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Blog not found"));

        blogTagRepository.deleteByBlog_Id(id);
        blogVisibilityRepository.deleteByBlog_Id(id);

        blogRepository.delete(blog);
    }
    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }
    }
    @Transactional(readOnly = true)
    public Page<Blog> getBlogsByAuthorId(
            Long authorId,
            int page,
            int size) {

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return blogRepository.findAllByAuthorIdAndDeletedFalse(
                authorId, pageable);
    }
    @Transactional(readOnly = true)
    public Page<Blog> getAllBlogs(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return blogRepository.findAllByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public BlogResponse getBlogResponseById(Long id) {

        Blog blog = getBlogById(id);

        List<BlogTagResponse> tags =
                blogTagRepository.findByBlog_Id(blog.getId())
                        .stream()
                        .map(blogTag -> new BlogTagResponse(
                                blogTag.getTag().getId(),
                                blogTag.getTag().getName(),
                                blogTag.getTag().getSlug()
                        ))
                        .toList();

        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getSlug(),
                blog.getExcerpt(),
                blog.getFeaturedImageUrl(),
                blog.getContent(),
                blog.getAuthorId(),
                blog.getCategoryId(),
                blog.getStatus(),
                blog.getLanguage(),
                blog.getSeoTitle(),
                blog.getSeoDescription(),
                blog.getSeoKeywords(),
                blog.getCommentsEnabled(),
                blog.getViewCount(),
                blog.getCreatedAt(),
                blog.getUpdatedAt(),
                blog.getPublishedAt(),
                tags
        );
    }

    @Transactional(readOnly = true)
    public Page<Blog> getBlogsByDateFilter(
            String filter,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {

        validatePagination(page, size);

        LocalDate today = LocalDate.now();
        LocalDateTime startDateTime;
        LocalDateTime endDateTime = LocalDateTime.now();

        String normalizedFilter = filter == null
                ? "ALL_TIME"
                : filter.trim().toUpperCase(Locale.ROOT);

        switch (normalizedFilter) {
            case "ALL_TIME":
                return getAllBlogs(page, size);

            case "TODAY":
                startDateTime = today.atStartOfDay();
                break;

            case "LAST_7_DAYS":
                startDateTime = today.minusDays(6).atStartOfDay();
                break;

            case "LAST_30_DAYS":
                startDateTime = today.minusDays(29).atStartOfDay();
                break;

            case "THIS_MONTH":
                startDateTime =
                        today.withDayOfMonth(1).atStartOfDay();
                break;

            case "THIS_YEAR":
                startDateTime =
                        today.withDayOfYear(1).atStartOfDay();
                break;

            case "CUSTOM":
                if (startDate == null || endDate == null) {
                    throw new IllegalArgumentException(
                            "Start date and end date are required for CUSTOM");
                }

                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException(
                            "Start date cannot be after end date");
                }

                startDateTime = startDate.atStartOfDay();
                endDateTime = endDate.atTime(LocalTime.MAX);
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid date filter: " + normalizedFilter);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return blogRepository.findAllByDeletedFalseAndCreatedAtBetween(
                startDateTime,
                endDateTime,
                pageable
        );
    }

    @Transactional
    public void deleteAllBlogTags(Long blogId) {
        getBlogById(blogId);
        blogTagRepository.deleteByBlog_Id(blogId);
    }

    @Transactional
    public void deleteBlogTag(Long blogId, Long tagId) {
        getBlogById(blogId);

        blogTagRepository.deleteByBlog_IdAndTag_Id(blogId, tagId);
    }

    @Transactional
    public void updateBlogTags(Long blogId, List<Long> tagIds) {
        getBlogById(blogId);

        blogTagRepository.deleteByBlog_Id(blogId);

        if (tagIds != null) {
            for (Long tagId : tagIds) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Tag not found: " + tagId));

                BlogTag blogTag = new BlogTag(
                        blogRepository.findById(blogId).orElseThrow(),
                        tag
                );

                blogTagRepository.save(blogTag);
            }
        }
    }

    @Transactional
    public Blog updateBlog(Long id, BlogRequest request) {

        Blog blog = getBlogById(id);

        String newSlug = request.slug().trim();

        // Check slug uniqueness only when the slug changes
        if (!blog.getSlug().equals(newSlug)
                && blogRepository.existsBySlug(newSlug)) {
            throw new IllegalArgumentException(
                    "A blog with this slug already exists");
        }

        blog.setTitle(request.title().trim());
        blog.setSlug(newSlug);
        blog.setExcerpt(request.excerpt());
        blog.setFeaturedImageUrl(request.featuredImageUrl());
        blog.setContent(request.content());
        blog.setAuthorId(request.authorId());
        blog.setCategoryId(request.categoryId());

        // Preserve the existing status during content updates
        blog.setLanguage(
                request.language() == null || request.language().isBlank()
                        ? blog.getLanguage()
                        : request.language().trim()
        );

        blog.setSeoTitle(request.seoTitle());
        blog.setSeoDescription(request.seoDescription());
        blog.setSeoKeywords(request.seoKeywords());

        if (request.commentsEnabled() != null) {
            blog.setCommentsEnabled(request.commentsEnabled());
        }

        Blog updatedBlog = blogRepository.save(blog);

        // Update tags only when tagIds is provided
        if (request.tagIds() != null) {
            updateBlogTags(id, request.tagIds());
        }
        if (request.visibilityTypeIds() != null) {
            updateBlogVisibility(id, request.visibilityTypeIds());
        }
        return updatedBlog;
    }

    @Transactional
    public Blog updateBlogStatus(Long id, String status) {

        Blog blog = getBlogById(id);

        String normalizedStatus = status.trim().toUpperCase();

        Set<String> allowedStatuses =
                Set.of("DRAFT", "PUBLISHED", "ARCHIVED");

        if (!allowedStatuses.contains(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: DRAFT, PUBLISHED, ARCHIVED");
        }

        if ("PUBLISHED".equals(normalizedStatus)) {
            // Set the publication date on first publication
            if (blog.getPublishedAt() == null) {
                blog.setPublishedAt(LocalDateTime.now());
            }
        } else {
            // Clear publication date when no longer published
            blog.setPublishedAt(null);
        }

        blog.setStatus(normalizedStatus);

        return blogRepository.save(blog);
    }

    @Transactional(readOnly = true)
    public Page<Blog> getPublishedBlogs(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "publishedAt")
        );

        return blogRepository.findAllByStatusAndDeletedFalse(
                "PUBLISHED",
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<Blog> searchPublishedBlogs(
            String keyword,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        if (keyword == null || keyword.isBlank()) {
            return getPublishedBlogs(page, size);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "publishedAt")
        );

        return blogRepository.searchPublishedBlogs(
                keyword.trim(),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<Blog> getPublishedBlogsByCategory(
            Long categoryId,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "publishedAt")
        );

        return blogRepository.findAllByCategoryIdAndStatusAndDeletedFalse(
                categoryId,
                "PUBLISHED",
                pageable
        );
    }
    @Transactional(readOnly = true)
    public Page<Blog> getPublishedBlogsByTag(
            Long tagId,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        if (!tagRepository.existsById(tagId)) {
            throw new IllegalArgumentException(
                    "Tag not found: " + tagId);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "publishedAt")
        );

        return blogRepository.findPublishedBlogsByTagId(
                tagId,
                pageable
        );
    }
    @Transactional
    public Long incrementPublishedBlogViewCount(String slug) {

        int updated = blogRepository.incrementPublishedViewCountBySlug(slug);

        if (updated == 0) {
            throw new IllegalArgumentException("Published blog not found");
        }

        return blogRepository
                .findBySlugAndStatusAndDeletedFalse(slug, "PUBLISHED")
                .orElseThrow(() ->
                        new IllegalArgumentException("Published blog not found"))
                .getViewCount();
    }

    @Transactional(readOnly = true)
    public Blog getPublishedBlogBySlug(String slug) {
        return blogRepository
                .findBySlugAndStatusAndDeletedFalse(slug, "PUBLISHED")
                .orElseThrow(() ->
                        new IllegalArgumentException("Published blog not found"));
    }

}
