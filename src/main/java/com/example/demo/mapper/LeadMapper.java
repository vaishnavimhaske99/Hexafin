
package com.example.demo.mapper;

import com.example.demo.dto.LeadRequestDTO;
import com.example.demo.dto.LeadResponseDTO;
import com.example.demo.entity.Lead;

public final class LeadMapper {

    private LeadMapper() {
        // Utility class; prevent object creation.
    }

    public static Lead toEntity(LeadRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                "LeadRequestDTO must not be null"
            );
        }

        Lead lead = new Lead();

        // Basic Details
        lead.setFirstName(dto.getFirstName());
        lead.setLastName(dto.getLastName());
        lead.setCompanyName(dto.getCompanyName());
        lead.setEmail(dto.getEmail());
        lead.setPhone(dto.getPhone());

        // Address Details
        lead.setAddressLine1(dto.getAddressLine1());
        lead.setAddressLine2(dto.getAddressLine2());
        lead.setCity(dto.getCity());
        lead.setState(dto.getState());
        lead.setCountry(dto.getCountry());
        lead.setPincode(dto.getPincode());

        // Lead Information
        lead.setSource(dto.getSource());
        lead.setProduct(dto.getProduct());
        lead.setDescription(dto.getDescription());

        return lead;
    }

    public static LeadResponseDTO toResponseDTO(Lead lead) {

        if (lead == null) {
            throw new IllegalArgumentException(
                "Lead must not be null"
            );
        }

        LeadResponseDTO dto = new LeadResponseDTO();

        // ID
        dto.setId(lead.getId());

        // Basic Details
        dto.setFirstName(lead.getFirstName());
        dto.setLastName(lead.getLastName());
        dto.setCompanyName(lead.getCompanyName());
        dto.setEmail(lead.getEmail());
        dto.setPhone(lead.getPhone());

        // Address Details
        dto.setAddressLine1(lead.getAddressLine1());
        dto.setAddressLine2(lead.getAddressLine2());
        dto.setCity(lead.getCity());
        dto.setState(lead.getState());
        dto.setCountry(lead.getCountry());
        dto.setPincode(lead.getPincode());

        // Lead Information
        dto.setSource(lead.getSource());
        dto.setProduct(lead.getProduct());
        dto.setStatus(lead.getStatus());
        dto.setDescription(lead.getDescription());

        // Audit Information
        dto.setCreatedAt(lead.getCreatedAt());
        dto.setUpdatedAt(lead.getUpdatedAt());

        // Assigned Sales User
        if (lead.getAssignedTo() != null) {

            dto.setAssignedToId(lead.getAssignedTo().getId());

            String firstName = lead.getAssignedTo().getFirstName();
            String lastName = lead.getAssignedTo().getLastName();

            String fullName = firstName == null ? "" : firstName;

            if (lastName != null && !lastName.isBlank()) {
                fullName = fullName.isBlank()
                    ? lastName
                    : fullName + " " + lastName;
            }

            dto.setAssignedToName(fullName);
        }

        return dto;
    }
}
