package com.example.demo.dto;

import java.util.List;

public class DashboardResponseDTO {

    private long totalLeads;
    private long newLeads;
    private long contactedLeads;
    private long qualifiedLeads;
    private long followUpLeads;
    private long proposalLeads;
    private long wonLeads;
    private long lostLeads;
    private long assignedLeads;
    private long unassignedLeads;
    private long totalSalesUsers;

    private List<SalesUserLeadCountDTO> salesUsers;

    public DashboardResponseDTO() {
    }

    public DashboardResponseDTO(
            long totalLeads,
            long newLeads,
            long contactedLeads,
            long qualifiedLeads,
            long followUpLeads,
            long proposalLeads,
            long wonLeads,
            long lostLeads,
            long assignedLeads,
            long unassignedLeads,
            long totalSalesUsers,
            List<SalesUserLeadCountDTO> salesUsers) {

        this.totalLeads = totalLeads;
        this.newLeads = newLeads;
        this.contactedLeads = contactedLeads;
        this.qualifiedLeads = qualifiedLeads;
        this.followUpLeads = followUpLeads;
        this.proposalLeads = proposalLeads;
        this.wonLeads = wonLeads;
        this.lostLeads = lostLeads;
        this.assignedLeads = assignedLeads;
        this.unassignedLeads = unassignedLeads;
        this.totalSalesUsers = totalSalesUsers;
        this.salesUsers = salesUsers;
    }

    public long getTotalLeads() {
        return totalLeads;
    }

    public void setTotalLeads(long totalLeads) {
        this.totalLeads = totalLeads;
    }

    public long getNewLeads() {
        return newLeads;
    }

    public void setNewLeads(long newLeads) {
        this.newLeads = newLeads;
    }

    public long getContactedLeads() {
        return contactedLeads;
    }

    public void setContactedLeads(long contactedLeads) {
        this.contactedLeads = contactedLeads;
    }

    public long getQualifiedLeads() {
        return qualifiedLeads;
    }

    public void setQualifiedLeads(long qualifiedLeads) {
        this.qualifiedLeads = qualifiedLeads;
    }

    public long getFollowUpLeads() {
        return followUpLeads;
    }

    public void setFollowUpLeads(long followUpLeads) {
        this.followUpLeads = followUpLeads;
    }

    public long getProposalLeads() {
        return proposalLeads;
    }

    public void setProposalLeads(long proposalLeads) {
        this.proposalLeads = proposalLeads;
    }

    public long getWonLeads() {
        return wonLeads;
    }

    public void setWonLeads(long wonLeads) {
        this.wonLeads = wonLeads;
    }

    public long getLostLeads() {
        return lostLeads;
    }

    public void setLostLeads(long lostLeads) {
        this.lostLeads = lostLeads;
    }

    public long getAssignedLeads() {
        return assignedLeads;
    }

    public void setAssignedLeads(long assignedLeads) {
        this.assignedLeads = assignedLeads;
    }

    public long getUnassignedLeads() {
        return unassignedLeads;
    }

    public void setUnassignedLeads(long unassignedLeads) {
        this.unassignedLeads = unassignedLeads;
    }

    public long getTotalSalesUsers() {
        return totalSalesUsers;
    }

    public void setTotalSalesUsers(long totalSalesUsers) {
        this.totalSalesUsers = totalSalesUsers;
    }

    public List<SalesUserLeadCountDTO> getSalesUsers() {
        return salesUsers;
    }

    public void setSalesUsers(List<SalesUserLeadCountDTO> salesUsers) {
        this.salesUsers = salesUsers;
    }
}