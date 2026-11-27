
package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.LeadImportBatch;

public interface LeadImportBatchRepository
        extends JpaRepository<LeadImportBatch, Long> {

}
