package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Task;
import com.example.demo.entity.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByLeadIdOrderByDueDateAsc(Long leadId);

    List<Task> findByAssignedToIdOrderByDueDateAsc(Long userId);

    List<Task> findByAssignedToIdAndStatusOrderByDueDateAsc(
            Long userId,
            TaskStatus status);

    List<Task> findByDueDate(LocalDate dueDate);

    List<Task> findByAssignedToIdAndDueDate(
            Long userId,
            LocalDate dueDate);

    List<Task> findByAssignedToIdAndDueDateBeforeAndStatusNot(
            Long userId,
            LocalDate date,
            TaskStatus status);
}