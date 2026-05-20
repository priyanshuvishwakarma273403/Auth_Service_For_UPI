package com.authService.util;

import com.authService.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * ================================================================
 * Scheduled Tasks - Background Jobs
 * ================================================================
 * Yeh class periodic cleanup jobs chalati hai.
 *
 * cleanupExpiredRefreshTokens():
 * - Har raat 2 AM par chalti hai (cron expression)
 * - Expired refresh tokens database se delete karta hai
 * - Warna table bahut badi ho jaayegi
 * ================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Expired refresh tokens cleanup
     * Cron: "0 0 2 * * ?" = Har raat 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        log.info("Starting expired refresh token cleanup...");
        int deleted = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Expired refresh token cleanup complete. Deleted: {} tokens", deleted);
    }
}

