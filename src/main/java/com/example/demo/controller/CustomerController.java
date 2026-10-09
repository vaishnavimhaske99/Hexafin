package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CustomerRequestDTO;
import com.example.demo.dto.CustomerResponseDTO;
import com.example.demo.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // GET ALL CUSTOMERS
    @GetMapping
    public List<CustomerResponseDTO> getAllCustomers() {

        return customerService.getAllCustomers();
    }

    // GET CUSTOMER BY ID
    @GetMapping("/{customerId}")
    public CustomerResponseDTO getCustomerById(
            @PathVariable Long customerId) {

        return customerService.getCustomerById(customerId);
    }

    // GET CUSTOMER BY SOURCE LEAD
    @GetMapping("/lead/{leadId}")
    public CustomerResponseDTO getCustomerByLeadId(
            @PathVariable Long leadId) {

        return customerService.getCustomerByLeadId(leadId);
    }

    // UPDATE CUSTOMER
    @PutMapping("/{customerId}")
    public CustomerResponseDTO updateCustomer(
            @PathVariable Long customerId,
            @RequestBody CustomerRequestDTO request) {

        return customerService.updateCustomer(
                customerId,
                request);
    }
}