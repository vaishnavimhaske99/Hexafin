
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadStatus;
import com.example.demo.entity.LeadImportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface LeadRepository extends JpaRepository<Lead, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
    List<Lead> findByImportBatch_Id(Long batchId);
    Page<Lead> findByImportBatch_Id(Long batchId, Pageable pageable);
 // Count Leads in a Batch
 long countByImportBatch_Id(Long batchId);
    // Assigned Leads
    List<Lead> findByAssignedTo_Id(Long assignedToId);

    // Dashboard counts
    long countByStatus(LeadStatus status);

    long countByAssignedToIsNotNull();

    long countByAssignedToIsNull();

    long countByAssignedTo_Id(Long userId);

    long countByAssignedTo_IdAndStatus(Long userId, LeadStatus status);

    // Raw Lead queries
    List<Lead> findByRawTrue();

    List<Lead> findByRawTrueAndImportStatus(LeadImportStatus importStatus);

    long countByRawTrue();
}
