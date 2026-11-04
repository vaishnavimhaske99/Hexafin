package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Sales User ने जे customers बनवले ते शोधण्यासाठी
    List<Customer> findByCreatedById(Long userId);

    List<Customer> findByCreatedByIdIn(List<Long> userIds);

    // Lead आधीच customer मध्ये convert झाली आहे का?
    Optional<Customer> findBySourceLeadId(Long leadId);

    boolean existsBySourceLeadId(Long leadId);
}