package com.example.demo.controller;

import java.util.List;

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

import com.example.demo.dto.AssignLeadRequestDTO;
import com.example.demo.dto.LeadRequestDTO;
import com.example.demo.dto.LeadResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
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
    // CREATE LEAD
    // =========================
    @PostMapping
    public LeadResponseDTO createLead(
            @Valid @RequestBody LeadRequestDTO request) {

        return leadService.createLead(request);
    }

    // =========================
    // GET ALL / MY LEADS
    // =========================
    @GetMapping
    public List<LeadResponseDTO> getAllLeads() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        // ADMIN -> see all leads
        if (user.getRole() == UserRole.ADMIN) {
            return leadService.getAllLeads();
        }

        // SALES -> see only assigned leads
        return leadService.getMyLeads(user.getId());
    }

    // =========================
    // GET LEAD BY ID
    // =========================
    @GetMapping("/{id}")
    public LeadResponseDTO getLeadById(
            @PathVariable Long id) {

        return leadService.getLeadById(id);
    }

    // =========================
    // UPDATE LEAD
    // =========================
    @PutMapping("/{id}")
    public LeadResponseDTO updateLead(
            @PathVariable Long id,
            @RequestBody LeadRequestDTO request) {

        return leadService.updateLead(id, request);
    }

    // =========================
    // DELETE LEAD
    // =========================
    @DeleteMapping("/{id}")
    public void deleteLead(
            @PathVariable Long id) {

        leadService.deleteLead(id);
    }

    // =========================
    // GET MY LEADS
    // =========================
    @GetMapping("/my-leads")
    public List<LeadResponseDTO> getMyLeads() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        return leadService.getMyLeads(user.getId());
    }

    // =========================
    // ASSIGN LEAD
    // =========================
    @PutMapping("/{leadId}/assign")
    public LeadResponseDTO assignLead(
            @PathVariable Long leadId,
            @RequestBody AssignLeadRequestDTO request) {

        return leadService.assignLead(leadId, request);
    }
}