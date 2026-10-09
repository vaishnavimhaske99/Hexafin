package com.example.demo.mapper;

import com.example.demo.dto.LeadDocumentResponseDTO;
import com.example.demo.entity.LeadDocument;

public class LeadDocumentMapper {

    private LeadDocumentMapper() {
        // Utility class
    }

    public static LeadDocumentResponseDTO toResponseDTO(
            LeadDocument document) {

        LeadDocumentResponseDTO response =
                new LeadDocumentResponseDTO();

        response.setId(document.getId());

        if (document.getLead() != null) {
            response.setLeadId(document.getLead().getId());
        }

        response.setFileName(document.getFileName());
        response.setOriginalFileName(
                document.getOriginalFileName());
        response.setFileType(document.getFileType());
        response.setFileSize(document.getFileSize());
        response.setFilePath(document.getFilePath());
        response.setUploadedAt(document.getUploadedAt());

        return response;
    }
}