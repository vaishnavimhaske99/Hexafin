package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.SignupRequestDTO;
import com.example.demo.dto.UpdateUserHierarchyRequestDTO;
import com.example.demo.dto.UserRequestDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.entity.UserSupervisorHistory;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSupervisorHistoryRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSupervisorHistoryRepository historyRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            UserSupervisorHistoryRepository historyRepository) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.historyRepository = historyRepository;
    }

    // =========================================
    // AUTHENTICATED USER HELPER
    // =========================================
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        if (principal instanceof String) {
            String email = ((String) principal).trim().toLowerCase(Locale.ROOT);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "User not found"));
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unable to identify user");
    }

    // =========================================
    // CREATE USER (ADMIN ONLY)
    // =========================================
    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("User request is required.");
        if (request.getFirstName() == null || request.getFirstName().isBlank())
            throw new IllegalArgumentException("First name is required.");
        if (request.getEmail() == null || request.getEmail().isBlank())
            throw new IllegalArgumentException("Email is required.");
        if (request.getPassword() == null || request.getPassword().length() < 8)
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        if (request.getRole() == null || request.getRole().isBlank())
            throw new IllegalArgumentException("Role is required.");

        UserRole selectedRole = parseRole(request.getRole());
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email is already registered.");

        String mobile = normalizeMobile(request.getMobile());
        if (mobile != null && userRepository.existsByMobile(mobile))
            throw new IllegalArgumentException("Mobile number is already registered.");

        Role roleEntity = findRole(selectedRole);

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(normalizeName(request.getLastName()));
        user.setEmail(email);
        user.setMobile(mobile);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setRole(selectedRole);
        user.setRoleEntity(roleEntity);

        return toUserResponseDTO(userRepository.save(user));
    }

    // =========================================
    // PUBLIC SIGNUP (SALES ONLY)
    // =========================================
    @Override
    public UserResponseDTO createPublicUser(SignupRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("Signup request is required.");
        if (request.getFirstName() == null || request.getFirstName().isBlank())
            throw new IllegalArgumentException("First name is required.");
        if (request.getEmail() == null || request.getEmail().isBlank())
            throw new IllegalArgumentException("Email is required.");
        if (request.getPassword() == null || request.getPassword().length() < 8)
            throw new IllegalArgumentException("Password must contain at least 8 characters.");

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email is already registered.");

        String mobile = normalizeMobile(request.getMobile());
        if (mobile != null && userRepository.existsByMobile(mobile))
            throw new IllegalArgumentException("Mobile number is already registered.");

        Role roleEntity = findRole(UserRole.SALES);

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(normalizeName(request.getLastName()));
        user.setEmail(email);
        user.setMobile(mobile);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setRole(UserRole.SALES);
        user.setRoleEntity(roleEntity);

        return toUserResponseDTO(userRepository.save(user));
    }

    // =========================================
    // GET ALL USERS — ROLE-BASED FILTER
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        User loggedInUser = getAuthenticatedUser();
        UserRole role = loggedInUser.getRole();
        List<User> users;

        if (role == UserRole.ADMIN) {
            users = userRepository.findAllWithReportsTo();
        } else if (role == UserRole.MANAGER) {
            users = getUsersInHierarchy(loggedInUser);
        } else if (role == UserRole.TEAM_LEAD) {
            List<User> mySales = userRepository.findByReportsTo_Id(loggedInUser.getId())
                    .stream()
                    .filter(u -> u.getRole() == UserRole.SALES)
                    .toList();

            List<User> result = new ArrayList<>();
            result.add(loggedInUser);
            result.addAll(mySales);
            users = result;
        } else {
            users = List.of(loggedInUser);
        }

        return users.stream().map(this::toUserResponseDTO).toList();
    }

    // =========================================
    // GET TEAM MEMBERS
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findByReportsToId(Long reportsToId) {
        if (reportsToId == null || reportsToId <= 0)
            throw new IllegalArgumentException("Reports-to ID must be a positive number.");

        User supervisor = userRepository.findById(reportsToId)
                .orElseThrow(() -> new IllegalArgumentException("Supervisor not found with id: " + reportsToId));

        User loggedInUser = getAuthenticatedUser();
        UserRole loggedInRole = loggedInUser.getRole();

        if (loggedInRole == UserRole.TEAM_LEAD && !supervisor.getId().equals(loggedInUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own team members");
        }

        List<User> members = userRepository.findByReportsTo_Id(reportsToId);

        return members.stream()
                .filter(u -> u.getRole() == UserRole.SALES)
                .map(this::toUserResponseDTO)
                .toList();
    }

    // =========================================
    // GET USER BY ID
    // =========================================
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("User ID must be a positive number.");

        User loggedInUser = getAuthenticatedUser();
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        UserRole role = loggedInUser.getRole();

        if (role == UserRole.ADMIN) return toUserResponseDTO(targetUser);
        if (targetUser.getId().equals(loggedInUser.getId())) return toUserResponseDTO(targetUser);

        if (role == UserRole.TEAM_LEAD) {
            boolean isMySales = targetUser.getReportsTo() != null
                    && targetUser.getReportsTo().getId().equals(loggedInUser.getId())
                    && targetUser.getRole() == UserRole.SALES;
            if (!isMySales) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your team members");
            return toUserResponseDTO(targetUser);
        }

        if (role == UserRole.MANAGER) {
            boolean isInHierarchy = isUserInMyHierarchy(targetUser, loggedInUser);
            if (!isInHierarchy) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your team members");
            return toUserResponseDTO(targetUser);
        }
        

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this user");
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getManagerTeam() {
        User loggedInUser = getAuthenticatedUser();
        return getUsersInHierarchy(loggedInUser)
                .stream()
                .filter(u -> !u.getId().equals(loggedInUser.getId()))
                .map(this::toUserResponseDTO)
                .toList();
    }

    // =========================================
    // UPDATE HIERARCHY (With History)
    // =========================================
    @Override
    public UserResponseDTO updateUserHierarchy(Long userId, UpdateUserHierarchyRequestDTO request) {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("User ID must be a positive number.");
        if (request == null) throw new IllegalArgumentException("Hierarchy request is required.");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        UserRole newRole = parseRole(request.getRole());
        Long supervisorId = request.getReportsToId();

        if (supervisorId != null && supervisorId.equals(userId))
            throw new IllegalArgumentException("A user cannot report to themselves.");

        if (user.getRole() == UserRole.ADMIN && newRole != UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) throw new IllegalArgumentException("Cannot demote the last ADMIN.");
        }

        User supervisor = null;
        if (supervisorId != null) {
            supervisor = userRepository.findById(supervisorId)
                    .orElseThrow(() -> new IllegalArgumentException("Supervisor not found with id: " + supervisorId));

            User current = supervisor;
            while (current != null) {
                if (current.getId().equals(userId))
                    throw new IllegalArgumentException("Circular reporting hierarchy is not allowed.");
                current = current.getReportsTo();
            }
        }

        validateParentRole(newRole, supervisor);
        if (supervisor != null) validateDescendantRoles(user, newRole);

        // ✅ HISTORY TRACKING LOGIC START
        Long oldSupervisorId = user.getReportsTo() != null ? user.getReportsTo().getId() : null;
        boolean supervisorChanged = (oldSupervisorId == null && supervisorId != null) 
                                 || (oldSupervisorId != null && !oldSupervisorId.equals(supervisorId));

        if (supervisorChanged) {
            LocalDateTime now = LocalDateTime.now();
            User loggedInUser = getAuthenticatedUser();

            // जुना रेकॉर्ड बंद करा
            historyRepository.findByUserIdAndEndedAtIsNull(user.getId()).ifPresent(oldRecord -> {
                oldRecord.setEndedAt(now);
                historyRepository.save(oldRecord);
            });

            // नवीन रेकॉर्ड बनवा
            if (supervisor != null) {
                UserSupervisorHistory newRecord = new UserSupervisorHistory();
                newRecord.setUser(user);
                newRecord.setSupervisor(supervisor);
                newRecord.setAssignedAt(now);
                newRecord.setChangedBy(loggedInUser);
                newRecord.setReason("Hierarchy Updated by " + loggedInUser.getRole());
                historyRepository.save(newRecord);
            }
        }
        // ✅ HISTORY TRACKING LOGIC END

        Role roleEntity = findRole(newRole);
        user.setRole(newRole);
        user.setRoleEntity(roleEntity);
        user.setReportsTo(supervisor);

        return toUserResponseDTO(userRepository.save(user));
    }

    // =========================================
    // DEACTIVATE & REACTIVATE (NEW)
    // =========================================
 // UserServiceImpl.java मध्ये हे दोन मेथड्स ऍड करा:

    @Override
    @Transactional
    public UserResponseDTO deactivateUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if ("INACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("User is already deactivated.");
        }

        // ✅ सुरक्षिततेसाठी: जर युजरच्या खाली लोक असतील तर डीऐक्टिव्हेट करू देऊ नका
        List<User> subordinates = userRepository.findByReportsTo_Id(userId);
        if (!subordinates.isEmpty()) {
            throw new IllegalArgumentException("Cannot deactivate. " + subordinates.size() + 
                " user(s) report to this user. Please reassign them first.");
        }

        user.setStatus("INACTIVE");
        // जर तुमच्याकडे deactivatedAt आणि deactivationReason फील्ड्स असतील तर ते पण सेट करा

        return toUserResponseDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDTO reactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setStatus("ACTIVE");
        // जर deactivatedAt फील्ड असेल तर ते null करा

        return toUserResponseDTO(userRepository.save(user));
    }
    // =========================================
    // HELPER METHODS (Your original ones)
    // =========================================
    private List<User> getUsersInHierarchy(User manager) {
        List<User> allUsers = userRepository.findAllWithReportsTo();
        Set<Long> visibleIds = new HashSet<>();
        visibleIds.add(manager.getId());

        boolean changed;
        do {
            changed = false;
            for (User u : allUsers) {
                if (u.getReportsTo() != null && visibleIds.contains(u.getReportsTo().getId())) {
                    if (visibleIds.add(u.getId())) changed = true;
                }
            }
        } while (changed);

        return allUsers.stream().filter(u -> visibleIds.contains(u.getId())).toList();
    }

    private boolean isUserInMyHierarchy(User target, User supervisor) {
        User current = target;
        Set<Long> visited = new HashSet<>();

        while (current != null) {
            if (current.getId().equals(supervisor.getId())) return true;
            if (!visited.add(current.getId())) return false;
            current = current.getReportsTo();
        }
        return false;
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role. Allowed: ADMIN, MANAGER, TEAM_LEAD, SALES.");
        }
    }

    private Role findRole(UserRole userRole) {
        return roleRepository.findByName(userRole.name())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + userRole.name()));
    }

    private String normalizeMobile(String mobile) {
        if (mobile == null || mobile.isBlank()) return null;
        return mobile.trim();
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return null;
        return name.trim();
    }

    private UserResponseDTO toUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setStatus(user.getStatus());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);

        if (user.getReportsTo() != null) {
            dto.setReportsToId(user.getReportsTo().getId());
            String firstName = user.getReportsTo().getFirstName();
            String lastName = user.getReportsTo().getLastName();
            dto.setReportsToName(((firstName != null) ? firstName : "") + " " + ((lastName != null) ? lastName : ""));
        } else {
            dto.setReportsToId(null);
            dto.setReportsToName(null);
        }
        return dto;
    }

    private void validateParentRole(UserRole role, User supervisor) {
        if (role == UserRole.ADMIN) {
            if (supervisor != null) throw new IllegalArgumentException("An ADMIN is top-level and must not report to any user.");
            return;
        }
        if (supervisor == null) return;

        UserRole parentRole = supervisor.getRole();
        if (role == UserRole.MANAGER && parentRole != UserRole.ADMIN)
            throw new IllegalArgumentException("A MANAGER must report to an ADMIN.");
        if (role == UserRole.TEAM_LEAD && parentRole != UserRole.MANAGER)
            throw new IllegalArgumentException("A TEAM_LEAD must report to a MANAGER.");
        if (role == UserRole.SALES && parentRole != UserRole.TEAM_LEAD && parentRole != UserRole.MANAGER)
            throw new IllegalArgumentException("A SALES must report to a MANAGER or a TEAM_LEAD.");
    }

    private UserRole requiredChildRole(UserRole role) {
        return switch (role) {
            case ADMIN -> UserRole.MANAGER;
            case MANAGER -> UserRole.TEAM_LEAD;
            case TEAM_LEAD -> UserRole.SALES;
            case SALES -> null;
        };
    }

    private void validateDescendantRoles(User user, UserRole newRole) {
        UserRole requiredChild = requiredChildRole(newRole);
        List<User> children = userRepository.findByReportsTo_Id(user.getId());

        if (requiredChild == null) {
            if (!children.isEmpty())
                throw new IllegalArgumentException("Cannot change this user to " + newRole
                        + " because " + children.size() + " user(s) report to them.");
            return;
        }

        for (User child : children) {
            if (child.getRole() != requiredChild)
                throw new IllegalArgumentException("Cannot change this user to " + newRole
                        + " because " + child.getFirstName() + " " + child.getLastName() 
                        + " (role " + child.getRole() + ") reports to them.");
        }
    }
}