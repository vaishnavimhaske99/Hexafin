package com.example.demo.mapper;

import org.springframework.stereotype.Component;

import com.example.demo.dto.CustomerRequestDTO;
import com.example.demo.dto.CustomerResponseDTO;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Lead;

@Component
public class CustomerMapper {

    /**
     * ✅ Request DTO → Entity
     * (Lead मधून default values घेतो, जिथे request मध्ये नसेल)
     */
    public Customer toEntity(CustomerRequestDTO request, Lead lead) {
        Customer customer = new Customer();

        // ════════════════ Personal Info (EXISTING) ════════════════
        customer.setFirstName(
            request.getFirstName() != null && !request.getFirstName().isBlank()
                ? request.getFirstName()
                : lead.getFirstName()
        );
        customer.setLastName(
            request.getLastName() != null && !request.getLastName().isBlank()
                ? request.getLastName()
                : lead.getLastName()
        );
        customer.setEmail(
            request.getEmail() != null && !request.getEmail().isBlank()
                ? request.getEmail()
                : lead.getEmail()
        );
        customer.setPhone(
            request.getPhone() != null && !request.getPhone().isBlank()
                ? request.getPhone()
                : lead.getPhone()
        );
        customer.setAlternatePhone(request.getAlternatePhone());

        // ════════════════ Personal KYC (NEW) ════════════════
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setGender(request.getGender());
        customer.setMaritalStatus(request.getMaritalStatus());
        customer.setPanNumber(request.getPanNumber());
        customer.setAadhaarNumber(request.getAadhaarNumber());
        customer.setPhotoUrl(request.getPhotoUrl());
        customer.setPermanentAddress(request.getPermanentAddress());

        // ════════════════ Address (EXISTING) ════════════════
        customer.setAddressLine1(request.getAddressLine1());
        customer.setAddressLine2(request.getAddressLine2());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setPincode(request.getPincode());

        // ════════════════ Employment (NEW) ════════════════
        customer.setEmploymentType(request.getEmploymentType());
        customer.setCompanyName(request.getCompanyName());
        customer.setDesignation(request.getDesignation());
        customer.setExperienceYears(request.getExperienceYears());
        customer.setItrFiled(request.getItrFiled());

        // ════════════════ Loan Details (EXISTING) ════════════════
        customer.setLoanType(request.getLoanType());
        customer.setLoanPurpose(request.getLoanPurpose());
        customer.setLoanAmount(request.getLoanAmount());
        customer.setTenureMonths(request.getTenureMonths());
        customer.setInterestRate(request.getInterestRate());

        // ════════════════ Financial (EXISTING) ════════════════
        customer.setMonthlyIncome(request.getMonthlyIncome());
        customer.setPropertyValue(request.getPropertyValue());
        customer.setExistingEmi(request.getExistingEmi());
        customer.setCibilScore(request.getCibilScore());

        // ════════════════ Financial (NEW) ════════════════
        customer.setOtherIncome(request.getOtherIncome());
        customer.setExpectedEmi(request.getExpectedEmi());
        customer.setDownPayment(request.getDownPayment());

        // annualIncome — service madhe calculate hoil; ithe fakt basic fallback
        customer.setAnnualIncome(
            (safeDouble(request.getMonthlyIncome()) + safeDouble(request.getOtherIncome())) * 12
        );

        // ════════════════ Previous Loan (NEW) ════════════════
        customer.setPreviousLoanTaken(request.getPreviousLoanTaken());
        customer.setPreviousLoanType(request.getPreviousLoanType());
        customer.setPreviousLoanLender(request.getPreviousLoanLender());
        customer.setPreviousLoanAmount(request.getPreviousLoanAmount());
        customer.setPreviousLoanEmi(request.getPreviousLoanEmi());
        customer.setPreviousLoanTenure(request.getPreviousLoanTenure());
        customer.setPreviousLoanReason(request.getPreviousLoanReason());
        customer.setPreviousLoanStatus(request.getPreviousLoanStatus());
        customer.setPreviousRepaymentHistory(request.getPreviousRepaymentHistory());

        // ════════════════ Current Requirement (NEW) ════════════════
        customer.setUrgency(request.getUrgency());

        // ════════════════ Documents (NEW) ════════════════
        customer.setDocPanCard(request.getDocPanCard());
        customer.setDocAadhaarCard(request.getDocAadhaarCard());
        customer.setDocSalarySlips(request.getDocSalarySlips());
        customer.setDocBankStatement(request.getDocBankStatement());
        customer.setDocItr(request.getDocItr());
        customer.setDocPropertyPapers(request.getDocPropertyPapers());
        customer.setDocPhoto(request.getDocPhoto());

        // ════════════════ Bank (EXISTING + NEW) ════════════════
        customer.setBankName(request.getBankName());
        customer.setAccountNumber(request.getAccountNumber());
        customer.setLoanStatus(
            request.getLoanStatus() != null && !request.getLoanStatus().isBlank()
                ? request.getLoanStatus()
                : "PENDING"
        );

        // ════════════════ Consultation (NEW) ════════════════
        customer.setLeadSource(request.getLeadSource());
        customer.setNotes(request.getNotes());
        customer.setNextFollowUp(request.getNextFollowUp());

        // ════════════════ Lead reference (EXISTING) ════════════════
        customer.setSourceLead(lead);

        return customer;
    }

