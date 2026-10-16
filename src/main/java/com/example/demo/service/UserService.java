
package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.SignupRequestDTO;
import com.example.demo.dto.UserRequestDTO;
import com.example.demo.entity.User;

public interface UserService {

    // Admin-controlled user creation
    User createUser(UserRequestDTO request);

    // Public signup for SALES users only
    User createPublicUser(SignupRequestDTO request);

    // Get all users
    List<User> getAllUsers();

    // Get user by ID
    User getUserById(Long id);
}
