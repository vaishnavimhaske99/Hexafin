package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class UpdateUserHierarchyRequestDTO {

    @NotBlank(message = "Role is required")
    private String role;

    @Positive(message = "Supervisor ID must be positive")
    private Long reportsToId;

    public UpdateUserHierarchyRequestDTO() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getReportsToId() {
        return reportsToId;
    }

    public void setReportsToId(Long reportsToId) {
        this.reportsToId = reportsToId;
    }
}