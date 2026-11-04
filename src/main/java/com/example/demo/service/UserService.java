package com.example.demo.service;

import java.util.List;
import com.example.demo.dto.SignupRequestDTO;
import com.example.demo.dto.UpdateUserHierarchyRequestDTO;
import com.example.demo.dto.UserRequestDTO;
import com.example.demo.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO request);
    UserResponseDTO createPublicUser(SignupRequestDTO request);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    List<UserResponseDTO> findByReportsToId(Long reportsToId);
    List<UserResponseDTO> getManagerTeam();
    UserResponseDTO updateUserHierarchy(Long userId, UpdateUserHierarchyRequestDTO request);
}