package com.authService.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * ================================================================
 * JWT Utility Class
 * ================================================================
 * JWT (JSON Web Token) teen parts mein hota hai:
 * Header.Payload.Signature
 *
 * Yeh class:
 * 1. Token generate karti hai (login ke baad)
 * 2. Token validate karti hai (har request par)
 * 3. Token se user info nikalti hai
 *
 * Secret key: application.yml mein base64 encoded honi chahiye
 * ================================================================
 */

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiry:900000}") // 15 minutes
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry:604800000}") // 7 days default
    private long refreshTokenExpiry;

    /**
     * Secret key ko SigningKey mein convert karta hai
     * HMAC-SHA256 algorithm use karta hai
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * Access Token generate karta hai
     * Claims mein userId aur roles add karta hai
     */
    public String generateAccessToken(UserDetails userDetails, Long userId, String roles) {
        Map<String , Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("roles", roles);
        extraClaims.put("type", "ACCESS");
        return buildToken(extraClaims, userDetails.getUsername(), refreshTokenExpiry);

    }

    /**
     * JWT token build karta hai
     */
    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Token se username (email) nikalta hai
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Token se userId nikalta hai
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Token valid hai ya nahi check karta hai
     * - Username match hona chahiye
     * - Token expire nahi hona chahiye
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Token ko parse karke saare claims nikalta hai
     * Agar token invalid hai to JwtException throw hogi
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
