package com.example.demo.mapper;

import com.example.demo.dto.LeadStatusHistoryResponseDTO;
import com.example.demo.entity.LeadStatusHistory;
import com.example.demo.entity.User;

public class LeadStatusHistoryMapper {

    public static LeadStatusHistoryResponseDTO toResponseDTO(LeadStatusHistory history) {

        LeadStatusHistoryResponseDTO dto = new LeadStatusHistoryResponseDTO();

        dto.setId(history.getId());

        if (history.getLead() != null) {
            dto.setLeadId(history.getLead().getId());
        }

        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setChangedAt(history.getChangedAt());

        // ✅ कोणी बदललं
        if (history.getChangedBy() != null) {
            User changedBy = history.getChangedBy();

            dto.setChangedById(changedBy.getId());

            String firstName = changedBy.getFirstName();
            String lastName = changedBy.getLastName();

            String fullName = firstName;

            if (lastName != null && !lastName.isBlank()) {
                fullName = firstName + " " + lastName;
            }

            dto.setChangedByName(fullName);
        }

        return dto;
    }
}