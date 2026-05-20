package com.authService.repository;

import com.authService.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Role Repository
 * Roles database se fetch karne ke liye.
 * Data initialization mein roles pehle insert honge (ROLE_USER, ROLE_MERCHANT, ROLE_ADMIN)
 */

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Role.RoleName name);

    boolean existsByName(Role.RoleName name);


}
