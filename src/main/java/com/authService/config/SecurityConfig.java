package com.authService.config;

import com.authService.security.CustomUserDetailsService;
import com.authService.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ================================================================
 * Spring Security Configuration
 * ================================================================
 * Security configuration:
 * - JWT-based Stateless authentication (koi session nahi)
 * - CSRF disabled (REST API ke liye zaruri nahi)
 * - BCrypt password encoding
 * - Public endpoints define kiye hain
 * ================================================================
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // @PreAuthorize annotations enable karta hai
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;


    /**
     * Password Encoder - BCrypt with strength 12
     * Strength jitna zyada, utna secure (lekin slow)
     * Production mein 12 recommended hai
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication Provider
     * UserDetailsService aur PasswordEncoder ko connect karta hai
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Authentication Manager - programmatic authentication ke liye
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Main Security Filter Chain
     * Yahan define hota hai ki kaunse endpoints public hain aur kaunse protected
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disable - REST APIs stateless hote hain
                .csrf(AbstractHttpConfigurer::disable)
                // Authorization rules
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                        // Public endpoints - koi bhi access kar sakta hai
                        "/auth/register",
                        "/auth/login",
                        "/auth/verify-otp",
                        "/auth/refresh-token",
                        "/actuator/health",
                        "/actuator/info",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()
                        // Admin only endpoints
                        .requestMatchers("/auth/admin/**").hasRole("ADMIN")
                        // Baaki sab authenticated user ke liye
                                .anyRequest().authenticated()
                )

        // Stateless - har request mein token chahiye, koi session nahi
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Custom auth provider
                .authenticationProvider(authenticationProvider())

                // JWT filter add karo UsernamePasswordAuthenticationFilter se pehle
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }


    
}
