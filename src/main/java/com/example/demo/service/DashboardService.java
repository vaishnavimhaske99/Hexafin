package com.example.demo.service;

import com.example.demo.dto.DashboardResponseDTO;

public interface DashboardService {

    DashboardResponseDTO getAdminDashboard();

    DashboardResponseDTO getSalesDashboard(Long userId);
}