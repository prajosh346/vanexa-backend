package com.vanexa.backend.service;

import com.vanexa.backend.entity.Role;
import com.vanexa.backend.entity.User;
import com.vanexa.backend.repository.UserRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> login(String email, String password) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email.trim().toLowerCase(Locale.ROOT),
                                password
                        )
                );

        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(() ->
                new IllegalStateException(
                        "Authenticated user not found")
        );

        Role role = user.getRoleEntity();

        if (role == null || role.getName() == null
                || role.getName().isBlank()) {
            throw new IllegalStateException(
                    "User role is not configured");
        }

        String roleName = role.getName()
                .trim()
                .toUpperCase(Locale.ROOT);

        String token = jwtService.generateToken(
                user.getEmail(),
                roleName
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 86400);
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", roleName);

        return response;
    }
}