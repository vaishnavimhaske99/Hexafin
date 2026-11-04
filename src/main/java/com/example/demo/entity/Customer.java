package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== PERSONAL INFO (EXISTING) ====================
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "alternate_phone")
    private String alternatePhone;

    // ==================== PERSONAL INFO (NEW - KYC) ====================
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String gender;            // MALE, FEMALE, OTHER
    private String maritalStatus;     // SINGLE, MARRIED, DIVORCED, WIDOWED

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "aadhaar_number", length = 12)
    private String aadhaarNumber;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "permanent_address", columnDefinition = "TEXT")
    private String permanentAddress;

    // ==================== ADDRESS (EXISTING) ====================
    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    private String city;
    private String state;
    private String country;
    private String pincode;

    // ==================== EMPLOYMENT & INCOME (NEW) ====================
    @Column(name = "employment_type")
    private String employmentType;    // SALARIED, SELF_EMPLOYED, BUSINESS, FREELANCER

    @Column(name = "company_name")
    private String companyName;

    private String designation;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "itr_filed")
    private Boolean itrFiled;

    // ==================== LOAN DETAILS (EXISTING) ====================
    private String loanType;
    private String loanPurpose;
    private Double loanAmount;
    private Integer tenureMonths;
    private Double interestRate;

    // ==================== FINANCIAL DETAILS (EXISTING) ====================
    private Double monthlyIncome;
    private Double propertyValue;
    private Double existingEmi;
    private Integer cibilScore;

    // ==================== FINANCIAL DETAILS (NEW) ====================
    @Column(name = "other_income")
    private Double otherIncome;

    @Column(name = "annual_income")
    private Double annualIncome;

    @Column(name = "expected_emi")
    private Double expectedEmi;

    @Column(name = "down_payment")
    private Double downPayment;

    // ==================== PREVIOUS LOAN (NEW - single record) ====================
    @Column(name = "previous_loan_taken")
    private Boolean previousLoanTaken;

    @Column(name = "previous_loan_type")
    private String previousLoanType;

    @Column(name = "previous_loan_lender")
    private String previousLoanLender;

    @Column(name = "previous_loan_amount")
    private Double previousLoanAmount;

    @Column(name = "previous_loan_emi")
    private Double previousLoanEmi;

    @Column(name = "previous_loan_tenure")
    private Integer previousLoanTenure;

    @Column(name = "previous_loan_reason", columnDefinition = "TEXT")
    private String previousLoanReason;

    @Column(name = "previous_loan_status")
    private String previousLoanStatus;    // ACTIVE, CLOSED, DEFAULT, NPA

    @Column(name = "previous_repayment_history")
    private String previousRepaymentHistory; // EXCELLENT, GOOD, LATE, DEFAULTED

    // ==================== CURRENT REQUIREMENT (NEW) ====================
    private String urgency;           // LOW, MEDIUM, HIGH

    // ==================== DOCUMENTS (NEW) ====================
    @Column(name = "doc_pan_card")        private Boolean docPanCard;
    @Column(name = "doc_aadhaar_card")    private Boolean docAadhaarCard;
    @Column(name = "doc_salary_slips")    private Boolean docSalarySlips;
    @Column(name = "doc_bank_statement")  private Boolean docBankStatement;
    @Column(name = "doc_itr")             private Boolean docItr;
    @Column(name = "doc_property_papers") private Boolean docPropertyPapers;
    @Column(name = "doc_photo")           private Boolean docPhoto;

    // ==================== BANK / STATUS (EXISTING + NEW) ====================
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    private String loanStatus;        // PENDING, APPROVED, REJECTED, DISBURSED

    // ==================== CONSULTATION (NEW) ====================
    @Column(name = "lead_source")
    private String leadSource;        // WALK_IN, REFERRAL, ONLINE, CALL, CAMPAIGN

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "next_follow_up")
    private LocalDate nextFollowUp;

    // ==================== LINKS (EXISTING) ====================
    @OneToOne
    @JoinColumn(name = "source_lead_id", nullable = false, unique = true)
    private Lead sourceLead;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    // ==================== TIMESTAMPS (EXISTING) ====================
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ==================== CONSTRUCTOR ====================
    public Customer() {}

    // ==================== GETTERS & SETTERS ====================

    public Long getId() { return id; }
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

    // --- Loan details (existing) ---
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
 // ════════════════ DOCUMENT FILE PATHS (NEW) ════════════════
    @Column(name = "pan_card_url", length = 500)
    private String panCardUrl;

    @Column(name = "aadhaar_card_url", length = 500)
    private String aadhaarCardUrl;

    @Column(name = "salary_slips_url", length = 500)
    private String salarySlipsUrl;

    @Column(name = "bank_statement_url", length = 500)
    private String bankStatementUrl;

    @Column(name = "itr_url", length = 500)
    private String itrUrl;

    @Column(name = "property_papers_url", length = 500)
    private String propertyPapersUrl;

    @Column(name = "photo_doc_url", length = 500)
    private String photoDocUrl;
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

    // --- Links (existing) ---
    public Lead getSourceLead() { return sourceLead; }
    public void setSourceLead(Lead sourceLead) { this.sourceLead = sourceLead; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    // --- Timestamps (existing) ---
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}