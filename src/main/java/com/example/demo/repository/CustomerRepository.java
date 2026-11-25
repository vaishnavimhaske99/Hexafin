package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {


    List<Customer> findByCreatedById(Long userId);

    List<Customer> findByCreatedByIdIn(List<Long> userIds);
 // Birthday customers (aaj birthday aslele)
    @Query("SELECT c FROM Customer c WHERE " +
           "MONTH(c.dateOfBirth) = :month AND DAY(c.dateOfBirth) = :day")
    List<Customer> findByBirthday(@Param("month") int month, @Param("day") int day);
    Optional<Customer> findBySourceLeadId(Long leadId);

    boolean existsBySourceLeadId(Long leadId);
}