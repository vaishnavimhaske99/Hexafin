package com.example.demo.dto;

public class SalesUserLeadCountDTO {

    private Long userId;
    private String name;
    private long leadCount;

    public SalesUserLeadCountDTO() {
    }

    public SalesUserLeadCountDTO(Long userId, String name, long leadCount) {
        this.userId = userId;
        this.name = name;
        this.leadCount = leadCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLeadCount() {
        return leadCount;
    }

    public void setLeadCount(long leadCount) {
        this.leadCount = leadCount;
    }
}