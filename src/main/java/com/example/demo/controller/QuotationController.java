
package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.QuotationRequestDTO;
import com.example.demo.dto.QuotationResponseDTO;
import com.example.demo.service.QuotationService;

@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    // Create quotation for a Lead
    @PostMapping("/lead/{leadId}")
    public QuotationResponseDTO createQuotation(
            @PathVariable Long leadId,
            @RequestBody QuotationRequestDTO request) {

        return quotationService.createQuotation(leadId, request);
    }

    // Get all quotations of a Lead
    @GetMapping("/lead/{leadId}")
    public List<QuotationResponseDTO> getQuotationsByLeadId(
            @PathVariable Long leadId) {

        return quotationService.getQuotationsByLeadId(leadId);
    }

    // Create quotation for a Customer
    @PostMapping("/customer/{customerId}")
    public QuotationResponseDTO createCustomerQuotation(
            @PathVariable Long customerId) {

        return quotationService.createCustomerQuotation(customerId);
    }

    // Get all quotations of a Customer
    @GetMapping("/customer/{customerId}")
    public List<QuotationResponseDTO> getQuotationsByCustomerId(
            @PathVariable Long customerId) {

        return quotationService.getQuotationsByCustomerId(customerId);
    }

    // Generate Customer quotation PDF
    @PostMapping("/{quotationId}/customer/pdf")
    public QuotationResponseDTO generateCustomerQuotationPdf(
            @PathVariable Long quotationId) {

        return quotationService.generateCustomerQuotationPdf(
                quotationId);
    }

    // Download Customer quotation PDF
    @GetMapping("/{quotationId}/customer/pdf")
    public ResponseEntity<Resource> downloadCustomerQuotationPdf(
            @PathVariable Long quotationId) {

        QuotationResponseDTO quotation =
                quotationService.getQuotationById(quotationId);

        if (quotation.getPdfFilePath() == null
                || quotation.getPdfFilePath().isBlank()) {

            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(quotation.getPdfFilePath())
                    .toAbsolutePath()
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"quotation-"
                                    + quotationId + ".pdf\"")
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to download quotation PDF", e);
        }
    }

    // Get quotation by ID
    @GetMapping("/{quotationId}")
    public QuotationResponseDTO getQuotationById(
            @PathVariable Long quotationId) {

        return quotationService.getQuotationById(quotationId);
    }

    // Update quotation
    @PutMapping("/{quotationId}")
    public QuotationResponseDTO updateQuotation(
            @PathVariable Long quotationId,
            @RequestBody QuotationRequestDTO request) {

        return quotationService.updateQuotation(
                quotationId, request);
    }

    // Delete quotation
    @DeleteMapping("/{quotationId}")
    public void deleteQuotation(@PathVariable Long quotationId) {
        quotationService.deleteQuotation(quotationId);
    }
}
