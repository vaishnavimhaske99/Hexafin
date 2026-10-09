package com.example.demo.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.LeadDocumentResponseDTO;

public interface LeadDocumentService {

    LeadDocumentResponseDTO uploadDocument(
            Long leadId,
            MultipartFile file);

    List<LeadDocumentResponseDTO> getDocumentsByLeadId(
            Long leadId);

    LeadDocumentResponseDTO getDocumentById(
            Long documentId);

    byte[] downloadDocument(
            Long documentId);

    void deleteDocument(
            Long documentId);
}