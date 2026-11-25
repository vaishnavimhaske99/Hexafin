
package com.example.demo.service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.example.demo.dto.QuotationRequestDTO;
import com.example.demo.dto.QuotationResponseDTO;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Lead;
import com.example.demo.entity.Quotation;
import com.example.demo.entity.QuotationStatus;
import com.example.demo.mapper.QuotationMapper;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.LeadRepository;
import com.example.demo.repository.QuotationRepository;

@Service
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;

    public QuotationServiceImpl(
            QuotationRepository quotationRepository,
            LeadRepository leadRepository,
            CustomerRepository customerRepository) {

        this.quotationRepository = quotationRepository;
        this.leadRepository = leadRepository;
        this.customerRepository = customerRepository;
    }

    // =====================================================
    // CREATE LEAD QUOTATION
    // =====================================================

    @Override
    @Transactional
    public QuotationResponseDTO createQuotation(
            Long leadId,
            QuotationRequestDTO request) {

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException(
                        "Lead not found with id: " + leadId));

        if (request.getQuotationNumber() != null
                && quotationRepository
                        .findByQuotationNumber(request.getQuotationNumber())
                        .isPresent()) {

            throw new RuntimeException(
                    "Quotation number already exists: "
                            + request.getQuotationNumber());
        }

        Quotation quotation =
                QuotationMapper.toEntity(request, lead);

        if (quotation.getQuotationDate() == null) {
            quotation.setQuotationDate(LocalDate.now());
        }

        if (quotation.getStatus() == null) {
            quotation.setStatus(QuotationStatus.DRAFT);
        }

        if (quotation.getRevisionNumber() == null) {
            quotation.setRevisionNumber(1);
        }

        LocalDateTime now = LocalDateTime.now();

        quotation.setCreatedAt(now);
        quotation.setUpdatedAt(now);

        Quotation savedQuotation =
                quotationRepository.save(quotation);

        return QuotationMapper.toResponseDTO(savedQuotation);
    }

    // =====================================================
    // GET LEAD QUOTATIONS
    // =====================================================

    @Override
    public List<QuotationResponseDTO> getQuotationsByLeadId(
            Long leadId) {

        if (!leadRepository.existsById(leadId)) {
            throw new RuntimeException(
                    "Lead not found with id: " + leadId);
        }

        return quotationRepository.findByLeadId(leadId)
                .stream()
                .map(QuotationMapper::toResponseDTO)
                .toList();
    }

    // =====================================================
    // GET QUOTATION BY ID
    // =====================================================

    @Override
    public QuotationResponseDTO getQuotationById(
            Long quotationId) {

        Quotation quotation =
                quotationRepository.findById(quotationId)
                        .orElseThrow(() -> new RuntimeException(
                                "Quotation not found with id: "
                                        + quotationId));

        return QuotationMapper.toResponseDTO(quotation);
    }

    // =====================================================
    // UPDATE QUOTATION
    // =====================================================

    @Override
    @Transactional
    public QuotationResponseDTO updateQuotation(
            Long quotationId,
            QuotationRequestDTO request) {

        Quotation quotation =
                quotationRepository.findById(quotationId)
                        .orElseThrow(() -> new RuntimeException(
                                "Quotation not found with id: "
                                        + quotationId));

        if (request.getQuotationNumber() != null
                && !request.getQuotationNumber()
                        .equals(quotation.getQuotationNumber())) {

            if (quotationRepository
                    .findByQuotationNumber(
                            request.getQuotationNumber())
                    .isPresent()) {

                throw new RuntimeException(
                        "Quotation number already exists: "
                                + request.getQuotationNumber());
            }

            quotation.setQuotationNumber(
                    request.getQuotationNumber());
        }

        quotation.setQuotationDate(request.getQuotationDate());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setStatus(request.getStatus());
        quotation.setRevisionNumber(request.getRevisionNumber());

        quotation.setLoanType(request.getLoanType());
        quotation.setLoanAmount(request.getLoanAmount());
        quotation.setInterestRate(request.getInterestRate());
        quotation.setTenureMonths(request.getTenureMonths());
        quotation.setLoanPurpose(request.getLoanPurpose());
        quotation.setLenderName(request.getLenderName());

        quotation.setProcessingFee(request.getProcessingFee());
        quotation.setOtherCharges(request.getOtherCharges());
        quotation.setDiscountPercent(request.getDiscountPercent());
        quotation.setDiscountAmount(request.getDiscountAmount());
        quotation.setTaxableAmount(request.getTaxableAmount());
        quotation.setTaxPercent(request.getTaxPercent());
        quotation.setTaxAmount(request.getTaxAmount());
        quotation.setGrandTotal(request.getGrandTotal());

        quotation.setPaymentTerms(request.getPaymentTerms());
        quotation.setTermsAndConditions(request.getTermsAndConditions());
        quotation.setNotes(request.getNotes());

        quotation.setUpdatedAt(LocalDateTime.now());

        Quotation updatedQuotation =
                quotationRepository.save(quotation);

        return QuotationMapper.toResponseDTO(updatedQuotation);
    }

    // =====================================================
    // CREATE CUSTOMER QUOTATION
    // =====================================================

    @Override
    @Transactional
    public QuotationResponseDTO createCustomerQuotation(
            Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException(
                        "Customer not found with id: " + customerId));

        if (customer.getLoanType() == null
                || customer.getLoanType().isBlank()) {
            throw new RuntimeException(
                    "Customer loan type is required");
        }

        if (customer.getLoanAmount() == null
                || !Double.isFinite(customer.getLoanAmount())
                || customer.getLoanAmount() <= 0) {
            throw new RuntimeException(
                    "Valid loan amount is required");
        }

        if (customer.getInterestRate() == null
                || !Double.isFinite(customer.getInterestRate())
                || customer.getInterestRate() < 0) {
            throw new RuntimeException(
                    "Valid interest rate is required");
        }

        if (customer.getTenureMonths() == null
                || customer.getTenureMonths() <= 0) {
            throw new RuntimeException(
                    "Valid loan tenure is required");
        }

        List<Quotation> existingQuotations =
                quotationRepository.findByCustomerId(customerId);

        if (!existingQuotations.isEmpty()) {
            throw new RuntimeException(
                    "Quotation already exists for customer id: "
                            + customerId);
        }

        String quotationNumber = "CUST-QTN-"
                + LocalDate.now().toString().replace("-", "")
                + "-"
                + UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase();

        Quotation quotation = new Quotation();

        quotation.setCustomer(customer);
        quotation.setQuotationNumber(quotationNumber);
        quotation.setQuotationDate(LocalDate.now());
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setRevisionNumber(1);

        quotation.setLoanType(customer.getLoanType());
        quotation.setLoanAmount(
                BigDecimal.valueOf(customer.getLoanAmount()));
        quotation.setInterestRate(
                BigDecimal.valueOf(customer.getInterestRate()));
        quotation.setTenureMonths(customer.getTenureMonths());
        quotation.setLoanPurpose(customer.getLoanPurpose());

        LocalDateTime now = LocalDateTime.now();

        quotation.setCreatedAt(now);
        quotation.setUpdatedAt(now);

        Quotation savedQuotation =
                quotationRepository.save(quotation);

        return QuotationMapper.toResponseDTO(savedQuotation);
    }

    // =====================================================
    // GENERATE CUSTOMER QUOTATION PDF
    // =====================================================

    @Override
    @Transactional
    public QuotationResponseDTO generateCustomerQuotationPdf(
            Long quotationId) {

        Quotation quotation =
                quotationRepository.findById(quotationId)
                        .orElseThrow(() -> new RuntimeException(
                                "Quotation not found with id: "
                                        + quotationId));

        Customer customer = quotation.getCustomer();

        if (customer == null) {
            throw new RuntimeException(
                    "This quotation is not linked to a customer");
        }

        Path pdfPath = null;

        try {

            Path directory = Paths.get(
                    "uploads",
                    "quotations",
                    "customers",
                    customer.getId().toString());

            Files.createDirectories(directory);

            String safeQuotationNumber =
                    quotation.getQuotationNumber()
                            .replaceAll("[^a-zA-Z0-9_-]", "_");

            pdfPath = directory.resolve(
                    safeQuotationNumber + ".pdf");

            Document document = new Document();

            try (OutputStream outputStream =
                         Files.newOutputStream(pdfPath)) {

                PdfWriter.getInstance(document, outputStream);

                document.open();

                Font titleFont = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD, 18);

                Font headingFont = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD, 12);

                Font normalFont = FontFactory.getFont(
                        FontFactory.HELVETICA, 10);

                Paragraph title = new Paragraph(
                        "LOAN QUOTATION", titleFont);

                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                document.add(new Paragraph(" "));

                document.add(new Paragraph(
                        "Quotation Number: "
                                + quotation.getQuotationNumber(),
                        normalFont));

                document.add(new Paragraph(
                        "Quotation Date: "
                                + quotation.getQuotationDate(),
                        normalFont));

                document.add(new Paragraph(
                        "Status: " + quotation.getStatus(),
                        normalFont));

                if (quotation.getValidUntil() != null) {
                    document.add(new Paragraph(
                            "Valid Until: "
                                    + quotation.getValidUntil(),
                            normalFont));
                }

                document.add(new Paragraph(" "));

                document.add(new Paragraph(
                        "CUSTOMER INFORMATION", headingFont));

                document.add(new Paragraph(
                        "Customer ID: " + customer.getId(),
                        normalFont));

                document.add(new Paragraph(" "));

                document.add(new Paragraph(
                        "LOAN DETAILS", headingFont));

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);

                table.addCell("Loan Type");
                table.addCell(
                        quotation.getLoanType() != null
                                ? quotation.getLoanType() : "-");

                table.addCell("Loan Amount");
                table.addCell(
                        formatAmount(quotation.getLoanAmount()));

                table.addCell("Interest Rate");
                table.addCell(
                        quotation.getInterestRate() != null
                                ? quotation.getInterestRate().toPlainString()
                                        + "%"
                                : "-");

                table.addCell("Tenure");
                table.addCell(
                        quotation.getTenureMonths() != null
                                ? quotation.getTenureMonths()
                                        + " Months"
                                : "-");

                table.addCell("Loan Purpose");
                table.addCell(
                        quotation.getLoanPurpose() != null
                                ? quotation.getLoanPurpose() : "-");

                table.addCell("Lender Name");
                table.addCell(
                        quotation.getLenderName() != null
                                ? quotation.getLenderName() : "-");

                document.add(table);

                document.add(new Paragraph(" "));

                document.add(new Paragraph(
                        "CHARGES & TOTAL", headingFont));

                PdfPTable chargesTable = new PdfPTable(2);
                chargesTable.setWidthPercentage(100);

                chargesTable.addCell("Processing Fee");
                chargesTable.addCell(
                        formatAmount(quotation.getProcessingFee()));

                chargesTable.addCell("Other Charges");
                chargesTable.addCell(
                        formatAmount(quotation.getOtherCharges()));

                chargesTable.addCell("Discount");
                chargesTable.addCell(
                        formatAmount(quotation.getDiscountAmount()));

                chargesTable.addCell("Tax");
                chargesTable.addCell(
                        formatAmount(quotation.getTaxAmount()));

                chargesTable.addCell("Grand Total");
                chargesTable.addCell(
                        formatAmount(quotation.getGrandTotal()));

                document.add(chargesTable);

                document.add(new Paragraph(" "));

                if (quotation.getPaymentTerms() != null) {
                    document.add(new Paragraph(
                            "PAYMENT TERMS", headingFont));

                    document.add(new Paragraph(
                            quotation.getPaymentTerms(), normalFont));

                    document.add(new Paragraph(" "));
                }

                if (quotation.getTermsAndConditions() != null) {
                    document.add(new Paragraph(
                            "TERMS & CONDITIONS", headingFont));

                    document.add(new Paragraph(
                            quotation.getTermsAndConditions(),
                            normalFont));

                    document.add(new Paragraph(" "));
                }

                if (quotation.getNotes() != null) {
                    document.add(new Paragraph(
                            "NOTES", headingFont));

                    document.add(new Paragraph(
                            quotation.getNotes(), normalFont));
                }

                document.close();
            }

            quotation.setPdfFilePath(
                    pdfPath.toString().replace("\\", "/"));

            quotation.setPdfGeneratedAt(LocalDateTime.now());
            quotation.setUpdatedAt(LocalDateTime.now());

            Quotation savedQuotation =
                    quotationRepository.save(quotation);

            return QuotationMapper.toResponseDTO(savedQuotation);

        } catch (Exception e) {

            if (pdfPath != null) {
                try {
                    Files.deleteIfExists(pdfPath);
                } catch (Exception ignored) {
                    // Ignore cleanup failure
                }
            }

            throw new RuntimeException(
                    "Failed to generate quotation PDF: "
                            + e.getMessage(), e);
        }
    }

    // =====================================================
    // FORMAT AMOUNT
    // =====================================================

    private String formatAmount(BigDecimal amount) {

        return amount != null
                ? "INR " + amount.setScale(
                        2, RoundingMode.HALF_UP).toPlainString()
                : "INR 0.00";
    }

    // =====================================================
    // DELETE QUOTATION
    // =====================================================

    @Override
    @Transactional
    public void deleteQuotation(Long quotationId) {

        Quotation quotation =
                quotationRepository.findById(quotationId)
                        .orElseThrow(() -> new RuntimeException(
                                "Quotation not found with id: "
                                        + quotationId));

        quotationRepository.delete(quotation);
    }

    // =====================================================
    // GET CUSTOMER QUOTATIONS
    // =====================================================

    @Override
    public List<QuotationResponseDTO> getQuotationsByCustomerId(
            Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new RuntimeException(
                    "Customer not found with id: " + customerId);
        }

        return quotationRepository.findByCustomerId(customerId)
                .stream()
                .map(QuotationMapper::toResponseDTO)
                .toList();
    }
}
