
package com.vanexa.backend.service;

import com.vanexa.backend.entity.User;
import com.vanexa.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminProvisioningService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminProvisioningService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createInitialAdmin(
            String name,
            String email,
            String rawPassword) {

        if (userRepository.count() > 0) {
            throw new IllegalStateException(
                    "Users already exist. Initial admin creation is disabled.");
        }

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || rawPassword == null || rawPassword.length() < 12) {
            throw new IllegalArgumentException(
                    "Provide a name, email, and password of at least 12 characters.");
        }

        User admin = new User();
        admin.setName(name.trim());
        admin.setEmail(email.trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole("ADMIN");

        return userRepository.save(admin);
    }
}
