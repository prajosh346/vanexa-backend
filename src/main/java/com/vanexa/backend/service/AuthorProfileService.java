package com.vanexa.backend.service;

import com.vanexa.backend.dto.AuthorProfileRequest;
import com.vanexa.backend.dto.AuthorProfileResponse;
import com.vanexa.backend.entity.AuthorProfile;
import com.vanexa.backend.entity.User;
import com.vanexa.backend.repository.AuthorProfileRepository;
import com.vanexa.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorProfileService {

    private final AuthorProfileRepository authorProfileRepository;
    private final UserRepository userRepository;

    public AuthorProfileService(
            AuthorProfileRepository authorProfileRepository,
            UserRepository userRepository) {
        this.authorProfileRepository = authorProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AuthorProfileResponse createProfile(
            Long userId,
            AuthorProfileRequest request) {

        if (authorProfileRepository.existsByUser_Id(userId)) {
            throw new IllegalArgumentException(
                    "An author profile already exists for user: " + userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId));

        AuthorProfile profile = new AuthorProfile();
        profile.setUser(user);
        applyRequest(profile, request);

        AuthorProfile savedProfile =
                authorProfileRepository.save(profile);

        return toResponse(savedProfile);
    }

    @Transactional(readOnly = true)
    public AuthorProfileResponse getProfileByUserId(Long userId) {

        AuthorProfile profile =
                authorProfileRepository.findByUser_Id(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Author profile not found for user: "
                                                + userId));

        return toResponse(profile);
    }

    @Transactional
    public AuthorProfileResponse updateProfile(
            Long userId,
            AuthorProfileRequest request) {

        AuthorProfile profile =
                authorProfileRepository.findByUser_Id(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Author profile not found for user: "
                                                + userId));

        applyRequest(profile, request);

        AuthorProfile updatedProfile =
                authorProfileRepository.save(profile);

        return toResponse(updatedProfile);
    }

    private void applyRequest(
            AuthorProfile profile,
            AuthorProfileRequest request) {

        if (request.displayName() == null
                || request.displayName().isBlank()) {
            throw new IllegalArgumentException(
                    "Display name is required");
        }

        profile.setDisplayName(request.displayName().trim());
        profile.setBio(request.bio());
        profile.setProfileImageUrl(request.profileImageUrl());
        profile.setWebsiteUrl(request.websiteUrl());
        profile.setFacebookUrl(request.facebookUrl());
        profile.setInstagramUrl(request.instagramUrl());
        profile.setLinkedinUrl(request.linkedinUrl());
    }

    private AuthorProfileResponse toResponse(
            AuthorProfile profile) {

        return new AuthorProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getProfileImageUrl(),
                profile.getWebsiteUrl(),
                profile.getFacebookUrl(),
                profile.getInstagramUrl(),
                profile.getLinkedinUrl(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}