package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.entity.ActivityType;

public class LeadActivityRequestDTO {

    private Long leadId;

    private ActivityType type;

    private LocalDateTime activityDateTime;

    private String description;

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public LocalDateTime getActivityDateTime() {
        return activityDateTime;
    }

    public void setActivityDateTime(LocalDateTime activityDateTime) {
        this.activityDateTime = activityDateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}