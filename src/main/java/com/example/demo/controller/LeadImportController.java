
package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.LeadImportPreviewDTO;
import com.example.demo.service.LeadImportService;

@RestController
@RequestMapping("/api/leads/import")
public class LeadImportController {

    private final LeadImportService leadImportService;

    public LeadImportController(LeadImportService leadImportService) {
        this.leadImportService = leadImportService;
    }

    // =========================================
    // PREVIEW EXCEL / CSV FILE
    // =========================================
    @PostMapping(
        value = "/preview",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<LeadImportPreviewDTO> previewImport(
            @RequestParam("file") MultipartFile file) {

        LeadImportPreviewDTO preview =
            leadImportService.previewFile(file);

        return ResponseEntity.ok(preview);
    }

    // =========================================
    // SAVE EXCEL / CSV DATA AS RAW LEADS
    // =========================================
    @PostMapping(
        value = "/save-raw",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> saveRawLeads(
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(
            leadImportService.saveRawLeads(file)
        );
    }
}