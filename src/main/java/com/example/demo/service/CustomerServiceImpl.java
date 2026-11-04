package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.CustomerRequestDTO;
import com.example.demo.dto.CustomerResponseDTO;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.LeadRepository;
import com.example.demo.repository.UserRepository;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    // ✅ NEW — file storage directory
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            LeadRepository leadRepository,
            UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
    }

    // ═══════════════════════════════════════════════════════
    // ✅ CONVERT LEAD TO CUSTOMER (UNCHANGED)
    // ═══════════════════════════════════════════════════════
    @Override
    @Transactional
    public CustomerResponseDTO convertLeadToCustomer(Long leadId, CustomerRequestDTO request) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found: " + leadId));

        if (customerRepository.existsBySourceLeadId(leadId)) {
            throw new RuntimeException("Lead already converted to customer.");
        }

        Customer customer = new Customer();

        // Personal Info
        customer.setFirstName(request.getFirstName() != null ? request.getFirstName() : lead.getFirstName());
        customer.setLastName(request.getLastName() != null ? request.getLastName() : lead.getLastName());
        customer.setEmail(request.getEmail() != null ? request.getEmail() : lead.getEmail());
        customer.setPhone(request.getPhone() != null ? request.getPhone() : lead.getPhone());
        customer.setAlternatePhone(request.getAlternatePhone());

        // KYC
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setGender(request.getGender());
        customer.setMaritalStatus(request.getMaritalStatus());
        customer.setPanNumber(request.getPanNumber());
        customer.setAadhaarNumber(request.getAadhaarNumber());
        customer.setPhotoUrl(request.getPhotoUrl());
        customer.setPermanentAddress(request.getPermanentAddress());

        // Address
        customer.setAddressLine1(request.getAddressLine1());
        customer.setAddressLine2(request.getAddressLine2());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setPincode(request.getPincode());

        // Employment
        customer.setEmploymentType(request.getEmploymentType());
        customer.setCompanyName(request.getCompanyName());
        customer.setDesignation(request.getDesignation());
        customer.setExperienceYears(request.getExperienceYears());
        customer.setItrFiled(request.getItrFiled());

        // Loan
        customer.setLoanType(request.getLoanType());
        customer.setLoanPurpose(request.getLoanPurpose());
        customer.setLoanAmount(request.getLoanAmount());
        customer.setTenureMonths(request.getTenureMonths());
        customer.setInterestRate(request.getInterestRate());

        // Financial
        customer.setMonthlyIncome(request.getMonthlyIncome());
        customer.setPropertyValue(request.getPropertyValue());
        customer.setExistingEmi(request.getExistingEmi());
        customer.setCibilScore(request.getCibilScore());
        customer.setOtherIncome(request.getOtherIncome());
        customer.setExpectedEmi(request.getExpectedEmi());
        customer.setDownPayment(request.getDownPayment());

        double monthly = safeDouble(request.getMonthlyIncome());
        double other = safeDouble(request.getOtherIncome());
        customer.setAnnualIncome((monthly + other) * 12);

        // Previous Loan
        customer.setPreviousLoanTaken(request.getPreviousLoanTaken());
        customer.setPreviousLoanType(request.getPreviousLoanType());
        customer.setPreviousLoanLender(request.getPreviousLoanLender());
        customer.setPreviousLoanAmount(request.getPreviousLoanAmount());
        customer.setPreviousLoanEmi(request.getPreviousLoanEmi());
        customer.setPreviousLoanTenure(request.getPreviousLoanTenure());
        customer.setPreviousLoanReason(request.getPreviousLoanReason());
        customer.setPreviousLoanStatus(request.getPreviousLoanStatus());
        customer.setPreviousRepaymentHistory(request.getPreviousRepaymentHistory());

        // Requirement
        customer.setUrgency(request.getUrgency());

        // Documents (booleans)
        customer.setDocPanCard(request.getDocPanCard());
        customer.setDocAadhaarCard(request.getDocAadhaarCard());
        customer.setDocSalarySlips(request.getDocSalarySlips());
        customer.setDocBankStatement(request.getDocBankStatement());
        customer.setDocItr(request.getDocItr());
        customer.setDocPropertyPapers(request.getDocPropertyPapers());
        customer.setDocPhoto(request.getDocPhoto());

        // Bank
        customer.setBankName(request.getBankName());
        customer.setAccountNumber(request.getAccountNumber());
        customer.setLoanStatus(request.getLoanStatus() != null ? request.getLoanStatus() : "PENDING");

        // Consultation
        customer.setLeadSource(request.getLeadSource());
        customer.setNotes(request.getNotes());
        customer.setNextFollowUp(request.getNextFollowUp());

        // Lead reference
        customer.setSourceLead(lead);

        // Logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User loggedIn = (User) auth.getPrincipal();
            customer.setCreatedBy(loggedIn);
            System.out.println("=== CUSTOMER CREATED BY ===");
            System.out.println("User ID: " + loggedIn.getId());
            System.out.println("User Name: " + loggedIn.getFirstName() + " " + loggedIn.getLastName());
            System.out.println("User Role: " + loggedIn.getRole());
        }

        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        Customer saved = customerRepository.save(customer);

        lead.setStatus(LeadStatus.WON);
        lead.setUpdatedAt(now);
        leadRepository.save(lead);

        return toResponseDTO(saved);
    }

    // ═══════════════════════════════════════════════════════
    // ✅ UPDATE CUSTOMER (UNCHANGED)
    // ═══════════════════════════════════════════════════════
    @Override
    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        User loggedInUser = getLoggedInUser();
        UserRole role = loggedInUser.getRole();
        Long myUserId = loggedInUser.getId();

        if (role != UserRole.ADMIN) {
            Long creatorId = customer.getCreatedBy() != null ? customer.getCreatedBy().getId() : null;

            if (creatorId == null) {
                throw new RuntimeException("You do not have permission to update this customer.");
            }

            if (role == UserRole.SALES && !creatorId.equals(myUserId)) {
                throw new RuntimeException("You can only update your own customers.");
            }

            if (role == UserRole.TEAM_LEAD) {
                boolean allowed = creatorId.equals(myUserId);
                if (!allowed) {
                    List<User> team = userRepository.findByReportsTo_Id(myUserId);
                    allowed = team.stream().anyMatch(u -> u.getId().equals(creatorId));
                }
                if (!allowed) throw new RuntimeException("You can only update your team's customers.");
            }

            if (role == UserRole.MANAGER) {
                List<Long> allowedIds = new ArrayList<>();
                allowedIds.add(myUserId);
                List<User> directs = userRepository.findByReportsTo_Id(myUserId);
                for (User dr : directs) {
                    allowedIds.add(dr.getId());
                    List<User> subs = userRepository.findByReportsTo_Id(dr.getId());
                    for (User sr : subs) allowedIds.add(sr.getId());
                }
                if (!allowedIds.contains(creatorId)) {
                    throw new RuntimeException("You can only update customers within your team.");
                }
            }
        }

        if (request.getFirstName() != null) customer.setFirstName(request.getFirstName());
        if (request.getLastName() != null) customer.setLastName(request.getLastName());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        customer.setAlternatePhone(request.getAlternatePhone());

        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setGender(request.getGender());
        customer.setMaritalStatus(request.getMaritalStatus());
        customer.setPanNumber(request.getPanNumber());
        customer.setAadhaarNumber(request.getAadhaarNumber());
        customer.setPhotoUrl(request.getPhotoUrl());
        customer.setPermanentAddress(request.getPermanentAddress());

        customer.setAddressLine1(request.getAddressLine1());
        customer.setAddressLine2(request.getAddressLine2());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setPincode(request.getPincode());

        customer.setEmploymentType(request.getEmploymentType());
        customer.setCompanyName(request.getCompanyName());
        customer.setDesignation(request.getDesignation());
        customer.setExperienceYears(request.getExperienceYears());
        customer.setItrFiled(request.getItrFiled());

        customer.setLoanType(request.getLoanType());
        customer.setLoanPurpose(request.getLoanPurpose());
        customer.setLoanAmount(request.getLoanAmount());
        customer.setTenureMonths(request.getTenureMonths());
        customer.setInterestRate(request.getInterestRate());

        customer.setMonthlyIncome(request.getMonthlyIncome());
        customer.setPropertyValue(request.getPropertyValue());
        customer.setExistingEmi(request.getExistingEmi());
        customer.setCibilScore(request.getCibilScore());
        customer.setOtherIncome(request.getOtherIncome());
        customer.setExpectedEmi(request.getExpectedEmi());
        customer.setDownPayment(request.getDownPayment());

        double monthly = safeDouble(request.getMonthlyIncome());
        double other = safeDouble(request.getOtherIncome());
        customer.setAnnualIncome((monthly + other) * 12);

        customer.setPreviousLoanTaken(request.getPreviousLoanTaken());
        customer.setPreviousLoanType(request.getPreviousLoanType());
        customer.setPreviousLoanLender(request.getPreviousLoanLender());
        customer.setPreviousLoanAmount(request.getPreviousLoanAmount());
        customer.setPreviousLoanEmi(request.getPreviousLoanEmi());
        customer.setPreviousLoanTenure(request.getPreviousLoanTenure());
        customer.setPreviousLoanReason(request.getPreviousLoanReason());
        customer.setPreviousLoanStatus(request.getPreviousLoanStatus());
        customer.setPreviousRepaymentHistory(request.getPreviousRepaymentHistory());

        customer.setUrgency(request.getUrgency());

        customer.setDocPanCard(request.getDocPanCard());
        customer.setDocAadhaarCard(request.getDocAadhaarCard());
        customer.setDocSalarySlips(request.getDocSalarySlips());
        customer.setDocBankStatement(request.getDocBankStatement());
        customer.setDocItr(request.getDocItr());
        customer.setDocPropertyPapers(request.getDocPropertyPapers());
        customer.setDocPhoto(request.getDocPhoto());

        customer.setBankName(request.getBankName());
        customer.setAccountNumber(request.getAccountNumber());
        if (request.getLoanStatus() != null) customer.setLoanStatus(request.getLoanStatus());

        customer.setLeadSource(request.getLeadSource());
        customer.setNotes(request.getNotes());
        customer.setNextFollowUp(request.getNextFollowUp());

        customer.setUpdatedAt(LocalDateTime.now());

        Customer saved = customerRepository.save(customer);
        return toResponseDTO(saved);
    }

    // ═══════════════════════════════════════════════════════
    // ✅ UPLOAD DOCUMENT (NEW)
    // ═══════════════════════════════════════════════════════
    @Override
    @Transactional
    public CustomerResponseDTO uploadDocument(Long id, String docType, MultipartFile file) throws IOException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        checkDocumentPermission(customer);

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("File exceeds 10MB limit");
        }

        // Old file delete (if exists) — overwrite strategy
        String oldPath = getDocPath(docType, customer);
        if (oldPath != null && !oldPath.isBlank()) {
            try {
                Files.deleteIfExists(Paths.get(oldPath));
            } catch (IOException ignored) {
                // file already missing — ignore
            }
        }

        // Save new file
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String safe = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String ext = "";
        int dot = safe.lastIndexOf('.');
        if (dot >= 0) ext = safe.substring(dot);

        String storedName = docType + "_" + System.currentTimeMillis() + ext;

        Path dir = Paths.get(uploadDir, "customers", String.valueOf(id));
        Files.createDirectories(dir);

        Path target = dir.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Update entity
        setDocPath(docType, customer, target.toString());
        setDocFlag(docType, customer, true);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        return toResponseDTO(customer);
    }

    // ═══════════════════════════════════════════════════════
    // ✅ DOWNLOAD DOCUMENT (NEW)
    // ═══════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long id, String docType) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        checkDocumentPermission(customer);

        String path = getDocPath(docType, customer);
        if (path == null || path.isBlank()) {
            throw new RuntimeException("Document not uploaded for type: " + docType);
        }
        if (!Files.exists(Paths.get(path))) {
            throw new RuntimeException("File missing on disk: " + path);
        }

        return new FileSystemResource(path);
    }

    // ═══════════════════════════════════════════════════════
    // ✅ DELETE DOCUMENT (NEW)
    // ═══════════════════════════════════════════════════════
    @Override
    @Transactional
    public CustomerResponseDTO deleteDocument(Long id, String docType) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        checkDocumentPermission(customer);

        String path = getDocPath(docType, customer);
        if (path != null && !path.isBlank()) {
            try {
                Files.deleteIfExists(Paths.get(path));
            } catch (IOException ignored) {
                // ignore
            }
        }

        setDocPath(docType, customer, null);
        setDocFlag(docType, customer, false);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        return toResponseDTO(customer);
    }

    // ═══════════════════════════════════════════════════════
    // ✅ ROLE-BASED CUSTOMER LIST (UNCHANGED)
    // ═══════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getAllCustomers() {
        User loggedInUser = getLoggedInUser();
        UserRole role = loggedInUser.getRole();
        Long myUserId = loggedInUser.getId();

        System.out.println("\n========== DEBUG START ==========");
        System.out.println("Logged In User: " + loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
        System.out.println("Logged In User ID: " + myUserId);
        System.out.println("Logged In User Role: " + role);

        List<Customer> customers;

        if (role == UserRole.ADMIN) {
            System.out.println("Access: ADMIN — showing ALL customers");
            customers = customerRepository.findAll();
        }
        else if (role == UserRole.SALES) {
            System.out.println("Access: SALES — showing customers created by: " + myUserId);
            customers = customerRepository.findByCreatedById(myUserId);
        }
        else if (role == UserRole.TEAM_LEAD) {
            List<Long> allowedUserIds = new ArrayList<>();
            allowedUserIds.add(myUserId);

            List<User> salesUnderMe = userRepository.findByReportsTo_Id(myUserId);
            for (User u : salesUnderMe) {
                if (u.getRole() == UserRole.SALES) {
                    allowedUserIds.add(u.getId());
                }
            }
            System.out.println("Access: TEAM_LEAD — showing customers created by: " + allowedUserIds);
            customers = customerRepository.findByCreatedByIdIn(allowedUserIds);
        }
        else if (role == UserRole.MANAGER) {
            List<Long> allowedUserIds = new ArrayList<>();
            allowedUserIds.add(myUserId);

            List<User> directReports = userRepository.findByReportsTo_Id(myUserId);
            for (User dr : directReports) {
                allowedUserIds.add(dr.getId());
                List<User> subReports = userRepository.findByReportsTo_Id(dr.getId());
                for (User sr : subReports) {
                    allowedUserIds.add(sr.getId());
                }
            }
            System.out.println("Access: MANAGER — showing customers created by: " + allowedUserIds);
            customers = customerRepository.findByCreatedByIdIn(allowedUserIds);
        }
        else {
            customers = new ArrayList<>();
        }

        System.out.println("Total customers returned: " + customers.size());
        for (Customer c : customers) {
            Long createdById = c.getCreatedBy() != null ? c.getCreatedBy().getId() : null;
            System.out.println("  - Customer #" + c.getId() + " '" + c.getFirstName() + "' created by ID: " + createdById);
        }
        System.out.println("========== DEBUG END ==========\n");

        return customers.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ═══════════════════════════════════════════════════════
    // ✅ GET SINGLE CUSTOMER (UNCHANGED)
    // ═══════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        User loggedInUser = getLoggedInUser();
        UserRole role = loggedInUser.getRole();
        Long myUserId = loggedInUser.getId();

        if (role != UserRole.ADMIN) {
            Long customerCreatorId = customer.getCreatedBy() != null
                    ? customer.getCreatedBy().getId()
                    : null;

            if (customerCreatorId == null) {
                throw new RuntimeException("You do not have permission to view this customer.");
            }

            if (role == UserRole.SALES) {
                if (!customerCreatorId.equals(myUserId)) {
                    throw new RuntimeException("You can only view your own customers.");
                }
            }
            else if (role == UserRole.TEAM_LEAD) {
                boolean allowed = customerCreatorId.equals(myUserId);
                if (!allowed) {
                    List<User> team = userRepository.findByReportsTo_Id(myUserId);
                    allowed = team.stream().anyMatch(u -> u.getId().equals(customerCreatorId));
                }
                if (!allowed) {
                    throw new RuntimeException("You can only view your team's customers.");
                }
            }
            else if (role == UserRole.MANAGER) {
                List<Long> allowedIds = new ArrayList<>();
                allowedIds.add(myUserId);
                List<User> directs = userRepository.findByReportsTo_Id(myUserId);
                for (User dr : directs) {
                    allowedIds.add(dr.getId());
                    List<User> subs = userRepository.findByReportsTo_Id(dr.getId());
                    for (User sr : subs) {
                        allowedIds.add(sr.getId());
                    }
                }
                if (!allowedIds.contains(customerCreatorId)) {
                    throw new RuntimeException("You can only view customers within your team.");
                }
            }
        }

        return toResponseDTO(customer);
    }

    // ───── Helpers ─────
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        throw new IllegalStateException("Authenticated user not found.");
    }

    private double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    // ═══════════════════════════════════════════════════════
    // ✅ DOCUMENT HELPERS (NEW)
    // ═══════════════════════════════════════════════════════

    private String getDocPath(String docType, Customer c) {
        return switch (docType) {
            case "PAN_CARD" -> c.getPanCardUrl();
            case "AADHAAR_CARD" -> c.getAadhaarCardUrl();
            case "SALARY_SLIPS" -> c.getSalarySlipsUrl();
            case "BANK_STATEMENT" -> c.getBankStatementUrl();
            case "ITR" -> c.getItrUrl();
            case "PROPERTY_PAPERS" -> c.getPropertyPapersUrl();
            case "PHOTO" -> c.getPhotoDocUrl();
            default -> throw new RuntimeException("Invalid docType: " + docType);
        };
    }

    private void setDocPath(String docType, Customer c, String path) {
        switch (docType) {
            case "PAN_CARD" -> c.setPanCardUrl(path);
            case "AADHAAR_CARD" -> c.setAadhaarCardUrl(path);
            case "SALARY_SLIPS" -> c.setSalarySlipsUrl(path);
            case "BANK_STATEMENT" -> c.setBankStatementUrl(path);
            case "ITR" -> c.setItrUrl(path);
            case "PROPERTY_PAPERS" -> c.setPropertyPapersUrl(path);
            case "PHOTO" -> c.setPhotoDocUrl(path);
            default -> throw new RuntimeException("Invalid docType: " + docType);
        }
    }

    private void setDocFlag(String docType, Customer c, boolean value) {
        switch (docType) {
            case "PAN_CARD" -> c.setDocPanCard(value);
            case "AADHAAR_CARD" -> c.setDocAadhaarCard(value);
            case "SALARY_SLIPS" -> c.setDocSalarySlips(value);
            case "BANK_STATEMENT" -> c.setDocBankStatement(value);
            case "ITR" -> c.setDocItr(value);
            case "PROPERTY_PAPERS" -> c.setDocPropertyPapers(value);
            case "PHOTO" -> c.setDocPhoto(value);
        }
    }

    private void checkDocumentPermission(Customer customer) {
        User me = getLoggedInUser();
        if (me.getRole() == UserRole.ADMIN) return;

        Long creatorId = customer.getCreatedBy() != null ? customer.getCreatedBy().getId() : null;
        if (creatorId == null) {
            throw new RuntimeException("You do not have permission for this customer.");
        }

        if (me.getRole() == UserRole.SALES && !creatorId.equals(me.getId())) {
            throw new RuntimeException("You can only access your own customers.");
        }

        if (me.getRole() == UserRole.TEAM_LEAD) {
            boolean allowed = creatorId.equals(me.getId());
            if (!allowed) {
                List<User> team = userRepository.findByReportsTo_Id(me.getId());
                allowed = team.stream().anyMatch(u -> u.getId().equals(creatorId));
            }
            if (!allowed) throw new RuntimeException("You can only access your team's customers.");
        }

        if (me.getRole() == UserRole.MANAGER) {
            List<Long> allowedIds = new ArrayList<>();
            allowedIds.add(me.getId());
            for (User dr : userRepository.findByReportsTo_Id(me.getId())) {
                allowedIds.add(dr.getId());
                for (User sr : userRepository.findByReportsTo_Id(dr.getId())) {
                    allowedIds.add(sr.getId());
                }
            }
            if (!allowedIds.contains(creatorId)) {
                throw new RuntimeException("You can only access customers within your team.");
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // ✅ Entity → Response DTO (updated with URL fields)
    // ═══════════════════════════════════════════════════════
    private CustomerResponseDTO toResponseDTO(Customer c) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(c.getId());

        // ── Personal ──
        dto.setFirstName(c.getFirstName());
        dto.setLastName(c.getLastName());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setAlternatePhone(c.getAlternatePhone());

        // ── Personal KYC ──
        dto.setDateOfBirth(c.getDateOfBirth());
        dto.setGender(c.getGender());
        dto.setMaritalStatus(c.getMaritalStatus());
        dto.setPanNumber(c.getPanNumber());
        dto.setAadhaarNumber(c.getAadhaarNumber());
        dto.setPhotoUrl(c.getPhotoUrl());
        dto.setPermanentAddress(c.getPermanentAddress());

        // ── Address ──
        dto.setAddressLine1(c.getAddressLine1());
        dto.setAddressLine2(c.getAddressLine2());
        dto.setCity(c.getCity());
        dto.setState(c.getState());
        dto.setCountry(c.getCountry());
        dto.setPincode(c.getPincode());

        // ── Employment ──
        dto.setEmploymentType(c.getEmploymentType());
        dto.setCompanyName(c.getCompanyName());
        dto.setDesignation(c.getDesignation());
        dto.setExperienceYears(c.getExperienceYears());
        dto.setItrFiled(c.getItrFiled());

        // ── Loan ──
        dto.setLoanType(c.getLoanType());
        dto.setLoanPurpose(c.getLoanPurpose());
        dto.setLoanAmount(c.getLoanAmount());
        dto.setTenureMonths(c.getTenureMonths());
        dto.setInterestRate(c.getInterestRate());

        // ── Financial ──
        dto.setMonthlyIncome(c.getMonthlyIncome());
        dto.setPropertyValue(c.getPropertyValue());
        dto.setExistingEmi(c.getExistingEmi());
        dto.setCibilScore(c.getCibilScore());
        dto.setOtherIncome(c.getOtherIncome());
        dto.setAnnualIncome(c.getAnnualIncome());
        dto.setExpectedEmi(c.getExpectedEmi());
        dto.setDownPayment(c.getDownPayment());

        // ── Previous Loan ──
        dto.setPreviousLoanTaken(c.getPreviousLoanTaken());
        dto.setPreviousLoanType(c.getPreviousLoanType());
        dto.setPreviousLoanLender(c.getPreviousLoanLender());
        dto.setPreviousLoanAmount(c.getPreviousLoanAmount());
        dto.setPreviousLoanEmi(c.getPreviousLoanEmi());
        dto.setPreviousLoanTenure(c.getPreviousLoanTenure());
        dto.setPreviousLoanReason(c.getPreviousLoanReason());
        dto.setPreviousLoanStatus(c.getPreviousLoanStatus());
        dto.setPreviousRepaymentHistory(c.getPreviousRepaymentHistory());

        // ── Current Requirement ──
        dto.setUrgency(c.getUrgency());

        // ── Documents (booleans) ──
        dto.setDocPanCard(c.getDocPanCard());
        dto.setDocAadhaarCard(c.getDocAadhaarCard());
        dto.setDocSalarySlips(c.getDocSalarySlips());
        dto.setDocBankStatement(c.getDocBankStatement());
        dto.setDocItr(c.getDocItr());
        dto.setDocPropertyPapers(c.getDocPropertyPapers());
        dto.setDocPhoto(c.getDocPhoto());

        // ── Document URLs (NEW) ──
        dto.setPanCardUrl(c.getPanCardUrl());
        dto.setAadhaarCardUrl(c.getAadhaarCardUrl());
        dto.setSalarySlipsUrl(c.getSalarySlipsUrl());
        dto.setBankStatementUrl(c.getBankStatementUrl());
        dto.setItrUrl(c.getItrUrl());
        dto.setPropertyPapersUrl(c.getPropertyPapersUrl());
        dto.setPhotoDocUrl(c.getPhotoDocUrl());

        // ── Bank ──
        dto.setBankName(c.getBankName());
        dto.setAccountNumber(c.getAccountNumber());
        dto.setLoanStatus(c.getLoanStatus());

        // ── Consultation ──
        dto.setLeadSource(c.getLeadSource());
        dto.setNotes(c.getNotes());
        dto.setNextFollowUp(c.getNextFollowUp());

        // ── Source Lead ──
        if (c.getSourceLead() != null) {
            dto.setSourceLeadId(c.getSourceLead().getId());
            dto.setSourceLeadName(
                    (c.getSourceLead().getFirstName() != null ? c.getSourceLead().getFirstName() : "") + " " +
                    (c.getSourceLead().getLastName() != null ? c.getSourceLead().getLastName() : "")
            );
        }

        // ── Created By ──
        if (c.getCreatedBy() != null) {
            dto.setCreatedById(c.getCreatedBy().getId());
            dto.setCreatedByName(
                    (c.getCreatedBy().getFirstName() != null ? c.getCreatedBy().getFirstName() : "") + " " +
                    (c.getCreatedBy().getLastName() != null ? c.getCreatedBy().getLastName() : "")
            );
        }

        // ── Timestamps ──
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        return dto;
    }
}