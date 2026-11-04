
package com.example.demo.dto;

public class SalesUserResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String status;
    private long assignedLeadCount;

    public SalesUserResponseDTO(
            Long id,
            String firstName,
            String lastName,
            String email,
            String mobile,
            String status,
            long assignedLeadCount) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.status = status;
        this.assignedLeadCount = assignedLeadCount;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getStatus() {
        return status;
    }

    public long getAssignedLeadCount() {
        return assignedLeadCount;
    }
}
