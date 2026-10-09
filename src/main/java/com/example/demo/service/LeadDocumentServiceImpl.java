package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.LeadDocumentResponseDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadDocument;
import com.example.demo.mapper.LeadDocumentMapper;
import com.example.demo.repository.LeadDocumentRepository;
import com.example.demo.repository.LeadRepository;

@Service
public class LeadDocumentServiceImpl implements LeadDocumentService {

    private final LeadDocumentRepository leadDocumentRepository;
    private final LeadRepository leadRepository;

    private final Path uploadDirectory =
            Paths.get("uploads/leads");

    public LeadDocumentServiceImpl(
            LeadDocumentRepository leadDocumentRepository,
            LeadRepository leadRepository) {

        this.leadDocumentRepository = leadDocumentRepository;
        this.leadRepository = leadRepository;
    }

    @Override
    public LeadDocumentResponseDTO uploadDocument(
            Long leadId,
            MultipartFile file) {

        // 1. Check Lead
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lead not found with id: " + leadId));

        // 2. Check file
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }

        try {

            // 3. Create Lead folder
            Path leadDirectory =
                    uploadDirectory.resolve(String.valueOf(leadId));

            Files.createDirectories(leadDirectory);

            // 4. Generate unique file name
            String originalFileName =
                    file.getOriginalFilename();

            String fileExtension = "";

            if (originalFileName != null
                    && originalFileName.contains(".")) {

                fileExtension =
                        originalFileName.substring(
                                originalFileName.lastIndexOf("."));
            }

            String uniqueFileName =
                    UUID.randomUUID() + fileExtension;

            // 5. Complete file path
            Path filePath =
                    leadDirectory.resolve(uniqueFileName);

            // 6. Save file
            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING);

            // 7. Save metadata in database
            LeadDocument document =
                    new LeadDocument();

            document.setLead(lead);
            document.setFileName(uniqueFileName);
            document.setOriginalFileName(
                    originalFileName);
            document.setFileType(
                    file.getContentType());
            document.setFileSize(
                    file.getSize());
            document.setFilePath(
                    filePath.toString());
            document.setUploadedAt(
                    LocalDateTime.now());

            LeadDocument savedDocument =
                    leadDocumentRepository.save(document);

            // 8. Return DTO
            return LeadDocumentMapper
                    .toResponseDTO(savedDocument);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to upload document", e);
        }
    }

    @Override
    public List<LeadDocumentResponseDTO> getDocumentsByLeadId(
            Long leadId) {

        if (!leadRepository.existsById(leadId)) {
            throw new RuntimeException(
                    "Lead not found with id: " + leadId);
        }

        return leadDocumentRepository
                .findByLeadIdOrderByUploadedAtDesc(leadId)
                .stream()
                .map(LeadDocumentMapper::toResponseDTO)
                .toList();
    }

    @Override
    public LeadDocumentResponseDTO getDocumentById(
            Long documentId) {

        LeadDocument document =
                leadDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found with id: "
                                                + documentId));

        return LeadDocumentMapper
                .toResponseDTO(document);
    }

    @Override
    public byte[] downloadDocument(
            Long documentId) {

        LeadDocument document =
                leadDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found with id: "
                                                + documentId));

        try {

            Path filePath =
                    Paths.get(document.getFilePath());

            return Files.readAllBytes(filePath);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to download document", e);
        }
    }

    @Override
    public void deleteDocument(
            Long documentId) {

        LeadDocument document =
                leadDocumentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found with id: "
                                                + documentId));

        try {

            // Delete physical file
            Path filePath =
                    Paths.get(document.getFilePath());

            Files.deleteIfExists(filePath);

            // Delete database record
            leadDocumentRepository.delete(document);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to delete document", e);
        }
    }
}