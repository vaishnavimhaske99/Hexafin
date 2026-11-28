
package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.demo.dto.AssignLeadRequestDTO;
import com.example.demo.dto.LeadRequestDTO;
import com.example.demo.dto.LeadResponseDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadImportBatch;
import com.example.demo.entity.LeadImportStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.LeadImportBatchRepository;
import com.example.demo.repository.LeadRepository;
import com.example.demo.service.LeadService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 100;

    private final LeadService leadService;
    private final LeadRepository leadRepository;
    private final LeadImportBatchRepository batchRepository;
    private final ObjectMapper objectMapper;

    public LeadController(
            LeadService leadService,
            LeadRepository leadRepository,
            LeadImportBatchRepository batchRepository,
            ObjectMapper objectMapper) {

        this.leadService = leadService;
        this.leadRepository = leadRepository;
        this.batchRepository = batchRepository;
        this.objectMapper = objectMapper;
    }

    // =========================================
    // PAGINATION VALIDATION
    // =========================================

    private Pageable createPageable(int page, int size) {

        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid page or size. Page must be >= 0 and size must be between 1 and 100."
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );
    }

    // =========================================
    // ASSIGN LEAD - REQUEST BODY
    // =========================================

    @PutMapping("/{leadId}/assign")
    public ResponseEntity<?> assignLead(
            @PathVariable Long leadId,
            @RequestBody AssignLeadRequestDTO request) {

        if (request == null || request.getAssignedToId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assigned user ID is required"
            );
        }

        return ResponseEntity.ok(
                leadService.assignLead(
                        leadId,
                        request.getAssignedToId()
                )
        );
    }

    // =========================================
    // GET LEADS ASSIGNED TO SELECTED USER
    // =========================================

    @GetMapping("/assigned-to/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public Page<LeadResponseDTO> getLeadsAssignedToUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        return leadService.getLeadsByAssignedTo(
                userId,
                createPageable(page, size)
        );
    }

    // =========================================
    // GET LOGGED-IN USER
    // =========================================

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please login first"
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user details are unavailable. Check JWT authentication."
        );
    }

    // =========================================
    // CREATE LEAD
    // =========================================

    @PostMapping
    public LeadResponseDTO createLead(
            @Valid @RequestBody LeadRequestDTO request) {

        return leadService.createLead(request);
    }

    // =========================================
    // GET ALL LEADS - PAGINATED
    // =========================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public Page<LeadResponseDTO> getAllLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        return leadService.getAllLeads(
                createPageable(page, size)
        );
    }

    // =========================================
    // GET LEAD BY ID
    // =========================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD', 'SALES')")
    public LeadResponseDTO getLeadById(@PathVariable Long id) {

        return leadService.getLeadById(id);
    }

    // =========================================
    // UPDATE LEAD
    // =========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES')")
    public LeadResponseDTO updateLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequestDTO request) {

        return leadService.updateLead(id, request);
    }

    // =========================================
    // DELETE LEAD - ADMIN ONLY
    // =========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteLead(@PathVariable Long id) {

        leadService.deleteLead(id);
    }

    // =========================================
    // GET MY LEADS - SALES ONLY
    // =========================================

    @GetMapping("/my-leads")
    @PreAuthorize("hasRole('SALES')")
    public Page<LeadResponseDTO> getMyLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        User user = getLoggedInUser();

        return leadService.getMyLeads(
                user.getId(),
                createPageable(page, size)
        );
    }

    // =========================================
    // GET LEADS BY SALES USER
    // =========================================

    @GetMapping("/sales-user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public Page<LeadResponseDTO> getLeadsBySalesUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        return leadService.getLeadsBySalesUser(
                userId,
                createPageable(page, size)
        );
    }

    // =========================================
    // GET TEAM LEADS
    // =========================================

    @GetMapping("/team-leads")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'MANAGER')")
    public Page<LeadResponseDTO> getTeamLeads(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        if (authentication == null
                || !(authentication.getPrincipal() instanceof User)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user details are unavailable"
            );
        }

        User loggedInUser = (User) authentication.getPrincipal();

        return leadService.getTeamLeads(
                loggedInUser.getId(),
                createPageable(page, size)
        );
    }

    // =========================================
    // ASSIGN / REASSIGN LEAD
    // ADMIN, MANAGER, TEAM_LEAD
    // =========================================

    @PutMapping("/{leadId}/assign/{assignedToId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public LeadResponseDTO assignLead(
            @PathVariable Long leadId,
            @PathVariable Long assignedToId) {

        return leadService.assignLead(leadId, assignedToId);
    }

    // =========================================
    // RAW LEADS - PAGINATED
    // =========================================

    @GetMapping("/raw")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public Page<LeadResponseDTO> getRawLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        return leadService.getRawLeads(
                createPageable(page, size)
        );
    }

    // =========================================
    // RAW LEADS - FILTER BY IMPORT STATUS
    // =========================================

    @GetMapping("/raw/status/{importStatus}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public Page<LeadResponseDTO> getRawLeadsByImportStatus(
            @PathVariable LeadImportStatus importStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        return leadService.getRawLeadsByImportStatus(
                importStatus,
                createPageable(page, size)
        );
    }

    // =========================================
    // RAW LEADS - COUNT
    // =========================================

    @GetMapping("/raw/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public long countRawLeads() {

        return leadService.countRawLeads();
    }

    // =========================================
    // GET BATCH-WISE RAW LEADS WITH PAGINATION
    // =========================================

    @GetMapping("/import-batches/{batchId}/leads")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    public Map<String, Object> getBatchLeads(
            @PathVariable Long batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid page or size"
            );
        }

        LeadImportBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Import batch not found"
                ));

        Pageable pageable = createPageable(page, size);

        Page<Lead> leadPage =
                leadRepository.findByImportBatch_Id(batchId, pageable);

        List<Map<String, Object>> rows = leadPage.getContent()
                .stream()
                .map(lead -> {

                    Map<String, Object> row = new LinkedHashMap<>();

                    row.put("leadId", lead.getId());

                    try {
                        Map<String, Object> rawData =
                                objectMapper.readValue(
                                        lead.getRawData(),
                                        new TypeReference<Map<String, Object>>() {}
                                );

                        row.put("rawData", rawData);

                    } catch (Exception e) {
                        row.put("rawData", new LinkedHashMap<>());
                    }

                    row.put(
                            "assignedToId",
                            lead.getAssignedTo() != null
                                    ? lead.getAssignedTo().getId()
                                    : null
                    );

                    row.put(
                            "assignedToName",
                            lead.getAssignedTo() != null
                                    ? lead.getAssignedTo().getFirstName() + " "
                                            + lead.getAssignedTo().getLastName()
                                    : null
                    );

                    return row;
                })
                .toList();

        List<String> headers;

        try {
            headers = objectMapper.readValue(
                    batch.getHeadersJson(),
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            headers = List.of();
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("batchId", batch.getId());
        response.put("fileName", batch.getFileName());
        response.put("headers", headers);
        response.put("rows", rows);
        response.put("currentPage", leadPage.getNumber());
        response.put("pageSize", leadPage.getSize());
        response.put("totalElements", leadPage.getTotalElements());
        response.put("totalPages", leadPage.getTotalPages());

        return response;
    }
}
