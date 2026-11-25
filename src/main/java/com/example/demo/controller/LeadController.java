
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.example.demo.entity.LeadImportStatus;
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
    @PutMapping("/{leadId}/assign")
    public ResponseEntity<?> assignLead(
            @PathVariable Long leadId,
            @RequestBody AssignLeadRequestDTO request) {

        return ResponseEntity.ok(
                leadService.assignLead(
                        leadId,
                        request.getAssignedToId()
                )
        );
    }
    // =========================================
    // GET LEADS ASSIGNED TO SELECTED USER
    // =========================================
    @GetMapping("/assigned-to/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public List<LeadResponseDTO> getLeadsAssignedToUser(
            @PathVariable Long userId) {

        return leadService.getLeadsByAssignedTo(userId);
    }

    // =========================================
    // GET LOGGED-IN USER
    // =========================================
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

    // =========================================
    // CREATE LEAD
    // TEMPORARY PUBLIC FOR LOCAL TESTING
    // =========================================
    @PostMapping
    // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public LeadResponseDTO createLead(
            @Valid @RequestBody LeadRequestDTO request) {

        return leadService.createLead(request);
    }

    // =========================================
    // GET ALL LEADS
    // =========================================
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public List<LeadResponseDTO> getAllLeads() {
        return leadService.getAllLeads();
    }

    // =========================================
    // GET LEAD BY ID
    // =========================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public LeadResponseDTO getLeadById(@PathVariable Long id) {
        return leadService.getLeadById(id);
    }

    // =========================================
    // UPDATE LEAD
    // =========================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public LeadResponseDTO updateLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequestDTO request) {

        return leadService.updateLead(id, request);
    }

    // =========================================
    // DELETE LEAD - ADMIN ONLY
    // =========================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
    }

    // =========================================
    // GET MY LEADS - SALES ONLY
    // =========================================
    @GetMapping("/my-leads")
    @PreAuthorize("hasRole('SALES')")
    public List<LeadResponseDTO> getMyLeads() {

        User user = getLoggedInUser();

        return leadService.getMyLeads(user.getId());
    }

    // =========================================
    // GET LEADS BY SALES USER
    // =========================================
    @GetMapping("/sales-user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public List<LeadResponseDTO> getLeadsBySalesUser(
            @PathVariable Long userId) {

        return leadService.getLeadsBySalesUser(userId);
    }

    // =========================================
    // GET TEAM LEADS
    // =========================================
    @GetMapping("/team-leads")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'MANAGER')")
    public List<LeadResponseDTO> getTeamLeads(
            Authentication authentication) {

        User loggedInUser = (User) authentication.getPrincipal();

        return leadService.getTeamLeads(loggedInUser.getId());
    }

    // =========================================
    // ASSIGN / REASSIGN LEAD
    // ADMIN, MANAGER, TEAM_LEAD
    // =========================================
    @PutMapping("/{leadId}/assign/{assignedToId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public LeadResponseDTO assignLead(
            @PathVariable Long leadId,
            @PathVariable Long assignedToId) {

        return leadService.assignLead(leadId, assignedToId);
    }

    // =========================================
    // RAW LEADS - GET ALL
    // =========================================
    @GetMapping("/raw")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public List<LeadResponseDTO> getRawLeads() {

        return leadService.getRawLeads();
    }

    // =========================================
    // RAW LEADS - FILTER BY IMPORT STATUS
    // =========================================
    @GetMapping("/raw/status/{importStatus}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public List<LeadResponseDTO> getRawLeadsByImportStatus(
            @PathVariable LeadImportStatus importStatus) {

        return leadService.getRawLeadsByImportStatus(importStatus);
    }

    // =========================================
    // RAW LEADS - COUNT
    // =========================================
    @GetMapping("/raw/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public long countRawLeads() {

        return leadService.countRawLeads();
    }
}
