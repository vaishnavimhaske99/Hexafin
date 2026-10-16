package com.example.demo.dto;

import com.example.demo.entity.UserRole;

public class UserProfileResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String status;
    private UserRole role;

    public UserProfileResponseDTO() {
    }

    public UserProfileResponseDTO(
            Long id,
            String firstName,
            String lastName,
            String email,
            String mobile,
            String status,
            UserRole role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.status = status;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getStatus() { return status; }
    public UserRole getRole() { return role; }
}