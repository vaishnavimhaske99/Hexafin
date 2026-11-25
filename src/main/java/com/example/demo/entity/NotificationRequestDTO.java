package com.example.demo.entity;

import com.example.demo.entity.NotificationCategory;
import com.example.demo.entity.NotificationPriority;
import com.example.demo.entity.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NotificationRequestDTO {

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Notification category is required")
    private NotificationCategory category;

    private NotificationPriority priority = NotificationPriority.NORMAL;

    @NotBlank(message = "Notification title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Message cannot exceed 5000 characters")
    private String message;

    @Size(max = 50)
    private String referenceType;

    private Long referenceId;

    @Size(max = 500)
    private String actionUrl;

    public NotificationRequestDTO() {
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public void setCategory(NotificationCategory category) {
        this.category = category;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}