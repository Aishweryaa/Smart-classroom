package com.smartclassroom.service;

import com.smartclassroom.entity.User;
import com.smartclassroom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CustomUserDetailsService - bridges our User entity with Spring Security.
 * Spring Security calls loadUserByUsername() during authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Loads user by username for authentication.
     * Maps our User.Role to a Spring Security GrantedAuthority (ROLE_ADMIN, etc.)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with username: " + username));

        // Spring Security expects roles prefixed with "ROLE_"
        SimpleGrantedAuthority authority =
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.isEnabled(),
            true, true, true,       // account/credentials non-expired, non-locked
            List.of(authority)
        );
    }
}