
package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.dto.LeadRequestDTO;
import com.example.demo.dto.LeadResponseDTO;
import com.example.demo.entity.LeadImportStatus;

public interface LeadService {

    // =========================================
    // CREATE LEAD
    // =========================================

    LeadResponseDTO createLead(LeadRequestDTO request);


    // =========================================
    // ALL LEADS
    // =========================================

    // Existing method - preserve compatibility
    List<LeadResponseDTO> getAllLeads();

    // New paginated method
    Page<LeadResponseDTO> getAllLeads(Pageable pageable);


    // =========================================
    // LEAD DETAILS
    // =========================================

    LeadResponseDTO getLeadById(Long id);


    // =========================================
    // ASSIGNED LEADS
    // =========================================

    // Existing method
    List<LeadResponseDTO> getLeadsByAssignedTo(Long userId);

    // New paginated method
    Page<LeadResponseDTO> getLeadsByAssignedTo(
            Long userId,
            Pageable pageable
    );


    // =========================================
    // UPDATE / DELETE
    // =========================================

    LeadResponseDTO updateLead(Long id, LeadRequestDTO request);

    void deleteLead(Long id);


    // =========================================
    // ASSIGNMENT
    // =========================================

    LeadResponseDTO assignLead(Long leadId, Long assignedToId);


    // =========================================
    // MY LEADS
    // =========================================

    // Existing method
    List<LeadResponseDTO> getMyLeads(Long userId);

    // New paginated method
    Page<LeadResponseDTO> getMyLeads(
            Long userId,
            Pageable pageable
    );


    // =========================================
    // SALES USER LEADS
    // =========================================

    // Existing method
    List<LeadResponseDTO> getLeadsBySalesUser(Long userId);

    // New paginated method
    Page<LeadResponseDTO> getLeadsBySalesUser(
            Long userId,
            Pageable pageable
    );


    // =========================================
    // TEAM LEADS
    // =========================================

    // Existing method
    List<LeadResponseDTO> getTeamLeads(Long supervisorId);

    // New paginated method
    Page<LeadResponseDTO> getTeamLeads(
            Long supervisorId,
            Pageable pageable
    );


    // =========================================
    // RAW ENQUIRIES
    // =========================================

    // Existing method
    List<LeadResponseDTO> getRawLeads();

    // New paginated method
    Page<LeadResponseDTO> getRawLeads(Pageable pageable);


    // =========================================
    // RAW ENQUIRIES BY IMPORT STATUS
    // =========================================

    // Existing method
    List<LeadResponseDTO> getRawLeadsByImportStatus(
            LeadImportStatus importStatus
    );

    // New paginated method
    Page<LeadResponseDTO> getRawLeadsByImportStatus(
            LeadImportStatus importStatus,
            Pageable pageable
    );


    // =========================================
    // RAW ENQUIRY COUNT
    // =========================================

    long countRawLeads();
}
