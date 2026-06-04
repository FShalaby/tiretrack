package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aem.tiretrack.dto.PlatformUserRequest;
import com.aem.tiretrack.dto.UserResponse;
import com.aem.tiretrack.service.PlatformUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/platform/users")
public class PlatformUserController {
    private final PlatformUserService platformUserService;

    public PlatformUserController(PlatformUserService platformUserService) {
        this.platformUserService = platformUserService;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return platformUserService.getAllUsers().stream().map(UserResponse::new).toList();
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody PlatformUserRequest request) {
        return new UserResponse(platformUserService.createUser(request));
    }

    @PostMapping("/{id}/activate")
    public UserResponse activateUser(@PathVariable Long id) {
        return new UserResponse(platformUserService.activateUser(id));
    }

    @PostMapping("/{id}/deactivate")
    public UserResponse deactivateUser(@PathVariable Long id) {
        return new UserResponse(platformUserService.deactivateUser(id));
    }
}
