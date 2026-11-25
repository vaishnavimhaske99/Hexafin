
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.LocationUpdateRequestDTO;
import com.example.demo.dto.UserLocationResponseDTO;
import com.example.demo.entity.NotificationCategory;
import com.example.demo.entity.NotificationPriority;
import com.example.demo.entity.NotificationRequestDTO;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.User;
import com.example.demo.entity.UserLocation;
import com.example.demo.entity.UserLocationHistory;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.UserLocationRepository;
import com.example.demo.repository.UserLocationHistoryRepository;
import com.example.demo.repository.UserRepository;

@Service
public class UserLocationService {

    private final UserLocationRepository locationRepository;
    private final UserLocationHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public UserLocationService(
            UserLocationRepository locationRepository,
            UserLocationHistoryRepository historyRepository,
            UserRepository userRepository,
            NotificationService notificationService) {

        this.locationRepository = locationRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // Update current location
    @Transactional
    public void updateLocation(
            Long userId,
            LocationUpdateRequestDTO request) {

        if (request.getLatitude() == null ||
                request.getLongitude() == null) {
            throw new IllegalArgumentException(
                    "Latitude and Longitude are required");
        }

        if (request.getLatitude() < -90 ||
                request.getLatitude() > 90) {
            throw new IllegalArgumentException("Invalid latitude");
        }

        if (request.getLongitude() < -180 ||
                request.getLongitude() > 180) {
            throw new IllegalArgumentException("Invalid longitude");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        UserLocation location = locationRepository
                .findByUserId(userId)
                .orElseGet(UserLocation::new);

        boolean wasTracking =
                Boolean.TRUE.equals(location.getIsTracking());

        location.setUser(user);
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAccuracy(request.getAccuracy());
        location.setIsTracking(true);
        location.setRecordedAt(now);

        locationRepository.save(location);

        // Save location history
        UserLocationHistory history = new UserLocationHistory();
        history.setUser(user);
        history.setLatitude(request.getLatitude());
        history.setLongitude(request.getLongitude());
        history.setAccuracy(request.getAccuracy());
        history.setRecordedAt(now);

        historyRepository.save(history);

        // Notify only when tracking starts
        if (!wasTracking) {
            sendLocationNotification(
                    user,
                    NotificationType.LIVE_LOCATION_STARTED,
                    "Live Location Started",
                    " has started sharing live location."
            );
        }
    }

    // Stop tracking
    @Transactional
    public void stopTracking(Long userId) {

        UserLocation location = locationRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Location record not found"));

        if (!Boolean.TRUE.equals(location.getIsTracking())) {
            return;
        }

        location.setIsTracking(false);
        locationRepository.save(location);

        User user = location.getUser();

        sendLocationNotification(
                user,
                NotificationType.LIVE_LOCATION_STOPPED,
                "Live Location Stopped",
                " has stopped sharing live location."
        );
    }

    // Send notification to all ADMINs and user's own TEAM_LEAD
    private void sendLocationNotification(
            User user,
            NotificationType type,
            String title,
            String messageSuffix) {

        Set<Long> recipientIds = new LinkedHashSet<>();

        List<User> admins =
                userRepository.findByRole(UserRole.ADMIN);

        for (User admin : admins) {
            if (!admin.getId().equals(user.getId())
                    && "ACTIVE".equalsIgnoreCase(admin.getStatus())) {
                recipientIds.add(admin.getId());
            }
        }

        User teamLeader = user.getReportsTo();

        if (teamLeader != null
                && teamLeader.getRole() == UserRole.TEAM_LEAD
                && !teamLeader.getId().equals(user.getId())
                && "ACTIVE".equalsIgnoreCase(teamLeader.getStatus())) {

            recipientIds.add(teamLeader.getId());
        }

        String userName =
                user.getFirstName() + " " + user.getLastName();

        for (Long recipientId : recipientIds) {

            NotificationRequestDTO request =
                    new NotificationRequestDTO();

            request.setType(type);
            request.setCategory(NotificationCategory.LOCATION);
            request.setPriority(NotificationPriority.NORMAL);
            request.setTitle(title);
            request.setMessage(userName + messageSuffix);
            request.setReferenceType("USER");
            request.setReferenceId(user.getId());
            request.setActionUrl(
                    "/users/" + user.getId() + "/profile"
            );

            notificationService.createNotification(
                    recipientId,
                    user.getId(),
                    request
            );
        }
    }

    // Get one user's location with authorization
    @Transactional(readOnly = true)
    public UserLocationResponseDTO getUserLocation(
            Long loggedInUserId,
            UserRole loggedInRole,
            Long targetUserId) {

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() ->
                        new RuntimeException("Target user not found"));

        boolean authorized = false;

        if (loggedInRole == UserRole.ADMIN) {
            authorized = true;
        }

        else if (loggedInRole == UserRole.SALES) {
            authorized = loggedInUserId.equals(targetUserId);
        }

        else if (loggedInRole == UserRole.TEAM_LEAD) {

            User parent = targetUser.getReportsTo();

            authorized = parent != null
                    && parent.getId().equals(loggedInUserId)
                    && targetUser.getRole() == UserRole.SALES;
        }

        else if (loggedInRole == UserRole.MANAGER) {

            User current = targetUser.getReportsTo();

            while (current != null) {

                if (current.getId().equals(loggedInUserId)) {
                    authorized = true;
                    break;
                }

                current = current.getReportsTo();
            }
        }

        if (!authorized) {
            throw new RuntimeException(
                    "You are not authorized to view this location");
        }

        UserLocation location = locationRepository
                .findByUserId(targetUserId)
                .orElseThrow(() ->
                        new RuntimeException("User location not found"));

        return mapToResponseDTO(location);
    }

    // Get all users' latest locations for Admin
    @Transactional(readOnly = true)
    public List<UserLocationResponseDTO> getAllLocations() {

        List<UserLocation> locations = locationRepository.findAll();

        List<UserLocationResponseDTO> response = new ArrayList<>();

        for (UserLocation location : locations) {
            response.add(mapToResponseDTO(location));
        }

        return response;
    }

    // Convert entity to response DTO
    private UserLocationResponseDTO mapToResponseDTO(
            UserLocation location) {

        UserLocationResponseDTO dto =
                new UserLocationResponseDTO();

        User user = location.getUser();

        dto.setUserId(user.getId());
        dto.setUserName(
                user.getFirstName() + " " + user.getLastName()
        );
        dto.setRole(user.getRole().name());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setAccuracy(location.getAccuracy());
        dto.setTracking(location.getIsTracking());
        dto.setRecordedAt(location.getRecordedAt());

        return dto;
    }
}