    /**
     * ✅ Entity → Response DTO
     */
    public CustomerResponseDTO toResponseDTO(Customer customer) {
        if (customer == null) return null;

        CustomerResponseDTO dto = new CustomerResponseDTO();

        dto.setId(customer.getId());

        // ════════════════ Personal (EXISTING) ════════════════
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAlternatePhone(customer.getAlternatePhone());

        // ════════════════ Personal KYC (NEW) ════════════════
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setGender(customer.getGender());
        dto.setMaritalStatus(customer.getMaritalStatus());
        dto.setPanNumber(customer.getPanNumber());
        dto.setAadhaarNumber(customer.getAadhaarNumber());
        dto.setPhotoUrl(customer.getPhotoUrl());
        dto.setPermanentAddress(customer.getPermanentAddress());

        // ════════════════ Address (EXISTING) ════════════════
        dto.setAddressLine1(customer.getAddressLine1());
        dto.setAddressLine2(customer.getAddressLine2());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setCountry(customer.getCountry());
        dto.setPincode(customer.getPincode());

        // ════════════════ Employment (NEW) ════════════════
        dto.setEmploymentType(customer.getEmploymentType());
        dto.setCompanyName(customer.getCompanyName());
        dto.setDesignation(customer.getDesignation());
        dto.setExperienceYears(customer.getExperienceYears());
        dto.setItrFiled(customer.getItrFiled());

        // ════════════════ Loan (EXISTING) ════════════════
        dto.setLoanType(customer.getLoanType());
        dto.setLoanPurpose(customer.getLoanPurpose());
        dto.setLoanAmount(customer.getLoanAmount());
        dto.setTenureMonths(customer.getTenureMonths());
        dto.setInterestRate(customer.getInterestRate());

        // ════════════════ Financial (EXISTING) ════════════════
        dto.setMonthlyIncome(customer.getMonthlyIncome());
        dto.setPropertyValue(customer.getPropertyValue());
        dto.setExistingEmi(customer.getExistingEmi());
        dto.setCibilScore(customer.getCibilScore());

        // ════════════════ Financial (NEW) ════════════════
        dto.setOtherIncome(customer.getOtherIncome());
        dto.setAnnualIncome(customer.getAnnualIncome());
        dto.setExpectedEmi(customer.getExpectedEmi());
        dto.setDownPayment(customer.getDownPayment());

        // ════════════════ Previous Loan (NEW) ════════════════
        dto.setPreviousLoanTaken(customer.getPreviousLoanTaken());
        dto.setPreviousLoanType(customer.getPreviousLoanType());
        dto.setPreviousLoanLender(customer.getPreviousLoanLender());
        dto.setPreviousLoanAmount(customer.getPreviousLoanAmount());
        dto.setPreviousLoanEmi(customer.getPreviousLoanEmi());
        dto.setPreviousLoanTenure(customer.getPreviousLoanTenure());
        dto.setPreviousLoanReason(customer.getPreviousLoanReason());
        dto.setPreviousLoanStatus(customer.getPreviousLoanStatus());
        dto.setPreviousRepaymentHistory(customer.getPreviousRepaymentHistory());

        // ════════════════ Current Requirement (NEW) ════════════════
        dto.setUrgency(customer.getUrgency());

        // ════════════════ Documents (NEW) ════════════════
        dto.setDocPanCard(customer.getDocPanCard());
        dto.setDocAadhaarCard(customer.getDocAadhaarCard());
        dto.setDocSalarySlips(customer.getDocSalarySlips());
        dto.setDocBankStatement(customer.getDocBankStatement());
        dto.setDocItr(customer.getDocItr());
        dto.setDocPropertyPapers(customer.getDocPropertyPapers());
        dto.setDocPhoto(customer.getDocPhoto());
     // Document URLs
        dto.setPanCardUrl(customer.getPanCardUrl());
        dto.setAadhaarCardUrl(customer.getAadhaarCardUrl());
        dto.setSalarySlipsUrl(customer.getSalarySlipsUrl());
        dto.setBankStatementUrl(customer.getBankStatementUrl());
        dto.setItrUrl(customer.getItrUrl());
        dto.setPropertyPapersUrl(customer.getPropertyPapersUrl());
        dto.setPhotoDocUrl(customer.getPhotoDocUrl());
        // ════════════════ Bank (EXISTING + NEW) ════════════════
        dto.setBankName(customer.getBankName());
        dto.setAccountNumber(customer.getAccountNumber());
        dto.setLoanStatus(customer.getLoanStatus());

        // ════════════════ Consultation (NEW) ════════════════
        dto.setLeadSource(customer.getLeadSource());
        dto.setNotes(customer.getNotes());
        dto.setNextFollowUp(customer.getNextFollowUp());

        // ════════════════ Source Lead (EXISTING) ════════════════
        if (customer.getSourceLead() != null) {
            Lead lead = customer.getSourceLead();
            dto.setSourceLeadId(lead.getId());
            dto.setSourceLeadName(
                (lead.getFirstName() != null ? lead.getFirstName() : "") + " " +
                (lead.getLastName() != null ? lead.getLastName() : "")
            );
        }

        // ════════════════ Created By (EXISTING) ════════════════
        if (customer.getCreatedBy() != null) {
            dto.setCreatedById(customer.getCreatedBy().getId());
            dto.setCreatedByName(
                (customer.getCreatedBy().getFirstName() != null
                    ? customer.getCreatedBy().getFirstName() : "") + " " +
                (customer.getCreatedBy().getLastName() != null
                    ? customer.getCreatedBy().getLastName() : "")
            );
        }

        // ════════════════ Timestamps (EXISTING + updatedAt NEW) ════════════════
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());

        return dto;
    }

    // ════════════════ Helper ════════════════
    private double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }
}