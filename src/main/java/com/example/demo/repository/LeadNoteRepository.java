package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.LeadNote;

public interface LeadNoteRepository extends JpaRepository<LeadNote, Long> {
	 @Query("SELECT n FROM LeadNote n LEFT JOIN FETCH n.createdBy WHERE n.lead.id = :leadId ORDER BY n.createdAt DESC")
    List<LeadNote> findByLeadId(Long leadId);
    
}