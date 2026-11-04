
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.LeadActivityRequestDTO;
import com.example.demo.dto.LeadActivityResponseDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadActivity;
import com.example.demo.entity.User;
import com.example.demo.mapper.LeadActivityMapper;
import com.example.demo.repository.LeadActivityRepository;
import com.example.demo.repository.LeadRepository;

@Service
public class LeadActivityServiceImpl implements LeadActivityService {

    private final LeadActivityRepository leadActivityRepository;
    private final LeadRepository leadRepository;

    public LeadActivityServiceImpl(
            LeadActivityRepository leadActivityRepository,
            LeadRepository leadRepository) {
        this.leadActivityRepository = leadActivityRepository;
        this.leadRepository = leadRepository;
    }

    // Get currently logged-in user from JWT SecurityContext
    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User)) {

            throw new IllegalStateException(
                    "Authenticated user not found. Please login again.");
        }

        return (User) authentication.getPrincipal();
    }

    // CREATE ACTIVITY
    @Override
    @Transactional
    public LeadActivityResponseDTO createActivity(
            Long leadId,
            LeadActivityRequestDTO request) {

        // Identify the user from the JWT token
        User loggedInUser = getLoggedInUser();

        // Find lead using the lead ID from the URL
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lead not found with id: " + leadId));

        // Validate request
        if (request == null || request.getType() == null) {
            throw new IllegalArgumentException(
                    "Activity type is required.");
        }

        // Convert request DTO to entity
        LeadActivity activity =
                LeadActivityMapper.toEntity(request, lead);

        // Set current date/time if not supplied
        LocalDateTime now = LocalDateTime.now();

        if (activity.getActivityDateTime() == null) {
            activity.setActivityDateTime(now);
        }

        // Automatically store the logged-in user
        activity.setCreatedBy(loggedInUser);

        // Store activity creation timestamp
        activity.setCreatedAt(now);

        // Save activity in database
        LeadActivity savedActivity =
                leadActivityRepository.save(activity);

        // Return saved activity details
        return LeadActivityMapper.toResponseDTO(savedActivity);
    }

    // GET ALL ACTIVITIES OF A LEAD
    @Override
    @Transactional(readOnly = true)
    public List<LeadActivityResponseDTO> getActivitiesByLeadId(
            Long leadId) {

        if (!leadRepository.existsById(leadId)) {
            throw new RuntimeException(
                    "Lead not found with id: " + leadId);
        }

        return leadActivityRepository
                .findByLeadIdOrderByActivityDateTimeDesc(leadId)
                .stream()
                .map(LeadActivityMapper::toResponseDTO)
                .toList();
    }

    // GET SINGLE ACTIVITY
    @Override
    @Transactional(readOnly = true)
    public LeadActivityResponseDTO getActivityById(Long id) {

        LeadActivity activity = leadActivityRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Activity not found with id: " + id));

        return LeadActivityMapper.toResponseDTO(activity);
    }

    // UPDATE ACTIVITY
    @Override
    @Transactional
    public LeadActivityResponseDTO updateActivity(
            Long id,
            LeadActivityRequestDTO request) {

        LeadActivity activity = leadActivityRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Activity not found with id: " + id));

        if (request == null || request.getType() == null) {
            throw new IllegalArgumentException(
                    "Activity type is required.");
        }

        activity.setType(request.getType());

        if (request.getActivityDateTime() != null) {
            activity.setActivityDateTime(
                    request.getActivityDateTime());
        }

        activity.setDescription(request.getDescription());

        // Keep original createdBy and createdAt unchanged
        LeadActivity updatedActivity =
                leadActivityRepository.save(activity);

        return LeadActivityMapper.toResponseDTO(updatedActivity);
    }

    // DELETE ACTIVITY
    @Override
    @Transactional
    public void deleteActivity(Long id) {

        LeadActivity activity = leadActivityRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Activity not found with id: " + id));

        leadActivityRepository.delete(activity);
    }
}
