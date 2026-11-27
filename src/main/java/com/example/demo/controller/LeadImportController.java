
package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.LeadImportPreviewDTO;
import com.example.demo.dto.LeadImportResultDTO;
import com.example.demo.service.SmartLeadImportService;

@RestController
@RequestMapping("/api/leads/import")
public class LeadImportController {

    private final SmartLeadImportService smartLeadImportService;

    public LeadImportController(
            SmartLeadImportService smartLeadImportService) {
        this.smartLeadImportService = smartLeadImportService;
    }

    // SMART IMPORT PREVIEW
    @PostMapping(
        value = "/preview",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<LeadImportPreviewDTO> previewImport(
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(
            smartLeadImportService.previewImport(file)
        );
    }

    // SMART IMPORT SAVE RAW
    @PostMapping(
        value = "/save-raw",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<LeadImportResultDTO> saveRawLeads(
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(
            smartLeadImportService.saveImport(file)
        );
    }
}
