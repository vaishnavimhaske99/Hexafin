package com.example.demo.mapper;

import com.example.demo.dto.LeadActivityRequestDTO;
import com.example.demo.dto.LeadActivityResponseDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadActivity;
import com.example.demo.entity.User;

public class LeadActivityMapper {

    public static LeadActivity toEntity(
            LeadActivityRequestDTO request,
            Lead lead) {

        LeadActivity activity = new LeadActivity();

        activity.setLead(lead);
        activity.setType(request.getType());
        activity.setActivityDateTime(
                request.getActivityDateTime());
        activity.setDescription(
                request.getDescription());

        return activity;
    }

    public static LeadActivityResponseDTO toResponseDTO(
            LeadActivity activity) {

        LeadActivityResponseDTO response =
                new LeadActivityResponseDTO();

        response.setId(activity.getId());
        response.setLeadId(activity.getLead().getId());
        response.setType(activity.getType());
        response.setActivityDateTime(
                activity.getActivityDateTime());
        response.setDescription(
                activity.getDescription());
        response.setCreatedAt(
                activity.getCreatedAt());

        User createdBy = activity.getCreatedBy();

        if (createdBy != null) {
            response.setCreatedById(createdBy.getId());

            response.setCreatedByName(
                    createdBy.getFirstName()
                    + " "
                    + createdBy.getLastName());
        }

        return response;
    }
}