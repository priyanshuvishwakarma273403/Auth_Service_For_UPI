package com.authService.service;

import com.authService.repository.UserRepository;
import com.authService.security.CustomUserDetailsService;
import com.authService.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ================================================================
 * Auth Service - Core Business Logic
 * ================================================================
 * Yeh service authentication ki saari business logic handle karti hai:
 * 1. User registration + UPI ID generation
 * 2. Login + JWT token generation
 * 3. Token refresh
 * 4. OTP verification (Redis mein store)
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final CustomUserDetailsService userDetailsService;

}
