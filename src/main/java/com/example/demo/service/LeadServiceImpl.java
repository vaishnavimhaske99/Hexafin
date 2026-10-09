
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AssignLeadRequestDTO;
import com.example.demo.dto.LeadRequestDTO;
import com.example.demo.dto.LeadResponseDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.mapper.LeadMapper;
import com.example.demo.repository.LeadRepository;
import com.example.demo.repository.UserRepository;

@Service
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LeadServiceImpl(
            LeadRepository leadRepository,
            UserRepository userRepository,
            NotificationService notificationService) {

        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // =========================
    // CREATE LEAD
    // =========================

    @Override
    public LeadResponseDTO createLead(LeadRequestDTO request) {

        Lead lead = LeadMapper.toEntity(request);

        lead.setStatus(LeadStatus.NEW);

        LocalDateTime now = LocalDateTime.now();

        lead.setCreatedAt(now);
        lead.setUpdatedAt(now);

        Lead savedLead = leadRepository.save(lead);

        return LeadMapper.toResponseDTO(savedLead);
    }

    // =========================
    // GET ALL LEADS
    // =========================

    @Override
    public List<LeadResponseDTO> getAllLeads() {

        List<Lead> leads = leadRepository.findAll();

        return leads.stream()
                .map(LeadMapper::toResponseDTO)
                .toList();
    }

    // =========================
    // GET LEAD BY ID
    // =========================

    @Override
    public LeadResponseDTO getLeadById(Long id) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lead not found with id: " + id));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        // ADMIN can view any lead
        if (user.getRole() == UserRole.ADMIN) {
            return LeadMapper.toResponseDTO(lead);
        }

        // SALES can view only assigned leads
        if (user.getRole() == UserRole.SALES) {

            if (lead.getAssignedTo() == null ||
                    !lead.getAssignedTo().getId().equals(user.getId())) {

                throw new RuntimeException(
                        "You are not authorized to view this lead");
            }

            return LeadMapper.toResponseDTO(lead);
        }

        // Other roles are not allowed
        throw new RuntimeException(
                "You are not authorized to view this lead");
    }

    // =========================
    // UPDATE LEAD
    // =========================

    @Override
    public LeadResponseDTO updateLead(
            Long id,
            LeadRequestDTO request) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lead not found with id: " + id));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        // ADMIN can update any lead
        if (user.getRole() == UserRole.ADMIN) {

            // allowed

        }

        // SALES can update only assigned lead
        else if (user.getRole() == UserRole.SALES) {

            if (lead.getAssignedTo() == null ||
                    !lead.getAssignedTo().getId().equals(user.getId())) {

                throw new RuntimeException(
                        "You are not authorized to update this lead");
            }
        }

        // Other roles are not allowed
        else {

            throw new RuntimeException(
                    "You are not authorized to update this lead");
        }

        // =========================
        // UPDATE LEAD FIELDS
        // =========================

        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setCompanyName(request.getCompanyName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());

        lead.setAddressLine1(request.getAddressLine1());
        lead.setAddressLine2(request.getAddressLine2());

        lead.setCity(request.getCity());
        lead.setState(request.getState());
        lead.setCountry(request.getCountry());
        lead.setPincode(request.getPincode());

        lead.setSource(request.getSource());
        lead.setDescription(request.getDescription());

        lead.setUpdatedAt(LocalDateTime.now());

        Lead updatedLead = leadRepository.save(lead);

        return LeadMapper.toResponseDTO(updatedLead);
    }

    // =========================
    // DELETE LEAD
    // =========================

    @Override
    public void deleteLead(Long id) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lead not found with id: " + id));

        leadRepository.delete(lead);
    }

    // =========================
    // ASSIGN LEAD
    // =========================

    @Override
    public LeadResponseDTO assignLead(
            Long leadId,
            AssignLeadRequestDTO request) {

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lead not found with id: " + leadId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: "
                                        + request.getUserId()));

        // Only SALES users can receive leads
        if (user.getRole() == null ||
                user.getRole() != UserRole.SALES) {

            throw new RuntimeException(
                    "Lead can only be assigned to SALES users");
        }

        // Assign lead
        lead.setAssignedTo(user);

        lead.setUpdatedAt(LocalDateTime.now());

        Lead updatedLead = leadRepository.save(lead);

        // =========================
        // CREATE NOTIFICATION
        // =========================

        notificationService.createNotification(
                user.getId(),
                "New Lead Assigned",
                "Lead #" + lead.getId()
                        + " has been assigned to you."
        );

        return LeadMapper.toResponseDTO(updatedLead);
    }

    // =========================
    // GET MY LEADS
    // =========================

    @Override
    public List<LeadResponseDTO> getMyLeads(Long userId) {

        List<Lead> leads =
                leadRepository.findByAssignedToId(userId);

        return leads.stream()
                .map(LeadMapper::toResponseDTO)
                .toList();
    }
}
