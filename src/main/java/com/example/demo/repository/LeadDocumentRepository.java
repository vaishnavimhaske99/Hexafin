package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.LeadDocument;

public interface LeadDocumentRepository
        extends JpaRepository<LeadDocument, Long> {

    List<LeadDocument> findByLeadIdOrderByUploadedAtDesc(Long leadId);
}