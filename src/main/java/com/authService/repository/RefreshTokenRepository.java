package com.authService.repository;

import com.authService.entity.RefreshToken;
import com.authService.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * RefreshToken Repository
 *
 * Refresh token:
 * - Login ke baad generate hota hai
 * - 7 din mein expire hota hai
 * - Har naye login par purana delete hota hai (single session strategy)
 * - Rotation: refresh token use hone par naya token milta hai, purana delete
 */

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    // User logout ya re-login par purana token delete karo
    @Modifying
    @Transactional
    void deleteByUser(User user);

    // Expired tokens cleanup karo (scheduler se call hoga)
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteExpiredTokens(Instant now);

    boolean existsByUser(User user);
}
