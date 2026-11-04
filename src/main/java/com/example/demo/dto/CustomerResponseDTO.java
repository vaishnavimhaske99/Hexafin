package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerResponseDTO {

    private Long id;

    // ════════════════ Personal (EXISTING) ════════════════
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String alternatePhone;

    // ════════════════ Personal KYC (NEW) ════════════════
    private LocalDate dateOfBirth;
    private String gender;
    private String maritalStatus;
    private String panNumber;
    private String aadhaarNumber;
    private String photoUrl;
    private String permanentAddress;

    // ════════════════ Address (EXISTING) ════════════════
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String pincode;

    // ════════════════ Employment (NEW) ════════════════
    private String employmentType;
    private String companyName;
    private String designation;
    private Integer experienceYears;
    private Boolean itrFiled;

    // ════════════════ Loan (EXISTING) ════════════════
    private String loanType;
    private String loanPurpose;
    private Double loanAmount;
    private Integer tenureMonths;
    private Double interestRate;

    // ════════════════ Financial (EXISTING) ════════════════
    private Double monthlyIncome;
    private Double propertyValue;
    private Double existingEmi;
    private Integer cibilScore;

    // ════════════════ Financial (NEW) ════════════════
    private Double otherIncome;
    private Double annualIncome;
    private Double expectedEmi;
    private Double downPayment;

    // ════════════════ Previous Loan (NEW) ════════════════
    private Boolean previousLoanTaken;
    private String previousLoanType;
    private String previousLoanLender;
    private Double previousLoanAmount;
    private Double previousLoanEmi;
    private Integer previousLoanTenure;
    private String previousLoanReason;
    private String previousLoanStatus;
    private String previousRepaymentHistory;

    // ════════════════ Current Requirement (NEW) ════════════════
    private String urgency;

    // ════════════════ Documents (NEW) ════════════════
    private Boolean docPanCard;
    private Boolean docAadhaarCard;
    private Boolean docSalarySlips;
    private Boolean docBankStatement;
    private Boolean docItr;
    private Boolean docPropertyPapers;
    private Boolean docPhoto;
 // Document file paths
    private String panCardUrl;
    private String aadhaarCardUrl;
    private String salarySlipsUrl;
    private String bankStatementUrl;
    private String itrUrl;
    private String propertyPapersUrl;
    private String photoDocUrl;

    // ════════════════ Bank (EXISTING + NEW) ════════════════
    private String bankName;
    private String accountNumber;
    private String loanStatus;

    // ════════════════ Consultation (NEW) ════════════════
    private String leadSource;
    private String notes;
    private LocalDate nextFollowUp;

    // ════════════════ Lead / Sales User (EXISTING) ════════════════
    private Long sourceLeadId;
    private String sourceLeadName;
    private Long createdById;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════════════
    //  Getters & Setters
    // ═══════════════════════════════════════════════════════════════════

    public Long getId() { return id; }
    public String getPanCardUrl() {
		return panCardUrl;
	}
	public void setPanCardUrl(String panCardUrl) {
		this.panCardUrl = panCardUrl;
	}
	public String getAadhaarCardUrl() {
		return aadhaarCardUrl;
	}
	public void setAadhaarCardUrl(String aadhaarCardUrl) {
		this.aadhaarCardUrl = aadhaarCardUrl;
	}
	public String getSalarySlipsUrl() {
		return salarySlipsUrl;
	}
	public void setSalarySlipsUrl(String salarySlipsUrl) {
		this.salarySlipsUrl = salarySlipsUrl;
	}
	public String getBankStatementUrl() {
		return bankStatementUrl;
	}
	public void setBankStatementUrl(String bankStatementUrl) {
		this.bankStatementUrl = bankStatementUrl;
	}
	public String getItrUrl() {
		return itrUrl;
	}
	public void setItrUrl(String itrUrl) {
		this.itrUrl = itrUrl;
	}
	public String getPropertyPapersUrl() {
		return propertyPapersUrl;
	}
	public void setPropertyPapersUrl(String propertyPapersUrl) {
		this.propertyPapersUrl = propertyPapersUrl;
	}
	public String getPhotoDocUrl() {
		return photoDocUrl;
	}
	public void setPhotoDocUrl(String photoDocUrl) {
		this.photoDocUrl = photoDocUrl;
	}
	public void setId(Long id) { this.id = id; }

    // --- Personal (existing) ---
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }

    // --- Personal KYC (new) ---
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }

    // --- Address (existing) ---
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    // --- Employment (new) ---
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public Boolean getItrFiled() { return itrFiled; }
    public void setItrFiled(Boolean itrFiled) { this.itrFiled = itrFiled; }

    // --- Loan (existing) ---
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public String getLoanPurpose() { return loanPurpose; }
    public void setLoanPurpose(String loanPurpose) { this.loanPurpose = loanPurpose; }

    public Double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(Double loanAmount) { this.loanAmount = loanAmount; }

    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }

    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }

    // --- Financial (existing) ---
    public Double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(Double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public Double getPropertyValue() { return propertyValue; }
    public void setPropertyValue(Double propertyValue) { this.propertyValue = propertyValue; }

    public Double getExistingEmi() { return existingEmi; }
    public void setExistingEmi(Double existingEmi) { this.existingEmi = existingEmi; }

    public Integer getCibilScore() { return cibilScore; }
    public void setCibilScore(Integer cibilScore) { this.cibilScore = cibilScore; }

    // --- Financial (new) ---
    public Double getOtherIncome() { return otherIncome; }
    public void setOtherIncome(Double otherIncome) { this.otherIncome = otherIncome; }

    public Double getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(Double annualIncome) { this.annualIncome = annualIncome; }

    public Double getExpectedEmi() { return expectedEmi; }
    public void setExpectedEmi(Double expectedEmi) { this.expectedEmi = expectedEmi; }

    public Double getDownPayment() { return downPayment; }
    public void setDownPayment(Double downPayment) { this.downPayment = downPayment; }

    // --- Previous loan (new) ---
    public Boolean getPreviousLoanTaken() { return previousLoanTaken; }
    public void setPreviousLoanTaken(Boolean previousLoanTaken) { this.previousLoanTaken = previousLoanTaken; }

    public String getPreviousLoanType() { return previousLoanType; }
    public void setPreviousLoanType(String previousLoanType) { this.previousLoanType = previousLoanType; }

    public String getPreviousLoanLender() { return previousLoanLender; }
    public void setPreviousLoanLender(String previousLoanLender) { this.previousLoanLender = previousLoanLender; }

    public Double getPreviousLoanAmount() { return previousLoanAmount; }
    public void setPreviousLoanAmount(Double previousLoanAmount) { this.previousLoanAmount = previousLoanAmount; }

    public Double getPreviousLoanEmi() { return previousLoanEmi; }
    public void setPreviousLoanEmi(Double previousLoanEmi) { this.previousLoanEmi = previousLoanEmi; }

    public Integer getPreviousLoanTenure() { return previousLoanTenure; }
    public void setPreviousLoanTenure(Integer previousLoanTenure) { this.previousLoanTenure = previousLoanTenure; }

    public String getPreviousLoanReason() { return previousLoanReason; }
    public void setPreviousLoanReason(String previousLoanReason) { this.previousLoanReason = previousLoanReason; }

    public String getPreviousLoanStatus() { return previousLoanStatus; }
    public void setPreviousLoanStatus(String previousLoanStatus) { this.previousLoanStatus = previousLoanStatus; }

    public String getPreviousRepaymentHistory() { return previousRepaymentHistory; }
    public void setPreviousRepaymentHistory(String previousRepaymentHistory) { this.previousRepaymentHistory = previousRepaymentHistory; }

    // --- Current requirement (new) ---
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    // --- Documents (new) ---
    public Boolean getDocPanCard() { return docPanCard; }
    public void setDocPanCard(Boolean docPanCard) { this.docPanCard = docPanCard; }

    public Boolean getDocAadhaarCard() { return docAadhaarCard; }
    public void setDocAadhaarCard(Boolean docAadhaarCard) { this.docAadhaarCard = docAadhaarCard; }

    public Boolean getDocSalarySlips() { return docSalarySlips; }
    public void setDocSalarySlips(Boolean docSalarySlips) { this.docSalarySlips = docSalarySlips; }

    public Boolean getDocBankStatement() { return docBankStatement; }
    public void setDocBankStatement(Boolean docBankStatement) { this.docBankStatement = docBankStatement; }

    public Boolean getDocItr() { return docItr; }
    public void setDocItr(Boolean docItr) { this.docItr = docItr; }

    public Boolean getDocPropertyPapers() { return docPropertyPapers; }
    public void setDocPropertyPapers(Boolean docPropertyPapers) { this.docPropertyPapers = docPropertyPapers; }

    public Boolean getDocPhoto() { return docPhoto; }
    public void setDocPhoto(Boolean docPhoto) { this.docPhoto = docPhoto; }

    // --- Bank (existing + new) ---
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getLoanStatus() { return loanStatus; }
    public void setLoanStatus(String loanStatus) { this.loanStatus = loanStatus; }

    // --- Consultation (new) ---
    public String getLeadSource() { return leadSource; }
    public void setLeadSource(String leadSource) { this.leadSource = leadSource; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getNextFollowUp() { return nextFollowUp; }
    public void setNextFollowUp(LocalDate nextFollowUp) { this.nextFollowUp = nextFollowUp; }

    // --- Lead / User (existing) ---
    public Long getSourceLeadId() { return sourceLeadId; }
    public void setSourceLeadId(Long sourceLeadId) { this.sourceLeadId = sourceLeadId; }

    public String getSourceLeadName() { return sourceLeadName; }
    public void setSourceLeadName(String sourceLeadName) { this.sourceLeadName = sourceLeadName; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    // --- Timestamps ---
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}