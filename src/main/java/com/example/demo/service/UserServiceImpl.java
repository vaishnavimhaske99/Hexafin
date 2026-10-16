
package com.example.demo.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.SignupRequestDTO;
import com.example.demo.dto.UserRequestDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Admin-controlled user creation: ADMIN or SALES
    @Override
    public User createUser(UserRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException(
                "User request is required."
            );
        }

        if (request.getFirstName() == null
                || request.getFirstName().isBlank()) {
            throw new IllegalArgumentException(
                "First name is required."
            );
        }

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                "Email is required."
            );
        }

        if (request.getPassword() == null
                || request.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                "Password must contain at least 8 characters."
            );
        }

        if (request.getRole() == null
                || request.getRole().isBlank()) {
            throw new IllegalArgumentException(
                "Role is required: ADMIN or SALES."
            );
        }

        UserRole selectedRole;

        try {
            selectedRole = UserRole.valueOf(
                request.getRole().trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                "Invalid role. Allowed roles: ADMIN, SALES."
            );
        }

        if (selectedRole != UserRole.ADMIN
                && selectedRole != UserRole.SALES) {
            throw new IllegalArgumentException(
                "Only ADMIN and SALES roles are allowed."
            );
        }

        String email = request.getEmail()
            .trim()
            .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "Email is already registered."
            );
        }

        String mobile = normalizeMobile(request.getMobile());

        if (mobile != null
                && userRepository.existsByMobile(mobile)) {
            throw new IllegalArgumentException(
                "Mobile number is already registered."
            );
        }

        Role roleEntity = findRole(selectedRole);

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(normalizeName(request.getLastName()));
        user.setEmail(email);
        user.setMobile(mobile);
        user.setPassword(
            passwordEncoder.encode(request.getPassword())
        );
        user.setStatus("ACTIVE");
        user.setRole(selectedRole);
        user.setRoleEntity(roleEntity);

        return userRepository.save(user);
    }

    // Public signup: SALES only
    @Override
    public User createPublicUser(SignupRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException(
                "Signup request is required."
            );
        }

        if (request.getFirstName() == null
                || request.getFirstName().isBlank()) {
            throw new IllegalArgumentException(
                "First name is required."
            );
        }

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                "Email is required."
            );
        }

        if (request.getPassword() == null
                || request.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                "Password must contain at least 8 characters."
            );
        }

        String email = request.getEmail()
            .trim()
            .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "Email is already registered."
            );
        }

        String mobile = normalizeMobile(request.getMobile());

        if (mobile != null
                && userRepository.existsByMobile(mobile)) {
            throw new IllegalArgumentException(
                "Mobile number is already registered."
            );
        }

        // Public signup always creates a SALES user.
        // Never trust a role supplied by the public client.
        Role roleEntity = findRole(UserRole.SALES);

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(normalizeName(request.getLastName()));
        user.setEmail(email);
        user.setMobile(mobile);
        user.setPassword(
            passwordEncoder.encode(request.getPassword())
        );
        user.setStatus("ACTIVE");
        user.setRole(UserRole.SALES);
        user.setRoleEntity(roleEntity);

        return userRepository.save(user);
    }

    // Find role in the roles table
    private Role findRole(UserRole userRole) {

        return roleRepository.findByName(userRole.name())
            .orElseThrow(() -> new IllegalArgumentException(
                "Role not found in roles table: " + userRole.name()
            ));
    }

    // Normalize optional mobile number
    private String normalizeMobile(String mobile) {

        if (mobile == null || mobile.isBlank()) {
            return null;
        }

        return mobile.trim();
    }

    // Normalize optional names
    private String normalizeName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        return name.trim();
    }

    // Get all users
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                "User ID must be a positive number."
            );
        }

        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "User not found with id: " + id
            ));
    }
}
