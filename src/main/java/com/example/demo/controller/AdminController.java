
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.LeadResponseDTO;
import com.example.demo.dto.SalesUserResponseDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.User;
import com.example.demo.repository.LeadRepository;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    public AdminController(
            UserRepository userRepository,
            LeadRepository leadRepository) {
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
    }

    // Get all Sales Users

@GetMapping("/sales-users")
public List<SalesUserResponseDTO> getSalesUsers() {

    return userRepository.findAll()
            .stream()
            .filter(user -> user.getRoleEntity() != null
                    && "SALES".equals(
                            user.getRoleEntity().getName()))
            .map(user -> new SalesUserResponseDTO(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getMobile(),
                    user.getStatus(),
                    leadRepository.countByAssignedTo_Id(user.getId())
            ))
            .toList();
}


    // Get leads assigned to a particular Sales User
    @GetMapping("/sales-users/{userId}/leads")
    public List<LeadResponseDTO> getSalesUserLeads(
            @PathVariable Long userId) {

        User salesUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId));

        if (salesUser.getRoleEntity() == null
                || !"SALES".equals(
                        salesUser.getRoleEntity().getName())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The selected user is not a Sales User");
        }

        return leadRepository.findByAssignedTo_Id(userId)
                .stream()
                .map(this::convertToLeadResponseDTO)
                .toList();
    }

    private LeadResponseDTO convertToLeadResponseDTO(Lead lead) {

        LeadResponseDTO dto = new LeadResponseDTO();

        dto.setId(lead.getId());
        dto.setFirstName(lead.getFirstName());
        dto.setLastName(lead.getLastName());
        dto.setCompanyName(lead.getCompanyName());
        dto.setEmail(lead.getEmail());
        dto.setPhone(lead.getPhone());

        dto.setAddressLine1(lead.getAddressLine1());
        dto.setAddressLine2(lead.getAddressLine2());
        dto.setCity(lead.getCity());
        dto.setState(lead.getState());
        dto.setCountry(lead.getCountry());
        dto.setPincode(lead.getPincode());

        dto.setSource(lead.getSource());
        dto.setStatus(lead.getStatus());
        dto.setProduct(lead.getProduct());
        dto.setDescription(lead.getDescription());

        if (lead.getAssignedTo() != null) {
            dto.setAssignedToId(lead.getAssignedTo().getId());
            dto.setAssignedToName(
                    lead.getAssignedTo().getFirstName() + " "
                    + lead.getAssignedTo().getLastName());
        }

        dto.setCreatedAt(lead.getCreatedAt());
        dto.setUpdatedAt(lead.getUpdatedAt());

        return dto;
    }
}
