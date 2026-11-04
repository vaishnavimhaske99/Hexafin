
package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AssignLeadRequestDTO {

    @NotNull(message = "Sales User ID is required")
    @Positive(message = "Sales User ID must be positive")
    private Long assignedToId;

    public AssignLeadRequestDTO() {
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }
}
