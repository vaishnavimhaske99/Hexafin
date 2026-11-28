
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private static final int DEFAULT_PAGE_SIZE = 25;

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
    // DEFAULT PAGINATION
    // =========================================

    private Pageable defaultPageable() {
        return PageRequest.of(
                0,
                DEFAULT_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "id")
        );
    }

    private Page<LeadResponseDTO> toDTOPage(Page<Lead> leads) {
        return leads.map(LeadMapper::toResponseDTO);
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
    // GET VISIBLE USER IDS
    // =========================================

    private Set<Long> getVisibleUserIds(User loggedInUser) {

        Set<Long> visibleUserIds = new HashSet<>();

        if (loggedInUser.getId() == null) {
            return visibleUserIds;
        }

        visibleUserIds.add(loggedInUser.getId());

        if (loggedInUser.getRole() == UserRole.SALES) {
            return visibleUserIds;
        }

        if (loggedInUser.getRole() != UserRole.ADMIN
                && loggedInUser.getRole() != UserRole.MANAGER
                && loggedInUser.getRole() != UserRole.TEAM_LEAD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view leads"
            );
        }

        List<User> allUsers = userRepository.findAll();

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

        return visibleUserIds;
    }

    // =========================================
    // GET ALL LEADS - PAGINATED
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponseDTO> getAllLeads(Pageable pageable) {

        User user = getAuthenticatedUser();

        if (user.getRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User role is not configured"
            );
        }

        switch (user.getRole()) {

            case ADMIN:
                return toDTOPage(leadRepository.findAll(pageable));

            case SALES:
            case MANAGER:
            case TEAM_LEAD:

                Set<Long> visibleUserIds = getVisibleUserIds(user);

                if (visibleUserIds.isEmpty()) {
                    return Page.empty(pageable);
                }

                return toDTOPage(
                        leadRepository.findByAssignedTo_IdIn(
                                visibleUserIds,
                                pageable
                        )
                );

            default:
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to view leads"
                );
        }
    }

    // Compatibility method for existing controller
    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getAllLeads() {
        return getAllLeads(defaultPageable()).getContent();
    }

    // =========================================
    // GET LEADS ASSIGNED TO A USER
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponseDTO> getLeadsByAssignedTo(
            Long userId,
            Pageable pageable) {

        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid User ID is required"
            );
        }

        return toDTOPage(
                leadRepository.findByAssignedTo_Id(userId, pageable)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getLeadsByAssignedTo(Long userId) {
        return getLeadsByAssignedTo(userId, defaultPageable()).getContent();
    }

    // =========================================
    // GET LEADS FOR SELECTED SALES USER
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponseDTO> getLeadsBySalesUser(
            Long userId,
            Pageable pageable) {

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

        if (role != UserRole.ADMIN
                && !isUnderUserHierarchy(salesUser, loggedInUser.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This Sales User is not in your team"
            );
        }

        return toDTOPage(
                leadRepository.findByAssignedTo_Id(userId, pageable)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getLeadsBySalesUser(Long userId) {
        return getLeadsBySalesUser(userId, defaultPageable()).getContent();
    }

    // =========================================
    // GET TEAM LEADS
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponseDTO> getTeamLeads(
            Long supervisorId,
            Pageable pageable) {

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser.getRole() != UserRole.MANAGER
                && loggedInUser.getRole() != UserRole.TEAM_LEAD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only MANAGER or TEAM_LEAD can view team leads"
            );
        }

        Long actualSupervisorId =
                supervisorId != null
                        ? supervisorId
                        : loggedInUser.getId();

        Set<Long> teamIds = new HashSet<>();
        teamIds.add(actualSupervisorId);

        List<User> allUsers = userRepository.findAll();

        boolean changed;

        do {
            changed = false;

            for (User candidate : allUsers) {

                if (candidate.getReportsTo() != null
                        && teamIds.contains(
                                candidate.getReportsTo().getId())) {

                    if (teamIds.add(candidate.getId())) {
                        changed = true;
                    }
                }
            }

        } while (changed);

        if (teamIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return toDTOPage(
                leadRepository.findByAssignedTo_IdIn(
                        teamIds,
                        pageable
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getTeamLeads(Long supervisorId) {
        return getTeamLeads(supervisorId, defaultPageable()).getContent();
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
    // GET LEAD BY ID
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

        if (user.getRole() != UserRole.ADMIN) {

            Set<Long> visibleUserIds = getVisibleUserIds(user);

            if (visibleUserIds.isEmpty()
                    || !leadRepository.existsByIdAndAssignedTo_IdIn(
                            id,
                            visibleUserIds
                    )) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to view this lead"
                );
            }
        }

        return LeadMapper.toResponseDTO(lead);
    }

    // =========================================
    // UPDATE LEAD
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

            Set<Long> visibleUserIds = getVisibleUserIds(user);

            if (visibleUserIds.isEmpty()
                    || !leadRepository.existsByIdAndAssignedTo_IdIn(
                            id,
                            visibleUserIds
                    )) {

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
    // ASSIGN / REASSIGN LEAD
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
    // CHECK USER HIERARCHY
    // =========================================

    private boolean isUnderUserHierarchy(
            User targetUser,
            Long supervisorId) {

        if (targetUser == null || supervisorId == null) {
            return false;
        }

        Set<Long> visitedUserIds = new HashSet<>();

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
    public Page<LeadResponseDTO> getMyLeads(
            Long userId,
            Pageable pageable) {

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

        return toDTOPage(
                leadRepository.findByAssignedTo_Id(user.getId(), pageable)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getMyLeads(Long userId) {
        return getMyLeads(userId, defaultPageable()).getContent();
    }

    // =========================================
    // RAW ENQUIRIES
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponseDTO> getRawLeads(Pageable pageable) {

        User loggedInUser = getAuthenticatedUser();

        if (loggedInUser.getRole() != UserRole.ADMIN
                && loggedInUser.getRole() != UserRole.MANAGER
                && loggedInUser.getRole() != UserRole.TEAM_LEAD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view raw leads"
            );
        }

        return toDTOPage(
                leadRepository.findByRawTrue(pageable)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getRawLeads() {
        return getRawLeads(defaultPageable()).getContent();
    }

    // =========================================
    // RAW ENQUIRIES BY IMPORT STATUS
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponseDTO> getRawLeadsByImportStatus(
            LeadImportStatus importStatus,
            Pageable pageable) {

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

        return toDTOPage(
                leadRepository.findByRawTrueAndImportStatus(
                        importStatus,
                        pageable
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getRawLeadsByImportStatus(
            LeadImportStatus importStatus) {

        return getRawLeadsByImportStatus(
                importStatus,
                defaultPageable()
        ).getContent();
    }

    // =========================================
    // COUNT RAW ENQUIRIES
    // =========================================

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
