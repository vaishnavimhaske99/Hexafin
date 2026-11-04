package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.LeadStatusHistoryResponseDTO;
import com.example.demo.dto.LeadStatusUpdateRequestDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadStatusHistory;
import com.example.demo.entity.User;
import com.example.demo.mapper.LeadStatusHistoryMapper;
import com.example.demo.repository.LeadRepository;
import com.example.demo.repository.LeadStatusHistoryRepository;
import com.example.demo.repository.UserRepository;

@Service
public class LeadStatusHistoryServiceImpl implements LeadStatusHistoryService {

    private final LeadRepository leadRepository;
    private final LeadStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public LeadStatusHistoryServiceImpl(
            LeadRepository leadRepository,
            LeadStatusHistoryRepository historyRepository,
            UserRepository userRepository) {
        this.leadRepository = leadRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public LeadStatusHistoryResponseDTO updateLeadStatus(
            Long leadId,
            LeadStatusUpdateRequestDTO request) {

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + leadId));

        if (lead.getStatus() == request.getStatus()) {
            throw new RuntimeException("Lead is already in status: " + request.getStatus());
        }

        // History तयार कर
        LeadStatusHistory history = new LeadStatusHistory();
        history.setLead(lead);
        history.setOldStatus(lead.getStatus());
        history.setNewStatus(request.getStatus());
        history.setChangedAt(LocalDateTime.now());

        // 🔍 Debug
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("=== AUTH DEBUG ===");
        System.out.println("Auth: " + auth);
        if (auth != null) {
            System.out.println("Principal: " + auth.getPrincipal());
            if (auth.getPrincipal() != null) {
                System.out.println("Principal class: " + auth.getPrincipal().getClass().getName());
            }
        }

        // ✅ changedBy सेट कर (User + String दोन्ही हाताळ)
        if (auth != null && auth.getPrincipal() != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User) {
                history.setChangedBy((User) principal);
                System.out.println("✅ changedBy SET: " + ((User) principal).getId());
            } else if (principal instanceof String) {
                String email = (String) principal;
                userRepository.findByEmail(email).ifPresent(u -> {
                    history.setChangedBy(u);
                    System.out.println("✅ changedBy SET from email: " + u.getId());
                });
            }
        } else {
            System.out.println("❌ changedBy NOT SET — no auth");
        }

        // Lead status update
        lead.setStatus(request.getStatus());
        leadRepository.save(lead);

        // History save
        LeadStatusHistory savedHistory = historyRepository.save(history);

        return LeadStatusHistoryMapper.toResponseDTO(savedHistory);
    }

    @Override
    public List<LeadStatusHistoryResponseDTO> getStatusHistoryByLeadId(Long leadId) {
        if (!leadRepository.existsById(leadId)) {
            throw new RuntimeException("Lead not found with id: " + leadId);
        }
        return historyRepository.findByLeadId(leadId).stream()
                .map(LeadStatusHistoryMapper::toResponseDTO)
                .toList();
    }
}