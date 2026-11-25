
package com.example.demo.service;

import java.util.List;


import com.example.demo.dto.LeadRequestDTO;
import com.example.demo.dto.LeadResponseDTO;
import com.example.demo.entity.LeadImportStatus;

public interface LeadService {

    // Create a new lead
    LeadResponseDTO createLead(LeadRequestDTO request);

    // Get leads visible to the logged-in user
    List<LeadResponseDTO> getAllLeads();

    // Get a specific lead by ID
    LeadResponseDTO getLeadById(Long id);

    List<LeadResponseDTO> getLeadsByAssignedTo(Long userId);

    // Update an existing lead
    LeadResponseDTO updateLead(Long id, LeadRequestDTO request);

    // Delete a lead
    void deleteLead(Long id);

    // Assign or reassign a lead to a Sales User
    LeadResponseDTO assignLead(Long leadId, Long assignedToId);

    // Get leads assigned to the logged-in Sales User
    List<LeadResponseDTO> getMyLeads(Long userId);

    // Admin: Get leads assigned to a selected Sales User
    List<LeadResponseDTO> getLeadsBySalesUser(Long userId);

    List<LeadResponseDTO> getTeamLeads(Long supervisorId);

    // Raw Lead management
    List<LeadResponseDTO> getRawLeads();

    List<LeadResponseDTO> getRawLeadsByImportStatus(
            LeadImportStatus importStatus);

    long countRawLeads();
}
