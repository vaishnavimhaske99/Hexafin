package com.example.demo.controller;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.LeadDocumentResponseDTO;
import com.example.demo.service.LeadDocumentService;

@RestController
@RequestMapping("/api")
public class LeadDocumentController {

    private final LeadDocumentService leadDocumentService;

    public LeadDocumentController(
            LeadDocumentService leadDocumentService) {

        this.leadDocumentService = leadDocumentService;
    }

    // Upload document
    @PostMapping(
            value = "/leads/{leadId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public LeadDocumentResponseDTO uploadDocument(
            @PathVariable Long leadId,
            @RequestParam("file") MultipartFile file) {

        return leadDocumentService.uploadDocument(
                leadId,
                file);
    }

    // Get all documents of a lead
    @GetMapping("/leads/{leadId}/documents")
    public List<LeadDocumentResponseDTO> getDocumentsByLeadId(
            @PathVariable Long leadId) {

        return leadDocumentService
                .getDocumentsByLeadId(leadId);
    }

    // Get document details
    @GetMapping("/documents/{documentId}")
    public LeadDocumentResponseDTO getDocumentById(
            @PathVariable Long documentId) {

        return leadDocumentService
                .getDocumentById(documentId);
    }

    // Download document
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<ByteArrayResource> downloadDocument(
            @PathVariable Long documentId) {

        LeadDocumentResponseDTO document =
                leadDocumentService
                        .getDocumentById(documentId);

        byte[] fileData =
                leadDocumentService
                        .downloadDocument(documentId);

        ByteArrayResource resource =
                new ByteArrayResource(fileData);

        MediaType mediaType =
                MediaType.APPLICATION_OCTET_STREAM;

        if (document.getFileType() != null) {
            try {
                mediaType =
                        MediaType.parseMediaType(
                                document.getFileType());
            } catch (Exception ignored) {
                // Keep default binary media type
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                document.getOriginalFileName() +
                                "\"")
                .contentLength(fileData.length)
                .body(resource);
    }

    // Delete document
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long documentId) {

        leadDocumentService
                .deleteDocument(documentId);

        return ResponseEntity.noContent().build();
    }
}