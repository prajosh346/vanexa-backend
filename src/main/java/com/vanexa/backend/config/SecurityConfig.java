
package com.vanexa.backend.config;

import com.vanexa.backend.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Enable CORS configuration from CorsConfig.java
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // JWT-based stateless authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Public login and logout endpoints
                        // LogoutController validates the JWT explicitly
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/logout"
                        ).permitAll()

                        // CORS preflight requests
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Public blog reading
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/blogs/public",
                                "/api/blogs/public/**"
                        ).permitAll()

                        // Public blog view counter
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/blogs/public/*/view"
                        ).permitAll()

                        // Public tag listing through BlogController
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/blogs/tags"
                        ).permitAll()

                        // Public category reading
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categories",
                                "/api/categories/**"
                        ).permitAll()

                        // ADMIN-only blog date filter
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/blogs/filter"
                        ).hasRole("ADMIN")

                        // Only ADMIN can restore blogs
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/blogs/*/restore"
                        ).hasRole("ADMIN")

                        // ADMIN and AUTHOR can access the status endpoint.
                        // BlogController enforces ownership and publishing rules.
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/blogs/*/status"
                        ).hasAnyRole("ADMIN", "AUTHOR")

                        // Only ADMIN can delete blogs
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/blogs/*",
                                "/api/blogs/*/permanent"
                        ).hasRole("ADMIN")

                        // ADMIN-only category and tag creation
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categories/**",
                                "/api/tags/**"
                        ).hasRole("ADMIN")

                        // ADMIN-only category and tag updates
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categories/**",
                                "/api/tags/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/categories/**",
                                "/api/tags/**"
                        ).hasRole("ADMIN")

                        // ADMIN-only category and tag deletion
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categories/**",
                                "/api/tags/**"
                        ).hasRole("ADMIN")

                        // Blog access; ownership is checked in BlogController
                        .requestMatchers("/api/blogs/**")
                        .hasAnyRole("ADMIN", "AUTHOR")

                        // Image upload
                        .requestMatchers("/api/uploads/**")
                        .hasAnyRole("ADMIN", "AUTHOR")

                        // Author profile access; ownership is checked
                        // in the author profile controller
                        .requestMatchers("/api/authors/**")
                        .hasAnyRole("ADMIN", "AUTHOR")

                        // All remaining endpoints are ADMIN-only
                        .anyRequest().hasRole("ADMIN")
                )

                // Add JWT authentication filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }
}
