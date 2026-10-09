package com.example.demo.mapper;

import com.example.demo.dto.TaskResponseDTO;
import com.example.demo.entity.Task;

public class TaskMapper {

    private TaskMapper() {
    }

    public static TaskResponseDTO toResponseDTO(Task task) {

        TaskResponseDTO dto = new TaskResponseDTO();

        dto.setId(task.getId());

        // Lead
        if (task.getLead() != null) {

            dto.setLeadId(task.getLead().getId());

            String firstName = task.getLead().getFirstName() != null
                    ? task.getLead().getFirstName()
                    : "";

            String lastName = task.getLead().getLastName() != null
                    ? task.getLead().getLastName()
                    : "";

            dto.setLeadName(
                    (firstName + " " + lastName).trim()
            );
        }


        // Assigned User
        if (task.getAssignedTo() != null) {

            dto.setAssignedToId(task.getAssignedTo().getId());

            String firstName = task.getAssignedTo().getFirstName() != null
                    ? task.getAssignedTo().getFirstName()
                    : "";

            String lastName = task.getAssignedTo().getLastName() != null
                    ? task.getAssignedTo().getLastName()
                    : "";

            dto.setAssignedToName(
                    (firstName + " " + lastName).trim()
            );
        }


        // Created By
        if (task.getCreatedBy() != null) {

            dto.setCreatedById(task.getCreatedBy().getId());

            String firstName = task.getCreatedBy().getFirstName() != null
                    ? task.getCreatedBy().getFirstName()
                    : "";

            String lastName = task.getCreatedBy().getLastName() != null
                    ? task.getCreatedBy().getLastName()
                    : "";

            dto.setCreatedByName(
                    (firstName + " " + lastName).trim()
            );
        }


        // Task details
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());

        dto.setTaskType(task.getTaskType());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());

        dto.setDueDate(task.getDueDate());
        dto.setCompletedAt(task.getCompletedAt());

        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        return dto;
    }
}