package com.example.demo.service;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.CustomerRequestDTO;
import com.example.demo.dto.CustomerResponseDTO;

public interface CustomerService {

    // ── Existing ──
    CustomerResponseDTO convertLeadToCustomer(Long leadId, CustomerRequestDTO request);
    List<CustomerResponseDTO> getAllCustomers();
    CustomerResponseDTO getCustomerById(Long id);
    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request);

    // ── Documents (NEW) ──
    CustomerResponseDTO uploadDocument(Long id, String docType, MultipartFile file) throws IOException;
    Resource downloadDocument(Long id, String docType);
    CustomerResponseDTO deleteDocument(Long id, String docType);
}