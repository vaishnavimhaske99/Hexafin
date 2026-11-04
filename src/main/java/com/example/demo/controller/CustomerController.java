package com.example.demo.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.CustomerRequestDTO;
import com.example.demo.dto.CustomerResponseDTO;
import com.example.demo.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/convert/{leadId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public CustomerResponseDTO convertLead(
            @PathVariable Long leadId,
            @RequestBody CustomerRequestDTO request) {
        return customerService.convertLeadToCustomer(leadId, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public CustomerResponseDTO getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public CustomerResponseDTO updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerRequestDTO request) {
        return customerService.updateCustomer(id, request);
    }

    // ═══════════ UPLOAD DOCUMENT ✅ FIXED ═══════════
    @PostMapping("/{id}/documents/{docType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public CustomerResponseDTO uploadDocument(
            @PathVariable Long id,
            @PathVariable String docType,
            @RequestParam("file") MultipartFile file) throws IOException {
        return customerService.uploadDocument(id, docType, file);
    }

    // ═══════════ DOWNLOAD DOCUMENT ═══════════
    @GetMapping("/{id}/documents/{docType}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @PathVariable String docType) {
        Resource file = customerService.downloadDocument(id, docType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    // ═══════════ DELETE DOCUMENT ═══════════
    @DeleteMapping("/{id}/documents/{docType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public CustomerResponseDTO deleteDocument(
            @PathVariable Long id,
            @PathVariable String docType) {
        return customerService.deleteDocument(id, docType);
    }
}