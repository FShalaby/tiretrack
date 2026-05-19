package com.aem.tiretrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.aem.tiretrack.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

   @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth

            // Public auth endpoints
            .requestMatchers("/api/auth/**").permitAll()

            // Admin-only business/financial routes
            .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
            .requestMatchers("/api/settings/**").hasRole("ADMIN")
            .requestMatchers("/api/reports/**").hasRole("ADMIN")
            .requestMatchers("/api/audit/**").hasRole("ADMIN")

            // Admin + employee operational routes
            .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "EMPLOYEE")
            .requestMatchers("/api/tires/**").hasAnyRole("ADMIN", "EMPLOYEE")
            .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "EMPLOYEE")
            .requestMatchers("/api/public/**").permitAll()
            // Anything else requires login
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}