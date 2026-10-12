package com.vanexa.backend.controller;

import com.vanexa.backend.dto.BlogRequest;
import com.vanexa.backend.dto.BlogResponse;
import com.vanexa.backend.dto.BlogStatusRequest;
import com.vanexa.backend.entity.Blog;
import com.vanexa.backend.entity.User;
import com.vanexa.backend.repository.UserRepository;
import com.vanexa.backend.service.BlogService;

import jakarta.validation.Valid;
import com.vanexa.backend.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;
    private final UserRepository userRepository;

    public BlogController(
            BlogService blogService,
            UserRepository userRepository) {
        this.blogService = blogService;
        this.userRepository = userRepository;
    }

    // -------------------- SECURITY HELPERS --------------------

    private User getCurrentUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required");
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Authenticated user not found"));
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private void requireAdmin(Authentication authentication) {

        User user = getCurrentUser(authentication);

        if (!isAdmin(user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "ADMIN permission is required");
        }
    }

    private Blog verifyBlogOwnership(
            Long blogId,
            Authentication authentication,
            boolean draftsOnly) {

        User user = getCurrentUser(authentication);
        Blog blog = blogService.getBlogById(blogId);

        if (isAdmin(user)) {
            return blog;
        }

        if (blog.getAuthorId() == null
                || !blog.getAuthorId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only manage your own blogs");
        }

        if (draftsOnly
                && !"DRAFT".equalsIgnoreCase(blog.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This action is allowed only for your own draft blogs");
        }

        return blog;
    }

    // -------------------- CREATE BLOG --------------------

    @PostMapping
    public ResponseEntity<Blog> createBlog(
            @Valid @RequestBody BlogRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        BlogRequest effectiveRequest = request;

        // Authors cannot assign a blog to another user's ID.
        // Their new blogs always start as drafts.
        if (!isAdmin(currentUser)) {
            effectiveRequest = new BlogRequest(
                    request.title(),
                    request.slug(),
                    request.excerpt(),
                    request.featuredImageUrl(),
                    request.content(),
                    currentUser.getId(),
                    request.categoryId(),
                    "DRAFT",
                    request.language(),
                    request.seoTitle(),
                    request.seoDescription(),
                    request.seoKeywords(),
                    request.commentsEnabled(),
                    request.tagIds(),
                    request.visibilityTypeIds()
            );
        }

        Blog blog = blogService.createBlog(effectiveRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(blog);
    }

    // -------------------- GET BLOG BY ID --------------------

    @GetMapping("/{id}")
    public ResponseEntity<Blog> getBlogById(
            @PathVariable Long id,
            Authentication authentication) {

        verifyBlogOwnership(id, authentication, false);

        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<BlogResponse> getBlogDetailsById(
            @PathVariable Long id,
            Authentication authentication) {

        verifyBlogOwnership(id, authentication, false);

        return ResponseEntity.ok(
                blogService.getBlogResponseById(id));
    }

    // -------------------- ADMIN / AUTHOR BLOG LIST --------------------

    @GetMapping
    public ResponseEntity<Page<Blog>> getAllBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        Page<Blog> result;

        if (isAdmin(user)) {
            // ADMIN sees all non-deleted blogs from all authors.
            result = blogService.getAllBlogs(page, size);
        } else {
            // AUTHOR sees only their own non-deleted blogs.
            result = blogService.getBlogsByAuthorId(
                    user.getId(), page, size);
        }

        return ResponseEntity.ok(result);
    }

    // -------------------- UPDATE BLOG --------------------

    @PutMapping("/{id}")
    public ResponseEntity<Blog> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogRequest request,
            Authentication authentication) {

        // An author may update their own draft OR published blog.
        verifyBlogOwnership(id, authentication, false);

        User user = getCurrentUser(authentication);
        BlogRequest effectiveRequest = request;

        if (!isAdmin(user)) {
            // Never trust the authorId supplied in the request.
            effectiveRequest = new BlogRequest(
                    request.title(),
                    request.slug(),
                    request.excerpt(),
                    request.featuredImageUrl(),
                    request.content(),
                    user.getId(),
                    request.categoryId(),
                    request.status(),
                    request.language(),
                    request.seoTitle(),
                    request.seoDescription(),
                    request.seoKeywords(),
                    request.commentsEnabled(),
                    request.tagIds(),
                    request.visibilityTypeIds()
            );
        }

        return ResponseEntity.ok(
                blogService.updateBlog(id, effectiveRequest));
    }

    // -------------------- DELETE / RESTORE --------------------

    // Only ADMIN can soft-delete a blog.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(
            @PathVariable Long id,
            Authentication authentication) {

        requireAdmin(authentication);

        blogService.softDeleteBlog(id);

        return ResponseEntity.noContent().build();
    }

    // Only ADMIN can restore a soft-deleted blog.
    @PatchMapping("/{id}/restore")
    public ResponseEntity<Blog> restoreBlog(
            @PathVariable Long id,
            Authentication authentication) {

        requireAdmin(authentication);

        return ResponseEntity.ok(blogService.restoreBlog(id));
    }

    // Only ADMIN can permanently delete a blog.
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteBlog(
            @PathVariable Long id,
            Authentication authentication) {

        requireAdmin(authentication);

        blogService.permanentlyDeleteBlog(id);

        return ResponseEntity.noContent().build();
    }

    // -------------------- BLOG TAG ASSOCIATIONS --------------------

    // Authors may modify tags only on their own blogs.
    @DeleteMapping("/{id}/tags")
    public ResponseEntity<Void> deleteAllBlogTags(
            @PathVariable Long id,
            Authentication authentication) {

        verifyBlogOwnership(id, authentication, false);

        blogService.deleteAllBlogTags(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<Void> deleteBlogTag(
            @PathVariable Long id,
            @PathVariable Long tagId,
            Authentication authentication) {

        verifyBlogOwnership(id, authentication, false);

        blogService.deleteBlogTag(id, tagId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/tags")
    public ResponseEntity<Void> updateBlogTags(
            @PathVariable Long id,
            @RequestBody List<Long> tagIds,
            Authentication authentication) {

        verifyBlogOwnership(id, authentication, false);

        blogService.updateBlogTags(id, tagIds);

        return ResponseEntity.noContent().build();
    }

    // -------------------- PUBLISHING --------------------

    @PatchMapping("/{id}/status")
    public ResponseEntity<Blog> updateBlogStatus(
            @PathVariable Long id,
            @Valid @RequestBody BlogStatusRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        if (!isAdmin(currentUser)) {

            // AUTHOR can publish only their own draft.
            verifyBlogOwnership(id, authentication, true);

            // AUTHOR cannot archive or unpublish blogs.
            if (!"PUBLISHED".equalsIgnoreCase(request.status())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Authors can only publish their own draft blogs");
            }
        }

        // ADMIN may set DRAFT, PUBLISHED, or ARCHIVED.
        return ResponseEntity.ok(
                blogService.updateBlogStatus(id, request.status()));
    }

    // -------------------- PUBLIC BLOG APIs --------------------

    @GetMapping("/public")
    public ResponseEntity<Page<Blog>> getPublishedBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                blogService.getPublishedBlogs(page, size));
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<Blog>> searchPublishedBlogs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                blogService.searchPublishedBlogs(
                        keyword, page, size));
    }

    @GetMapping("/public/category/{categoryId}")
    public ResponseEntity<Page<Blog>> getPublishedBlogsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                blogService.getPublishedBlogsByCategory(
                        categoryId, page, size));
    }

    @GetMapping("/public/tag/{tagId}")
    public ResponseEntity<Page<Blog>> getPublishedBlogsByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                blogService.getPublishedBlogsByTag(
                        tagId, page, size));
    }

    @GetMapping("/public/{slug}")
    public ResponseEntity<Blog> getPublishedBlogBySlug(
            @PathVariable String slug) {

        return ResponseEntity.ok(
                blogService.getPublishedBlogBySlug(slug));
    }

    @PostMapping("/public/{slug}/view")
    public ResponseEntity<Long> incrementBlogViewCount(
            @PathVariable String slug) {

        return ResponseEntity.ok(
                blogService.incrementPublishedBlogViewCount(slug));
    }

    // -------------------- ADMIN DATE FILTER --------------------

    // ADMIN receives blogs by date across all authors.
    @GetMapping("/filter")
    public ResponseEntity<Page<Blog>> getBlogsByDateFilter(
            @RequestParam(defaultValue = "ALL_TIME") String filter,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        requireAdmin(authentication);

        return ResponseEntity.ok(
                blogService.getBlogsByDateFilter(
                        filter, startDate, endDate, page, size));
    }
    // -------------------- TAG LIST --------------------

    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(blogService.getAllTags());
    }
}