package com.vanexa.backend.controller;

import com.vanexa.backend.dto.AuthorProfileRequest;
import com.vanexa.backend.dto.AuthorProfileResponse;
import com.vanexa.backend.entity.User;
import com.vanexa.backend.repository.UserRepository;
import com.vanexa.backend.service.AuthorProfileService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequestMapping("/api/authors")
public class AuthorProfileController {

    private final AuthorProfileService authorProfileService;
    private final UserRepository userRepository;

    public AuthorProfileController(
            AuthorProfileService authorProfileService,
            UserRepository userRepository) {

        this.authorProfileService = authorProfileService;
        this.userRepository = userRepository;
    }

    // Create an author profile
    @PostMapping("/{userId}/profile")
    public ResponseEntity<AuthorProfileResponse> createProfile(
            @PathVariable Long userId,
            @Valid @RequestBody AuthorProfileRequest request,
            Authentication authentication) {

        verifyProfileAccess(userId, authentication);

        AuthorProfileResponse response =
                authorProfileService.createProfile(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get an author's profile
    @GetMapping("/{userId}/profile")
    public ResponseEntity<AuthorProfileResponse> getProfile(
            @PathVariable Long userId,
            Authentication authentication) {

        verifyProfileAccess(userId, authentication);

        return ResponseEntity.ok(
                authorProfileService.getProfileByUserId(userId)
        );
    }

    // Update an author's profile
    @PutMapping("/{userId}/profile")
    public ResponseEntity<AuthorProfileResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody AuthorProfileRequest request,
            Authentication authentication) {

        verifyProfileAccess(userId, authentication);

        return ResponseEntity.ok(
                authorProfileService.updateProfile(userId, request)
        );
    }

    // Allow users to manage their own profile.
    // ADMIN and SUPER_ADMIN can manage other users' profiles.
    private void verifyProfileAccess(
            Long targetUserId,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Authenticated user not found"
                        )
                );

        String role = currentUser.getRole();

        String normalizedRole = role == null
                ? ""
                : role.trim().toUpperCase(Locale.ROOT);

        if (normalizedRole.startsWith("ROLE_")) {
            normalizedRole = normalizedRole.substring(5);
        }

        boolean isAdmin =
                normalizedRole.equals("ADMIN")
                        || normalizedRole.equals("SUPER_ADMIN");

        boolean isOwnProfile =
                currentUser.getId().equals(targetUserId);

        if (!isAdmin && !isOwnProfile) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only manage your own author profile"
            );
        }
    }
}