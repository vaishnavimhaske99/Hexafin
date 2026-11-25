
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.LocationUpdateRequestDTO;
import com.example.demo.dto.UserLocationResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserLocationService;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "http://localhost:4200")
public class LocationController {

    private final UserLocationService locationService;

    public LocationController(UserLocationService locationService) {
        this.locationService = locationService;
    }

    // Update logged-in user's location
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','SALES')")
    public ResponseEntity<String> updateLocation(
            @RequestBody LocationUpdateRequestDTO request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        locationService.updateLocation(user.getId(), request);

        return ResponseEntity.ok("Location updated successfully");
    }

    // Get all locations - ADMIN only
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserLocationResponseDTO>> getAllLocations() {

        List<UserLocationResponseDTO> locations =
                locationService.getAllLocations();

        return ResponseEntity.ok(locations);
    }

    // Get specific user's location
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','SALES')")
    public ResponseEntity<UserLocationResponseDTO> getUserLocation(
            @PathVariable Long userId,
            Authentication authentication) {

        User loggedInUser = (User) authentication.getPrincipal();

        UserLocationResponseDTO location =
                locationService.getUserLocation(
                        loggedInUser.getId(),
                        loggedInUser.getRole(),
                        userId
                );

        return ResponseEntity.ok(location);
    }

    // Stop logged-in user's tracking
    @PostMapping("/stop")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','SALES')")
    public ResponseEntity<String> stopTracking(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        locationService.stopTracking(user.getId());

        return ResponseEntity.ok("Location tracking stopped");
    }
}
