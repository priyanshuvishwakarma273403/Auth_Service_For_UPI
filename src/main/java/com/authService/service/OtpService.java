package com.authService.service;


import com.authService.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * ================================================================
 * OTP Service - Redis-backed OTP Management
 * ================================================================
 * OTP Flow:
 * 1. 6-digit OTP generate karo (SecureRandom - cryptographically safe)
 * 2. Redis mein store karo with 5-minute TTL (auto-expire)
 * 3. SMS/Email se bhejo (production mein real provider use karo)
 * 4. Verify karte waqt Redis se match karo
 *
 * Redis Key Pattern: otp:<phoneNumber>
 * Redis Value: 6-digit OTP string
 * TTL: 5 minutes
 *
 * Rate Limiting: ek phone number se max 3 OTP in 15 min
 * ================================================================
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_ATTEMPT_PREFIX = "otp_attempt:";
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final int ATTEMPT_WINDOW_MINUTES = 15;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * OTP Generate karo aur Redis mein save karo
     * Production mein yahan SMS gateway call hogi (Twilio, MSG91, etc.)
     */
    public void generateAndSendOtp(String phoneNumber){
        // Rate limiting check
        checkRateLimit(phoneNumber);

        // 6-digit OTP generate karo
        String otp = String.format("%06d", secureRandom.nextInt(999999));

        // Redis mein store karo with TTL
        String redisKey = OTP_PREFIX + phoneNumber;

        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMinutes(OTP_EXPIRY_MINUTES));

        // Attempt count increment karo
        String attemptKey = OTP_ATTEMPT_PREFIX + phoneNumber;
        redisTemplate.opsForValue().increment(attemptKey);
        redisTemplate.expire(attemptKey, Duration.ofMinutes(ATTEMPT_WINDOW_MINUTES));

//        TODO: Production mein real SMS bhejo
        // smsGateway.sendOtp(phoneNumber, otp);
        log.info("OTP generated for phone: {} (DEV MODE - OTP: {})", phoneNumber, otp);
        // SECURITY: Production mein yeh log line HATAO
    }

    /**
     * OTP Verify karo
     * Redis se fetch karo aur match karo
     */
    public boolean verifyOtp(String phoneNumber, String inputOtp){
        String redisKey = OTP_PREFIX + phoneNumber;
        String storedOtp = redisTemplate.opsForValue().get(redisKey);
        if(storedOtp == null){
            log.warn("OTP not found or expired for phone : {} ", phoneNumber);
            throw new AuthException("OTP expired or not found. Please request a new OTP.");
        }

        if(!storedOtp.equals(inputOtp)){
            log.warn("Invalid OTP for phone : {} ", phoneNumber);
            throw new AuthException("Invalid OTP. Please try again.");
        }


        // OTP use ho gaya - Redis se delete karo (one-time use)
        redisTemplate.delete(redisKey);
        log.info("OTP verified successfully for phone: {}", phoneNumber);
        return true;

    }

    /**
     * Rate Limiting - zyada OTP requests block karo
     */
    private void checkRateLimit(String phoneNumber) {
        String attemptKey = OTP_ATTEMPT_PREFIX + phoneNumber;
        String attempts = redisTemplate.opsForValue().get(attemptKey);

        if (attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS) {
            throw new AuthException(
                    "Too many OTP requests. Please wait 15 minutes before trying again.");
        }
    }
}
