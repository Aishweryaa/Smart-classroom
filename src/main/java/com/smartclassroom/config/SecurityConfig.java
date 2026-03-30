package com.smartclassroom.config;

import com.smartclassroom.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig - configures Spring Security for the application.
 *
 * URL access rules:
 *   /login, /register, /css/**, /js/** → public (no auth required)
 *   /admin/**                           → ADMIN role only
 *   /faculty/**                         → FACULTY role only
 *   everything else                     → authenticated users
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on controller methods
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * BCrypt password encoder - strength 10 (standard for production).
     * Used both when registering (encoding) and login (matching).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ---- Authorization Rules ----
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**",
                                 "/js/**", "/images/**", "/error").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/faculty/**").hasAnyRole("FACULTY", "ADMIN")
                .anyRequest().authenticated()
            )
            // ---- Form Login ----
            .formLogin(form -> form
                .loginPage("/login")                  // custom login page
                .loginProcessingUrl("/login")         // Spring handles POST /login
                .successHandler((req, res, auth) -> { // role-based redirect after login
                    var roles = auth.getAuthorities();
                    if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
                        res.sendRedirect("/admin/dashboard");
                    } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_FACULTY"))) {
                        res.sendRedirect("/faculty/dashboard");
                    } else {
                        res.sendRedirect("/student/dashboard");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // ---- Logout ----
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            // ---- Session Management ----
            .sessionManagement(session -> session
                .maximumSessions(1)  // one session per user
            );

        return http.build();
    }
}
