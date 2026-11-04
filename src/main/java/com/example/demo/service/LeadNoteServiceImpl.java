package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.LeadNoteRequestDTO;
import com.example.demo.dto.LeadNoteResponseDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadNote;
import com.example.demo.entity.User;
import com.example.demo.mapper.LeadNoteMapper;
import com.example.demo.repository.LeadNoteRepository;
import com.example.demo.repository.LeadRepository;
import com.example.demo.repository.UserRepository;

@Service
public class LeadNoteServiceImpl implements LeadNoteService {

    private final LeadNoteRepository leadNoteRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository; // ✅ नवीन

    public LeadNoteServiceImpl(
            LeadNoteRepository leadNoteRepository,
            LeadRepository leadRepository,
            UserRepository userRepository) { // ✅ नवीन

        this.leadNoteRepository = leadNoteRepository;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
    }

    @Override
    public LeadNoteResponseDTO createNote(
            Long leadId,
            LeadNoteRequestDTO request) {

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lead not found with id: " + leadId));

        LeadNote note =
                LeadNoteMapper.toEntity(request, lead);

        LocalDateTime now = LocalDateTime.now();

        note.setCreatedAt(now);
        note.setUpdatedAt(now);

        // ✅ लॉगिन केलेल्या युजरची माहिती सेट करा
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🔍 Debug (कारण तपासण्यासाठी — नंतर काढून टाक)
        System.out.println("=== AUTH DEBUG ===");
        System.out.println("Auth: " + auth);
        System.out.println("Principal: " + (auth != null ? auth.getPrincipal() : "null"));
        if (auth != null && auth.getPrincipal() != null) {
            System.out.println("Principal class: " + auth.getPrincipal().getClass().getName());
        }

        if (auth != null && auth.getPrincipal() != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User) {
                note.setCreatedBy((User) principal);
            } else if (principal instanceof String) {
                String email = (String) principal;
                userRepository.findByEmail(email).ifPresent(note::setCreatedBy);
            }
        }

        LeadNote savedNote =
                leadNoteRepository.save(note);

        return LeadNoteMapper
                .toResponseDTO(savedNote);
    }

    @Override
    public List<LeadNoteResponseDTO> getNotesByLeadId(
            Long leadId) {

        if (!leadRepository.existsById(leadId)) {
            throw new RuntimeException(
                    "Lead not found with id: " + leadId);
        }

        return leadNoteRepository
                .findByLeadId(leadId)
                .stream()
                .map(LeadNoteMapper::toResponseDTO)
                .toList();
    }

    @Override
    public LeadNoteResponseDTO getNoteById(Long id) {

        LeadNote note =
                leadNoteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Note not found with id: " + id));

        return LeadNoteMapper
                .toResponseDTO(note);
    }

    @Override
    public LeadNoteResponseDTO updateNote(
            Long id,
            LeadNoteRequestDTO request) {

        LeadNote note =
                leadNoteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Note not found with id: " + id));

        note.setContent(request.getContent());
        note.setUpdatedAt(LocalDateTime.now());

        LeadNote updatedNote =
                leadNoteRepository.save(note);

        return LeadNoteMapper
                .toResponseDTO(updatedNote);
    }

    @Override
    public void deleteNote(Long id) {

        LeadNote note =
                leadNoteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Note not found with id: " + id));

        leadNoteRepository.delete(note);
    }
}