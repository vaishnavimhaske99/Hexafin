
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.AssignLeadRequestDTO;
import com.example.demo.dto.LeadRequestDTO;
import com.example.demo.dto.LeadResponseDTO;

import com.example.demo.entity.User;
import com.example.demo.service.LeadService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    // =========================
    // GET LOGGED-IN USER
    // =========================
    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please login first"
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user details are unavailable. Check JWT authentication."
        );
    }

    // =========================
    // CREATE LEAD
    // TEMPORARY PUBLIC FOR LOCAL TESTING
    // =========================
    @PostMapping
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public LeadResponseDTO createLead(
            @Valid @RequestBody LeadRequestDTO request) {

        return leadService.createLead(request);
    }

    // =========================
    // GET ALL LEADS
    // =========================
    @GetMapping
    public List<LeadResponseDTO> getAllLeads() {

        return leadService.getAllLeads();
    }

    // =========================
    // GET LEAD BY ID
    // =========================

@GetMapping("/{id}")
public LeadResponseDTO getLeadById(@PathVariable Long id) {
    return leadService.getLeadById(id);
}

    // =========================
    // UPDATE LEAD
    // =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public LeadResponseDTO updateLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequestDTO request) {

        return leadService.updateLead(id, request);
    }

    // =========================
    // DELETE LEAD
    // =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteLead(
            @PathVariable Long id) {

        leadService.deleteLead(id);
    }

    // =========================
    // GET MY LEADS
    // =========================
    @GetMapping("/my-leads")
    @PreAuthorize("hasRole('SALES')")
    public List<LeadResponseDTO> getMyLeads() {

        User user = getLoggedInUser();

        return leadService.getMyLeads(user.getId());
    }

    // =========================
    // ASSIGN LEAD
    // =========================
    @PutMapping("/{leadId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public LeadResponseDTO assignLead(
            @PathVariable Long leadId,
            @Valid @RequestBody AssignLeadRequestDTO request) {

        return leadService.assignLead(leadId, request);
    }
}
