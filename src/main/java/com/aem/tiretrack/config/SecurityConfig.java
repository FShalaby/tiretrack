package com.aem.tiretrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'"))
            .frameOptions(frame -> frame.deny())
            .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
        )
        .authorizeHttpRequests(auth -> auth

            // Public auth endpoints
            .requestMatchers("/api/auth/**").permitAll()

            // Admin-only business/financial routes
            .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
            .requestMatchers("/api/settings/**").hasRole("ADMIN")
            .requestMatchers("/api/reports/**").hasRole("ADMIN")
            .requestMatchers("/api/accounting/**").hasRole("ADMIN")
            .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")
            .requestMatchers("/api/customers/**").hasRole("ADMIN")
            .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
            .requestMatchers("/api/shifts/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/payroll/shift-slots/**").hasAnyRole("ADMIN", "EMPLOYEE")
            .requestMatchers(HttpMethod.POST, "/api/payroll/shift-slots/{slotId}/signup").hasRole("EMPLOYEE")
            .requestMatchers(HttpMethod.DELETE, "/api/payroll/shift-slots/{slotId}/signup").hasRole("EMPLOYEE")
            .requestMatchers(HttpMethod.POST, "/api/payroll/shift-slots/{slotId}/employees/{employeeId}").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/payroll/shift-slots/{slotId}/signups/{signupId}").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/payroll/shift-slots").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/payroll/shift-slots/{slotId}").hasRole("ADMIN")
            .requestMatchers("/api/payroll/periods/**").hasRole("ADMIN")
            .requestMatchers("/api/payroll/records/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/payroll/employees").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/payroll/employees/*/settings").hasRole("ADMIN")

            // Admin + employee operational routes
            .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "EMPLOYEE")
            .requestMatchers("/api/tires/**").hasAnyRole("ADMIN", "EMPLOYEE")
            .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "EMPLOYEE")
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/payroll/employees/**").hasAnyRole("ADMIN", "EMPLOYEE")
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
