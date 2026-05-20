package com.authService.service;

import com.authService.dto.request.LoginRequest;
import com.authService.dto.request.RegisterRequest;
import com.authService.dto.response.AuthResponse;
import com.authService.entity.RefreshToken;
import com.authService.entity.Role;
import com.authService.entity.User;
import com.authService.exception.AuthException;
import com.authService.repository.RefreshTokenRepository;
import com.authService.repository.RoleRepository;
import com.authService.repository.UserRepository;
import com.authService.security.CustomUserDetailsService;
import com.authService.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

//**
//        * ================================================================
//        * Auth Service - Core Business Logic
// * ================================================================
//         * Yeh service authentication ki saari business logic handle karti hai:
//        * 1. User registration + UPI ID generation
// * 2. Login + JWT token generation
// * 3. Token refresh
// * 4. OTP verification (Redis mein store)
// * ================================================================
//         */
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

    /**
     * User Registration
     * ================
     * 1. Duplicate email/phone check
     * 2. Password BCrypt encrypt karo
     * 3. UPI ID generate karo (phone@upimesh)
     * 4. Default ROLE_USER assign karo
     * 5. Database mein save karo
     * 6. OTP send karo phone verification ke liye
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Duplicate checks
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AuthException("Phone number already registered");
        }

        // Default role fetch (ROLE_USER)
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new AuthException("Default role not found. Run data initialization."));

        // UPI ID generate: phoneNumber@upimesh
        String upiId = request.getPhoneNumber() + "@upimesh";

        // User entity build karo
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hash
                .upiId(upiId)
                .roles(Set.of(userRole))
                .isActive(true)
                .isPhoneVerified(false)
                .isEmailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} with UPI ID: {}", savedUser.getId(), upiId);

        // OTP send karo phone verification ke liye
        otpService.generateAndSendOtp(request.getPhoneNumber());

        // Token generate karo
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails, savedUser.getId(), "ROLE_USER");
        String refreshToken = createRefreshToken(savedUser);

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    /**
     * User Login
     * ==========
     * 1. Email/Password validate karo (AuthenticationManager se)
     * 2. JWT Access Token generate karo (15 min validity)
     * 3. Refresh Token generate karo (7 days validity)
     * 4. Response return karo
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // Spring Security se authenticate karo
            // Yeh automatically password match karta hai (BCrypt)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AuthException("User not found"));

            // Purana refresh token delete karo (single session)
            refreshTokenRepository.deleteByUser(user);

            // Roles string banao
            String roles = userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);

            // Tokens generate karo
            String accessToken = jwtUtil.generateAccessToken(userDetails, user.getId(), roles);
            String refreshToken = createRefreshToken(user);

            log.info("Login successful for user: {}", user.getId());
            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for email: {}", request.getEmail());
            throw new AuthException("Invalid email or password");
        }
    }

    /**
     * Refresh Token se naya Access Token generate karo
     */
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        // Expiry check
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException("Refresh token expired. Please login again.");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);

        String newAccessToken = jwtUtil.generateAccessToken(userDetails, user.getId(), roles);
        String newRefreshToken = createRefreshToken(user);

        // Purana refresh token delete karo (rotation strategy)
        refreshTokenRepository.delete(refreshToken);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    /**
     * Refresh Token database mein save karo
     */
    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(java.util.UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000L)) // 7 days
                .build();
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    /**
     * AuthResponse build karna - common helper
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L) // 15 minutes in seconds
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .upiId(user.getUpiId())
                .issuedAt(LocalDateTime.now())
                .build();
    }
}
