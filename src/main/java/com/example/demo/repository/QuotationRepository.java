package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Quotation;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    // Existing Lead quotation method
    List<Quotation> findByLeadId(Long leadId);

    // New Customer quotation method
    List<Quotation> findByCustomerId(Long customerId);
    

    // Existing quotation number lookup
    Optional<Quotation> findByQuotationNumber(String quotationNumber);
}