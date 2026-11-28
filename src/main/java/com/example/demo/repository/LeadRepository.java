
package com.example.demo.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadStatus;
import com.example.demo.entity.LeadImportStatus;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    // =========================================
    // DUPLICATE CHECKS
    // =========================================

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);


    // =========================================
    // IMPORT BATCH QUERIES
    // =========================================

    List<Lead> findByImportBatch_Id(Long batchId);

    Page<Lead> findByImportBatch_Id(Long batchId, Pageable pageable);

    long countByImportBatch_Id(Long batchId);


    // =========================================
    // ASSIGNED LEADS
    // =========================================

    // Existing list method - preserve for compatibility
    List<Lead> findByAssignedTo_Id(Long assignedToId);

    // Paginated assigned leads
    Page<Lead> findByAssignedTo_Id(Long assignedToId, Pageable pageable);

    // Paginated leads assigned to multiple users
    Page<Lead> findByAssignedTo_IdIn(
            Collection<Long> userIds,
            Pageable pageable
    );

    // Check lead visibility without loading all leads
    boolean existsByIdAndAssignedTo_Id(Long id, Long assignedToId);

    boolean existsByIdAndAssignedTo_IdIn(
            Long id,
            Collection<Long> userIds
    );


    // =========================================
    // ASSIGNED / UNASSIGNED PAGINATION
    // =========================================

    Page<Lead> findByAssignedToIsNotNull(Pageable pageable);

    Page<Lead> findByAssignedToIsNull(Pageable pageable);


    // =========================================
    // DASHBOARD COUNTS
    // =========================================

    long countByStatus(LeadStatus status);

    long countByAssignedToIsNotNull();

    long countByAssignedToIsNull();

    long countByAssignedTo_Id(Long userId);

    long countByAssignedTo_IdAndStatus(
            Long userId,
            LeadStatus status
    );

    long countByAssignedTo_IdIn(Collection<Long> userIds);

    long countByAssignedTo_IdInAndStatus(
            Collection<Long> userIds,
            LeadStatus status
    );


    // =========================================
    // RAW ENQUIRY QUERIES
    // =========================================

    // Existing list methods - preserve for compatibility
    List<Lead> findByRawTrue();

    List<Lead> findByRawTrueAndImportStatus(
            LeadImportStatus importStatus
    );

    // Paginated RAW enquiries
    Page<Lead> findByRawTrue(Pageable pageable);

    // Paginated RAW enquiries by import status
    Page<Lead> findByRawTrueAndImportStatus(
            LeadImportStatus importStatus,
            Pageable pageable
    );

    // RAW enquiry counts
    long countByRawTrue();

    long countByRawTrueAndImportStatus(
            LeadImportStatus importStatus
    );
}
