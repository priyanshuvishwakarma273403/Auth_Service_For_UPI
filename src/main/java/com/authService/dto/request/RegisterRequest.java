package com.authService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Registration Request DTO
 * Client yeh data bhejta hai register karne ke liye
 */

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name required")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "Email required")
    @Email(message = "Valid email required")
    private String email;

    @NotBlank(message = "Phone number required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Valid 10-digit Indian mobile number required")
    private String phoneNumber;

    @NotBlank(message = "Password required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "Password must have uppercase, lowercase, digit, and special character")
    private String password;

}
