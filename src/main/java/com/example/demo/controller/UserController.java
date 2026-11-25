package com.example.demo.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.UpdateUserHierarchyRequestDTO;
import com.example.demo.dto.UserRequestDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 🔥 ADMIN only - create user
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO createUser(@RequestBody UserRequestDTO request) {
        return userService.createUser(request);
    }

    // 🔥 Any logged-in user - own profile
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponseDTO getMyProfile(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new IllegalStateException("Authenticated principal is not a User object");
        }
        User loggedInUser = (User) principal;
        return userService.getUserById(loggedInUser.getId());
    }

    // 🔥 ADMIN, MANAGER, TEAM_LEAD - team members under a given team lead
    @GetMapping("/reports-to/{teamLeadId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public List<UserResponseDTO> getUsersByReportsTo(@PathVariable Long teamLeadId) {
        return userService.findByReportsToId(teamLeadId);
    }
 // UserController.java मध्ये हे ऍड करा:

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')") // फक्त ॲडमिन करू शकतो
    public UserResponseDTO deactivateUser(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "No Reason") String reason) {
        return userService.deactivateUser(id, reason);
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO reactivateUser(@PathVariable Long id) {
        return userService.reactivateUser(id);
    }
    // 🔥 MANAGER - team members
    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    public List<UserResponseDTO> getManagerTeam() {
        return userService.getManagerTeam();
    }

    // 🔥 ADMIN, MANAGER, TEAM_LEAD - view user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // 🔥 ADMIN, MANAGER, TEAM_LEAD - list all users
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    // 🔥 ADMIN only - update hierarchy
    @PutMapping("/{id}/hierarchy")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO updateUserHierarchy(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserHierarchyRequestDTO request) {
        return userService.updateUserHierarchy(id, request);
    }
}