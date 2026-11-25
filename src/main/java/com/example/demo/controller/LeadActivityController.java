
package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LeadActivityRequestDTO;
import com.example.demo.dto.LeadActivityResponseDTO;
import com.example.demo.service.LeadActivityService;

@RestController
@RequestMapping("/api/leads")
public class LeadActivityController {

    private final LeadActivityService leadActivityService;

    public LeadActivityController(
            LeadActivityService leadActivityService) {
        this.leadActivityService = leadActivityService;
    }

    // CREATE ACTIVITY
    @PostMapping("/{leadId}/activities")
    public LeadActivityResponseDTO createActivity(
            @PathVariable Long leadId,
            @RequestBody LeadActivityRequestDTO request) {

        return leadActivityService.createActivity(leadId, request);
    }

    // GET ACTIVITIES BY LEAD ID
    @GetMapping("/{leadId}/activities")
    public List<LeadActivityResponseDTO> getActivitiesByLeadId(
            @PathVariable Long leadId) {

        return leadActivityService.getActivitiesByLeadId(leadId);
    }

    // GET TODAY'S ACTIVITIES OF ALL USERS
    @GetMapping("/activities/today")
    public List<LeadActivityResponseDTO> getTodaysActivities() {

        return leadActivityService.getTodaysActivities();
    }

    // GET ACTIVITY BY ID (NUMERIC ID ONLY)
    @GetMapping("/activities/{id:\\d+}")
    public LeadActivityResponseDTO getActivityById(
            @PathVariable Long id) {

        return leadActivityService.getActivityById(id);
    }

    // UPDATE ACTIVITY (NUMERIC ID ONLY)
    @PutMapping("/activities/{id:\\d+}")
    public LeadActivityResponseDTO updateActivity(
            @PathVariable Long id,
            @RequestBody LeadActivityRequestDTO request) {

        return leadActivityService.updateActivity(id, request);
    }

    // DELETE ACTIVITY (NUMERIC ID ONLY)
    @DeleteMapping("/activities/{id:\\d+}")
    public void deleteActivity(
            @PathVariable Long id) {

        leadActivityService.deleteActivity(id);
    }
}
