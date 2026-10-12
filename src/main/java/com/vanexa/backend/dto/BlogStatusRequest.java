
package com.vanexa.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record BlogStatusRequest(
        @NotBlank(message = "Status is required")
        String status
) {}
