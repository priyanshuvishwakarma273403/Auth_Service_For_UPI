package com.authService.repository;

import com.authService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository - JPA ke through MySQL access
 * Spring Data JPA automatically implementation generate karta hai
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByUpiId(String upiId);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    // Custom JPQL query - active users count
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

}
