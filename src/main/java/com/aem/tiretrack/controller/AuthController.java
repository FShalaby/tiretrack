package com.aem.tiretrack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import com.aem.tiretrack.service.AuthService;
import com.aem.tiretrack.dto.auth.LoginRequest;
import com.aem.tiretrack.dto.auth.RegisterRequest;
import com.aem.tiretrack.dto.auth.LoginResponse;
@RestController
@RequestMapping("/api/auth")
public class AuthController 
{
    
    AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest request)
    {
        return authService.register(request);
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request)
    {
        return authService.login(request);
    }


}
