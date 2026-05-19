package com.authService.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login Request DTO
 */
@Data
public class LoginRequest {
    @NotBlank(message = "Email required")
    private String email;

    @NotBlank(message = "Password required")
    private String password;

}
