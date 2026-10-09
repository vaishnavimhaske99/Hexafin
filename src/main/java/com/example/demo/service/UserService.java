package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.UserRequestDTO;
import com.example.demo.entity.User;

public interface UserService {

    User createUser(UserRequestDTO request);

    List<User> getAllUsers();

    User getUserById(Long id);
}