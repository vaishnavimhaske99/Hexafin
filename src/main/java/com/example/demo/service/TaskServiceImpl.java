package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CreateTaskRequestDTO;
import com.example.demo.dto.TaskResponseDTO;
import com.example.demo.dto.UpdateTaskRequestDTO;
import com.example.demo.dto.UpdateTaskStatusRequestDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.Task;
import com.example.demo.entity.TaskStatus;
import com.example.demo.entity.User;
import com.example.demo.mapper.TaskMapper;
import com.example.demo.repository.LeadRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            LeadRepository leadRepository,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
    }

    // =====================================================
    // CREATE TASK
    // =====================================================

    @Override
    public TaskResponseDTO createTask(CreateTaskRequestDTO request) {

        // Find Lead
        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() ->
                        new RuntimeException("Lead not found"));

        // Find assigned User
        User assignedTo = userRepository
                .findById(request.getAssignedToId())
                .orElseThrow(() ->
                        new RuntimeException("Assigned user not found"));

        Task task = new Task();

        task.setLead(lead);

        task.setAssignedTo(assignedTo);

        /*
         * TEMPORARY:
         * JWT authentication is disabled for Task testing.
         * Therefore we cannot get createdBy from
         * SecurityContextHolder.
         *
         * For development/testing, using assignedTo
         * as createdBy.
         *
         * Later, when JWT is enabled, this will be replaced
         * with the actual logged-in User.
         */
        task.setCreatedBy(assignedTo);

        task.setTitle(request.getTitle());

        task.setDescription(request.getDescription());

        task.setTaskType(request.getTaskType());

        task.setPriority(request.getPriority());

        // New task always starts with PENDING
        task.setStatus(TaskStatus.PENDING);

        task.setDueDate(request.getDueDate());

        task.setCreatedAt(LocalDateTime.now());

        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask =
                taskRepository.save(task);

        return TaskMapper.toResponseDTO(savedTask);
    }

    // =====================================================
    // GET ALL TASKS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks() {

        return taskRepository.findAll()
                .stream()
                .map(TaskMapper::toResponseDTO)
                .toList();
    }

    // =====================================================
    // GET TASK BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        return TaskMapper.toResponseDTO(task);
    }

    // =====================================================
    // UPDATE TASK
    // =====================================================

    @Override
    public TaskResponseDTO updateTask(
            Long taskId,
            UpdateTaskRequestDTO request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        task.setTitle(request.getTitle());

        task.setDescription(request.getDescription());

        task.setTaskType(request.getTaskType());

        task.setPriority(request.getPriority());

        task.setDueDate(request.getDueDate());

        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask =
                taskRepository.save(task);

        return TaskMapper.toResponseDTO(updatedTask);
    }

    // =====================================================
    // DELETE TASK
    // =====================================================

    @Override
    public void deleteTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        taskRepository.delete(task);
    }

    // =====================================================
    // UPDATE STATUS
    // =====================================================

    @Override
    public TaskResponseDTO updateTaskStatus(
            Long taskId,
            UpdateTaskStatusRequestDTO request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        TaskStatus newStatus =
                request.getStatus();

        task.setStatus(newStatus);

        if (newStatus == TaskStatus.COMPLETED) {

            task.setCompletedAt(
                    LocalDateTime.now());

        } else {

            task.setCompletedAt(null);
        }

        task.setUpdatedAt(
                LocalDateTime.now());

        Task updatedTask =
                taskRepository.save(task);

        return TaskMapper.toResponseDTO(updatedTask);
    }

    // =====================================================
    // ASSIGN TASK
    // =====================================================

    @Override
    public TaskResponseDTO assignTask(
            Long taskId,
            Long assignedToId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        User assignedTo =
                userRepository.findById(assignedToId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        task.setAssignedTo(assignedTo);

        task.setUpdatedAt(
                LocalDateTime.now());

        Task updatedTask =
                taskRepository.save(task);

        return TaskMapper.toResponseDTO(updatedTask);
    }

    // =====================================================
    // TASKS BY LEAD
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByLead(
            Long leadId) {

        return taskRepository
                .findByLeadIdOrderByDueDateAsc(leadId)
                .stream()
                .map(TaskMapper::toResponseDTO)
                .toList();
    }

    // =====================================================
    // TASKS BY USER
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByUser(
            Long userId) {

        return taskRepository
                .findByAssignedToIdOrderByDueDateAsc(userId)
                .stream()
                .map(TaskMapper::toResponseDTO)
                .toList();
    }

    // =====================================================
    // MY TASKS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getMyTasks() {

        User loggedInUser =
                getLoggedInUser();

        return taskRepository
                .findByAssignedToIdOrderByDueDateAsc(
                        loggedInUser.getId())
                .stream()
                .map(TaskMapper::toResponseDTO)
                .toList();
    }

    // =====================================================
    // TODAY'S TASKS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTodayTasks() {

        User loggedInUser =
                getLoggedInUser();

        return taskRepository
                .findByAssignedToIdAndDueDate(
                        loggedInUser.getId(),
                        LocalDate.now())
                .stream()
                .map(TaskMapper::toResponseDTO)
                .toList();
    }

    // =====================================================
    // OVERDUE TASKS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getOverdueTasks() {

        User loggedInUser =
                getLoggedInUser();

        return taskRepository
                .findByAssignedToIdAndDueDateBeforeAndStatusNot(
                        loggedInUser.getId(),
                        LocalDate.now(),
                        TaskStatus.COMPLETED)
                .stream()
                .map(TaskMapper::toResponseDTO)
                .toList();
    }

    // =====================================================
    // GET LOGGED-IN USER
    // =====================================================

    private User getLoggedInUser() {

        var authentication =
                org.springframework.security.core.context
                        .SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not authenticated");
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof User)) {

            throw new RuntimeException(
                    "Authenticated user not found");
        }

        return (User) principal;
    }
}