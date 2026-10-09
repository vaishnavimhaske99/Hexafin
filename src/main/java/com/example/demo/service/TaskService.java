package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.CreateTaskRequestDTO;
import com.example.demo.dto.TaskResponseDTO;
import com.example.demo.dto.UpdateTaskRequestDTO;
import com.example.demo.dto.UpdateTaskStatusRequestDTO;

public interface TaskService {

    TaskResponseDTO createTask(CreateTaskRequestDTO request);

    List<TaskResponseDTO> getAllTasks();

    TaskResponseDTO getTaskById(Long taskId);

    TaskResponseDTO updateTask(
            Long taskId,
            UpdateTaskRequestDTO request);

    void deleteTask(Long taskId);

    TaskResponseDTO updateTaskStatus(
            Long taskId,
            UpdateTaskStatusRequestDTO request);

    TaskResponseDTO assignTask(
            Long taskId,
            Long assignedToId);

    List<TaskResponseDTO> getTasksByLead(Long leadId);

    List<TaskResponseDTO> getTasksByUser(Long userId);

    List<TaskResponseDTO> getMyTasks();

    List<TaskResponseDTO> getTodayTasks();

    List<TaskResponseDTO> getOverdueTasks();
}