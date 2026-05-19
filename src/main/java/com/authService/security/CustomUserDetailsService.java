package com.authService.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * ================================================================
 * Custom UserDetailsService
 * ================================================================
 * Spring Security ko batata hai ki user database se kaise load karna hai.
 *
 * Flow:
 * 1. Spring Security authentication ke time username (email) deta hai
 * 2. Yeh class database se user dhundti hai
 * 3. UserDetails return karta hai jisme roles aur password hai
 * 4. Spring Security khud password match karta hai
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {



    }
}
