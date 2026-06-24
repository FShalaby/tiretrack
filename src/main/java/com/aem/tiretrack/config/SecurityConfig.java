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
            .requestMatchers("/api/platform/**").hasRole("SUPER_ADMIN")
            .requestMatchers("/api/shop-locations/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/attendance/clock-in").hasRole("EMPLOYEE")
            .requestMatchers(HttpMethod.POST, "/api/attendance/clock-out").hasRole("EMPLOYEE")
            .requestMatchers(HttpMethod.GET, "/api/attendance/me/**").hasRole("EMPLOYEE")
            .requestMatchers(HttpMethod.GET, "/api/attendance/employees").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/attendance/day").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/attendance/employee/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/attendance/absences/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/attendance/*/resolve-absence").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/attendance/**").denyAll()

            // Admin-only business/financial routes
            .requestMatchers("/api/dashboard/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/settings/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/reports/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/accounting/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/audit-logs/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/customers/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
            .requestMatchers("/api/shifts/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/payroll/shift-slots/**").hasAnyRole("OWNER", "ADMIN", "EMPLOYEE")
            .requestMatchers(HttpMethod.POST, "/api/payroll/shift-slots/{slotId}/signup").hasRole("EMPLOYEE")
            .requestMatchers(HttpMethod.DELETE, "/api/payroll/shift-slots/{slotId}/signup").hasRole("EMPLOYEE")
            .requestMatchers(HttpMethod.POST, "/api/payroll/shift-slots/{slotId}/employees/{employeeId}").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/payroll/shift-slots/{slotId}/signups/{signupId}").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/payroll/shift-slots").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/payroll/shift-slots/{slotId}").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/payroll/periods/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/payroll/records/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers("/api/payroll/loans/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/payroll/employees").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/payroll/employees/*/settings").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/payroll/employees/{employeeId}/records").hasAnyRole("OWNER", "ADMIN", "EMPLOYEE")
            .requestMatchers("/api/payroll/employees/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN")

            // Admin + employee operational routes
            .requestMatchers("/api/work-orders/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN", "EMPLOYEE")
            .requestMatchers("/api/estimates/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN", "EMPLOYEE")
            .requestMatchers("/api/invoices/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN", "EMPLOYEE")
            .requestMatchers("/api/tires/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN", "EMPLOYEE")
            .requestMatchers("/api/appointments/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN", "EMPLOYEE")
            .requestMatchers("/api/tire-requests/**").hasAnyRole("SUPER_ADMIN", "OWNER", "ADMIN", "EMPLOYEE")
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
