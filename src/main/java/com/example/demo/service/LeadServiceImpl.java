
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.LeadRequestDTO;
import com.example.demo.dto.LeadResponseDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadImportStatus;
import com.example.demo.entity.LeadStatus;
import com.example.demo.entity.NotificationCategory;
import com.example.demo.entity.NotificationPriority;
import com.example.demo.entity.NotificationRequestDTO;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.mapper.LeadMapper;
import com.example.demo.repository.LeadRepository;
import com.example.demo.repository.UserRepository;

@Service
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LeadServiceImpl(
            LeadRepository leadRepository,
            UserRepository userRepository,
            NotificationService notificationService) {

        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // =========================================
    // GET LEADS FOR A SELECTED SALES USER - ADMIN
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getLeadsBySalesUser(Long userId) {

        User loggedInUser = getAuthenticatedUser();
        UserRole role = loggedInUser.getRole();

        if (role != UserRole.ADMIN
                && role != UserRole.MANAGER
                && role != UserRole.TEAM_LEAD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN, MANAGER or TEAM_LEAD can view Sales User leads"
            );
        }

        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid Sales User ID is required"
            );
        }

        User salesUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));

        if (salesUser.getRole() != UserRole.SALES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected user is not a SALES user"
            );
        }

        if (role != UserRole.ADMIN) {
            boolean isInMyTeam =
                    isUnderUserHierarchy(salesUser, loggedInUser.getId());

            if (!isInMyTeam) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "This Sales User is not in your team"
                );
            }
        }

        return leadRepository.findByAssignedTo_Id(userId)
                .stream()
                .map(LeadMapper::toResponseDTO)
                .toList();
    }

    // =========================================
    // GET LEADS ASSIGNED TO ANY USER
    // =========================================
    @Override
    public List<LeadResponseDTO> getLeadsByAssignedTo(Long userId) {

        List<Lead> leads =
                leadRepository.findByAssignedTo_Id(userId);

        return leads.stream()
                .map(LeadMapper::toResponseDTO)
                .toList();
    }

    // =========================================
    // GET AUTHENTICATED USER
    // =========================================
    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please login first"
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        if (principal instanceof String) {

            String email = ((String) principal)
                    .trim()
                    .toLowerCase(Locale.ROOT);

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Authenticated user not found"
                    ));
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Unable to identify logged-in user"
        );
    }

    // =========================================
    // GET TEAM LEADS (MANAGER / TEAM_LEAD)
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getTeamLeads(Long supervisorId) {

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser.getRole() != UserRole.MANAGER
                && loggedInUser.getRole() != UserRole.TEAM_LEAD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only MANAGER or TEAM_LEAD can view team leads"
            );
        }

        Long actualSupervisorId =
                (supervisorId != null) ? supervisorId : loggedInUser.getId();

        List<User> allUsers = userRepository.findAll();

        java.util.Set<Long> teamIds = new java.util.HashSet<>();
        teamIds.add(actualSupervisorId);

        boolean changed;

        do {
            changed = false;

            for (User candidate : allUsers) {
                if (candidate.getReportsTo() != null
                        && teamIds.contains(candidate.getReportsTo().getId())) {

                    if (teamIds.add(candidate.getId())) {
                        changed = true;
                    }
                }
            }

        } while (changed);

        return leadRepository.findAll()
                .stream()
                .filter(lead -> lead.getAssignedTo() != null
                        && teamIds.contains(lead.getAssignedTo().getId()))
                .map(LeadMapper::toResponseDTO)
                .toList();
    }

    // =========================================
    // CREATE LEAD
    // =========================================
    @Override
    @Transactional
    public LeadResponseDTO createLead(LeadRequestDTO request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lead request is required"
            );
        }

        Lead lead = LeadMapper.toEntity(request);

        lead.setStatus(LeadStatus.NEW);

        LocalDateTime now = LocalDateTime.now();

        lead.setCreatedAt(now);
        lead.setUpdatedAt(now);

        Lead savedLead = leadRepository.save(lead);

        return LeadMapper.toResponseDTO(savedLead);
    }

    // =========================================
    // GET ALL LEADS - ROLE-BASED FILTERING
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getAllLeads() {

        User user = getAuthenticatedUser();

        if (user.getRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User role is not configured"
            );
        }

        List<Lead> leads;

        switch (user.getRole()) {

            case ADMIN:
                leads = leadRepository.findAll();
                break;

            case MANAGER:
            case TEAM_LEAD:
            case SALES:
                leads = getVisibleLeads(user);
                break;

            default:
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to view leads"
                );
        }

        return leads.stream()
                .map(LeadMapper::toResponseDTO)
                .toList();
    }

    private List<Lead> getVisibleLeads(User loggedInUser) {

        Long loggedInUserId = loggedInUser.getId();

        if (loggedInUser.getRole() == UserRole.SALES) {
            return leadRepository.findByAssignedTo_Id(loggedInUserId);
        }

        List<User> allUsers = userRepository.findAll();

        java.util.Set<Long> visibleUserIds = new java.util.HashSet<>();
        visibleUserIds.add(loggedInUserId);

        boolean changed;

        do {
            changed = false;

            for (User candidate : allUsers) {

                if (candidate.getReportsTo() != null
                        && visibleUserIds.contains(
                                candidate.getReportsTo().getId())) {

                    if (visibleUserIds.add(candidate.getId())) {
                        changed = true;
                    }
                }
            }

        } while (changed);

        return leadRepository.findAll()
                .stream()
                .filter(lead ->
                        lead.getAssignedTo() != null
                        && visibleUserIds.contains(
                                lead.getAssignedTo().getId()))
                .toList();
    }

    // =========================================
    // GET LEAD BY ID - OWNERSHIP CHECK
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public LeadResponseDTO getLeadById(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid lead ID is required"
            );
        }

        User user = getAuthenticatedUser();

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lead not found with id: " + id
                ));

        if (user.getRole() == UserRole.ADMIN) {
            return LeadMapper.toResponseDTO(lead);
        }

        boolean canView = getVisibleLeads(user)
                .stream()
                .anyMatch(visibleLead ->
                        visibleLead.getId().equals(lead.getId()));

        if (!canView) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view this lead"
            );
        }

        return LeadMapper.toResponseDTO(lead);
    }

    // =========================================
    // UPDATE LEAD - OWNERSHIP CHECK
    // =========================================
    @Override
    @Transactional
    public LeadResponseDTO updateLead(
            Long id,
            LeadRequestDTO request) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid lead ID is required"
            );
        }

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lead request is required"
            );
        }

        User user = getAuthenticatedUser();

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lead not found with id: " + id
                ));

        if (user.getRole() != UserRole.ADMIN) {

            boolean canUpdate = getVisibleLeads(user)
                    .stream()
                    .anyMatch(visibleLead ->
                            visibleLead.getId().equals(lead.getId()));

            if (!canUpdate) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to update this lead"
                );
            }
        }

        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setCompanyName(request.getCompanyName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());

        lead.setAddressLine1(request.getAddressLine1());
        lead.setAddressLine2(request.getAddressLine2());

        lead.setCity(request.getCity());
        lead.setState(request.getState());
        lead.setCountry(request.getCountry());
        lead.setPincode(request.getPincode());

        lead.setSource(request.getSource());
        lead.setProduct(request.getProduct());
        lead.setDescription(request.getDescription());

        lead.setUpdatedAt(LocalDateTime.now());

        Lead updatedLead = leadRepository.save(lead);

        return LeadMapper.toResponseDTO(updatedLead);
    }

    // =========================================
    // DELETE LEAD - ADMIN ONLY
    // =========================================
    @Override
    @Transactional
    public void deleteLead(Long id) {

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN can delete leads"
            );
        }

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid lead ID is required"
            );
        }

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lead not found with id: " + id
                ));

        leadRepository.delete(lead);
    }

    // =========================================
    // ASSIGN / REASSIGN LEAD - HIERARCHY BASED
    // =========================================
    @Override
    @Transactional
    public LeadResponseDTO assignLead(Long leadId, Long assignedToId) {

        if (leadId == null || leadId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid Lead ID is required"
            );
        }

        if (assignedToId == null || assignedToId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid User ID is required"
            );
        }

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser.getRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User role is not configured"
            );
        }

        UserRole role = loggedInUser.getRole();

        if (role != UserRole.ADMIN
                && role != UserRole.MANAGER
                && role != UserRole.TEAM_LEAD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to assign leads"
            );
        }

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lead not found"
                ));

        User targetUser = userRepository.findById(assignedToId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (role != UserRole.ADMIN) {

            boolean belongsToHierarchy =
                    isUnderUserHierarchy(targetUser, loggedInUser.getId());

            if (!belongsToHierarchy) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can assign leads only to users within your hierarchy"
                );
            }
        }

        lead.setAssignedTo(targetUser);
        lead.setUpdatedAt(LocalDateTime.now());

        Lead updatedLead = leadRepository.save(lead);

        NotificationRequestDTO request = new NotificationRequestDTO();

        request.setType(NotificationType.LEAD_ASSIGNED);
        request.setCategory(NotificationCategory.SALES);
        request.setPriority(NotificationPriority.NORMAL);

        request.setTitle("New Lead Assigned");
        request.setMessage(
            "Lead #" + updatedLead.getId()
            + " has been assigned to you."
        );

        request.setReferenceType("LEAD");
        request.setReferenceId(updatedLead.getId());
        request.setActionUrl("/leads/" + updatedLead.getId());

        notificationService.createNotification(
            targetUser.getId(),
            loggedInUser.getId(),
            request
        );
        return LeadMapper.toResponseDTO(updatedLead);
    }

    // =========================================
    // CHECK WHETHER TARGET IS A DESCENDANT
    // =========================================
    private boolean isUnderUserHierarchy(
            User targetUser,
            Long supervisorId) {

        if (targetUser == null || supervisorId == null) {
            return false;
        }

        java.util.Set<Long> visitedUserIds = new java.util.HashSet<>();

        User current = targetUser;

        while (current != null) {

            Long currentId = current.getId();

            if (currentId == null
                    || !visitedUserIds.add(currentId)) {
                return false;
            }

            User parent = current.getReportsTo();

            if (parent == null) {
                return false;
            }

            if (supervisorId.equals(parent.getId())) {
                return true;
            }

            current = parent;
        }

        return false;
    }

    // =========================================
    // GET MY LEADS
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getMyLeads(Long userId) {

        User user = getAuthenticatedUser();

        if (user.getRole() != UserRole.SALES) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only SALES users can access their assigned leads"
            );
        }

        if (userId == null || !user.getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only view your own assigned leads"
            );
        }

        List<Lead> leads =
                leadRepository.findByAssignedTo_Id(user.getId());

        return leads.stream()
                .map(LeadMapper::toResponseDTO)
                .toList();
    }

    // =========================================
    // RAW LEADS MANAGEMENT
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getRawLeads() {

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser.getRole() != UserRole.ADMIN
                && loggedInUser.getRole() != UserRole.MANAGER
                && loggedInUser.getRole() != UserRole.TEAM_LEAD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view raw leads"
            );
        }

        return leadRepository.findByRawTrue()
                .stream()
                .map(LeadMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getRawLeadsByImportStatus(
            LeadImportStatus importStatus) {

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser.getRole() != UserRole.ADMIN
                && loggedInUser.getRole() != UserRole.MANAGER
                && loggedInUser.getRole() != UserRole.TEAM_LEAD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view raw leads"
            );
        }

        if (importStatus == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Import status is required"
            );
        }

        return leadRepository
                .findByRawTrueAndImportStatus(importStatus)
                .stream()
                .map(LeadMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countRawLeads() {

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser.getRole() != UserRole.ADMIN
                && loggedInUser.getRole() != UserRole.MANAGER
                && loggedInUser.getRole() != UserRole.TEAM_LEAD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to count raw leads"
            );
        }

        return leadRepository.countByRawTrue();
    }
}
