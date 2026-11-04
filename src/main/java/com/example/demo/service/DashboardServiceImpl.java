package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.DashboardResponseDTO;
import com.example.demo.dto.SalesUserLeadCountDTO;
import com.example.demo.entity.LeadStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.LeadRepository;
import com.example.demo.repository.UserRepository;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(
            LeadRepository leadRepository,
            UserRepository userRepository) {

        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
    }

    // =====================================================
    // ADMIN DASHBOARD
    // =====================================================

    @Override
    public DashboardResponseDTO getAdminDashboard() {

        long totalLeads = leadRepository.count();

        long newLeads =
                leadRepository.countByStatus(LeadStatus.NEW);

        long contactedLeads =
                leadRepository.countByStatus(LeadStatus.CONTACTED);

        long qualifiedLeads =
                leadRepository.countByStatus(LeadStatus.QUALIFIED);

        long followUpLeads =
                leadRepository.countByStatus(LeadStatus.FOLLOW_UP);

        long proposalLeads =
                leadRepository.countByStatus(LeadStatus.PROPOSAL);

        long wonLeads =
                leadRepository.countByStatus(LeadStatus.WON);

        long lostLeads =
                leadRepository.countByStatus(LeadStatus.LOST);

        long assignedLeads =
                leadRepository.countByAssignedToIsNotNull();

        long unassignedLeads =
                leadRepository.countByAssignedToIsNull();


        // =================================================
        // GET ALL SALES USERS
        // =================================================

        List<User> salesUsers =
                userRepository.findByRole(UserRole.SALES);


        // =================================================
        // SALES USER-WISE LEAD COUNT
        // =================================================

        List<SalesUserLeadCountDTO> salesUserLeadCounts =
                new ArrayList<>();

        for (User user : salesUsers) {

            long leadCount =
                    leadRepository.countByAssignedTo_Id(user.getId());

            String fullName =
                    user.getFirstName() + " " + user.getLastName();

            SalesUserLeadCountDTO dto =
                    new SalesUserLeadCountDTO(
                            user.getId(),
                            fullName,
                            leadCount
                    );

            salesUserLeadCounts.add(dto);
        }


        long totalSalesUsers = salesUsers.size();


        return new DashboardResponseDTO(
                totalLeads,
                newLeads,
                contactedLeads,
                qualifiedLeads,
                followUpLeads,
                proposalLeads,
                wonLeads,
                lostLeads,
                assignedLeads,
                unassignedLeads,
                totalSalesUsers,
                salesUserLeadCounts
        );
    }


    // =====================================================
    // SALES USER DASHBOARD
    // =====================================================

    @Override
    public DashboardResponseDTO getSalesDashboard(Long userId) {

        long totalLeads =
                leadRepository.countByAssignedTo_Id(userId);

        long newLeads =
                leadRepository.countByAssignedTo_IdAndStatus(
                        userId,
                        LeadStatus.NEW
                );

        long contactedLeads =
                leadRepository.countByAssignedTo_IdAndStatus(
                        userId,
                        LeadStatus.CONTACTED
                );

        long qualifiedLeads =
                leadRepository.countByAssignedTo_IdAndStatus(
                        userId,
                        LeadStatus.QUALIFIED
                );

        long followUpLeads =
                leadRepository.countByAssignedTo_IdAndStatus(
                        userId,
                        LeadStatus.FOLLOW_UP
                );

        long proposalLeads =
                leadRepository.countByAssignedTo_IdAndStatus(
                        userId,
                        LeadStatus.PROPOSAL
                );

        long wonLeads =
                leadRepository.countByAssignedTo_IdAndStatus(
                        userId,
                        LeadStatus.WON
                );

        long lostLeads =
                leadRepository.countByAssignedTo_IdAndStatus(
                        userId,
                        LeadStatus.LOST
                );

        long assignedLeads = totalLeads;

        long unassignedLeads = 0;

        long totalSalesUsers = 0;


        // Sales dashboard ला sales-user list ची गरज नाही
        List<SalesUserLeadCountDTO> salesUserLeadCounts =
                new ArrayList<>();


        return new DashboardResponseDTO(
                totalLeads,
                newLeads,
                contactedLeads,
                qualifiedLeads,
                followUpLeads,
                proposalLeads,
                wonLeads,
                lostLeads,
                assignedLeads,
                unassignedLeads,
                totalSalesUsers,
                salesUserLeadCounts
        );
    }
}