
package com.example.demo.dto;

import java.time.LocalDateTime;
import com.example.demo.entity.ActivityType;

public class LeadActivityResponseDTO {

    private Long id;
    private Long leadId;
    private String leadName;
    private ActivityType type;
    private String description;
    private Long userId;
    private String userName;
    private LocalDateTime activityDateTime;
    private LocalDateTime createdAt;

    public LeadActivityResponseDTO() {
    }

    // GETTERS

    public Long getId() {
        return id;
    }

    public Long getLeadId() {
        return leadId;
    }

    public String getLeadName() {
        return leadName;
    }

    public ActivityType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public LocalDateTime getActivityDateTime() {
        return activityDateTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // SETTERS

    public void setId(Long id) {
        this.id = id;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public void setLeadName(String leadName) {
        this.leadName = leadName;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setActivityDateTime(LocalDateTime activityDateTime) {
        this.activityDateTime = activityDateTime;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Compatibility with Mapper naming
    public void setCreatedById(Long createdById) {
        this.userId = createdById;
    }

    public void setCreatedByName(String createdByName) {
        this.userName = createdByName;
    }

    public Long getCreatedById() {
        return userId;
    }

    public String getCreatedByName() {
        return userName;
    }
}
