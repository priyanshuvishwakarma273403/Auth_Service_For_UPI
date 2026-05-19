package com.authService.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Authentication Response DTO
 * Login/Register successful hone par yeh response milta hai
 */
@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;        // seconds mein
    private Long userId;
    private String email;
    private String fullName;
    private String upiId;
    private String roles;
    private LocalDateTime issuedAt;

}
