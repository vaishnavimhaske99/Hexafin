
package com.example.demo.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.LeadImportPreviewDTO;
import com.example.demo.dto.LeadImportResultDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadImportBatch;
import com.example.demo.entity.LeadImportStatus;
import com.example.demo.entity.LeadSource;
import com.example.demo.entity.LeadStatus;
import com.example.demo.repository.LeadRepository;
import com.example.demo.repository.LeadImportBatchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SmartLeadImportServiceImpl implements SmartLeadImportService {

    private final LeadRepository leadRepository;
    private final LeadImportBatchRepository batchRepository;
    private final ObjectMapper objectMapper;

    public SmartLeadImportServiceImpl(
            LeadRepository leadRepository,
            LeadImportBatchRepository batchRepository,
            ObjectMapper objectMapper) {

        this.leadRepository = leadRepository;
        this.batchRepository = batchRepository;
        this.objectMapper = objectMapper;
    }

    // ================= HEADER NORMALIZATION =================

    private String normalizeHeader(String header) {

        if (header == null) {
            return "";
        }

        return header.replace("\uFEFF", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    // ================= AUTOMATIC COLUMN MAPPING =================

    private String identifyColumn(String header) {

        String key = normalizeHeader(header);

        if (key.isBlank()) {
            return null;
        }

        if (List.of(
                "company", "companyname", "businessname",
                "organization", "organisation",
                "organizationname", "organisationname",
                "firm", "firmname", "business"
        ).contains(key)) {
            return "companyName";
        }

        if (List.of(
                "name", "fullname", "customername",
                "contactname", "leadname", "legalname",
                "applicantname", "borrowername",
                "clientname", "personname",
                "individualname", "proprietorname",
                "ownername", "contactperson",
                "customerfullname", "applicantfullname",
                "borrowerfullname", "clientfullname"
        ).contains(key)) {
            return "fullName";
        }

        if (List.of(
                "firstname", "first", "givenname",
                "fname", "forename"
        ).contains(key)) {
            return "firstName";
        }

        if (List.of(
                "lastname", "last", "surname",
                "familyname", "lname"
        ).contains(key)) {
            return "lastName";
        }

        if (List.of(
                "mobile", "mobileno", "mobilenumber",
                "mob", "mobno", "mobnum", "mobnumber",
                "mobilenum", "phone", "phoneno",
                "phonenumber", "contactnumber",
                "contactno", "contactnum",
                "whatsapp", "whatsappno",
                "whatsappnumber", "cell", "cellphone",
                "telephone", "telephoneno",
                "primarymobile", "primaryphone",
                "alternatemobile", "alternatenumber"
        ).contains(key)
                || key.contains("mobile")
                || key.contains("phone")
                || key.contains("telephone")
                || key.startsWith("mob")
                || key.endsWith("contactnumber")
                || key.endsWith("contactno")) {

            return "phone";
        }

        if (List.of(
                "email", "emailid", "emailaddress",
                "mail", "mailid", "emailaddr",
                "primaryemail", "contactemail"
        ).contains(key) || key.contains("email")) {
            return "email";
        }

        if (List.of(
                "address", "addressline1",
                "streetaddress", "residentialaddress",
                "officeaddress", "permanentaddress"
        ).contains(key)) {
            return "addressLine1";
        }

        if (List.of(
                "addressline2", "landmark",
                "area", "locality"
        ).contains(key)) {
            return "addressLine2";
        }

        if (List.of("city", "town").contains(key)) {
            return "city";
        }

        if (List.of("state", "province").contains(key)) {
            return "state";
        }

        if (List.of("country", "nation").contains(key)) {
            return "country";
        }

        if (List.of(
                "pincode", "postalcode",
                "zipcode", "zip", "postcode"
        ).contains(key)) {
            return "pincode";
        }

        if (List.of(
                "source", "leadsource",
                "enquirysource", "referencesource"
        ).contains(key)) {
            return "source";
        }

        if (List.of(
                "status", "leadstatus",
                "enquirystatus"
        ).contains(key)) {
            return "status";
        }

        if (List.of(
                "description", "remarks", "notes",
                "comments", "remark", "comment",
                "requirement", "customerrequirement"
        ).contains(key)) {
            return "description";
        }

        return null;
    }

    // ================= READ EXCEL / CSV =================

    private List<Map<String, String>> readFile(MultipartFile file) {

        List<Map<String, String>> rows = new ArrayList<>();

        try {

            String filename = file.getOriginalFilename();

            if (filename == null || !filename.contains(".")) {
                throw new RuntimeException("Invalid file name.");
            }

            String extension = filename.substring(
                    filename.lastIndexOf('.') + 1)
                    .toLowerCase(Locale.ROOT);

            // ================= CSV =================

            if (extension.equals("csv")) {

                try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    file.getInputStream(),
                                    StandardCharsets.UTF_8));

                    CSVParser parser = CSVFormat.DEFAULT.builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .setIgnoreEmptyLines(true)
                            .get()
                            .parse(reader)
                ) {

                    List<String> headers = parser.getHeaderNames();

                    for (CSVRecord record : parser) {

                        Map<String, String> row = new LinkedHashMap<>();

                        for (int i = 0; i < headers.size(); i++) {

                            String header = headers.get(i);

                            if (header == null || header.isBlank()) {
                                header = "Column_" + (i + 1);
                            }

                            String value = i < record.size()
                                    ? record.get(i)
                                    : "";

                            row.put(header, value);
                        }

                        rows.add(row);
                    }
                }
            }

            // ================= EXCEL =================

            else if (extension.equals("xlsx")
                    || extension.equals("xls")) {

                try (
                    InputStream inputStream = file.getInputStream();
                    Workbook workbook = WorkbookFactory.create(inputStream)
                ) {

                    if (workbook.getNumberOfSheets() == 0) {
                        return rows;
                    }

                    // Currently reads the first worksheet
                    Sheet sheet = workbook.getSheetAt(0);

                    Row headerRow = sheet.getRow(0);

                    if (headerRow == null) {
                        return rows;
                    }

                    DataFormatter formatter = new DataFormatter();

                    List<String> headers = new ArrayList<>();

                    for (int i = 0; i < headerRow.getLastCellNum(); i++) {

                        String header = formatter.formatCellValue(
                                headerRow.getCell(i));

                        if (header == null || header.isBlank()) {
                            header = "Column_" + (i + 1);
                        }

                        headers.add(header);
                    }

                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                        Row dataRow = sheet.getRow(i);

                        if (dataRow == null) {
                            continue;
                        }

                        Map<String, String> row = new LinkedHashMap<>();

                        for (int j = 0; j < headers.size(); j++) {

                            String value = formatter.formatCellValue(
                                    dataRow.getCell(j));

                            row.put(headers.get(j), value);
                        }

                        // No row validation. Save every physical row.
                        rows.add(row);
                    }
                }
            }

            else {
                throw new RuntimeException(
                        "Only XLSX, XLS and CSV files are supported.");
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to read import file: " +
                    e.getMessage(), e);
        }

        return rows;
    }

    // ================= MAP ROW =================

    private Map<String, String> mapRow(
            Map<String, String> originalRow) {

        Map<String, String> mapped = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry :
                originalRow.entrySet()) {

            String field = identifyColumn(entry.getKey());

            if (field != null) {
                mapped.put(field, entry.getValue());
            }
        }

        String fullName = mapped.get("fullName");

        if (fullName != null && !fullName.isBlank()) {

            String[] nameParts = fullName.trim().split("\\s+", 2);

            if (mapped.get("firstName") == null
                    || mapped.get("firstName").isBlank()) {
                mapped.put("firstName", nameParts[0]);
            }

            if ((mapped.get("lastName") == null
                    || mapped.get("lastName").isBlank())
                    && nameParts.length > 1) {
                mapped.put("lastName", nameParts[1]);
            }
        }

        mapped.remove("fullName");

        return mapped;
    }

    // ================= SOURCE NORMALIZATION =================

    private LeadSource getLeadSource(String value) {

        if (value == null || value.isBlank()) {
            return LeadSource.OTHER;
        }

        String source = normalizeHeader(value);

        switch (source) {

            case "website":
            case "web":
                return LeadSource.WEBSITE;

            case "referral":
            case "referred":
                return LeadSource.REFERRAL;

            case "socialmedia":
            case "facebook":
            case "instagram":
                return LeadSource.SOCIAL_MEDIA;

            case "coldcall":
            case "calling":
                return LeadSource.COLD_CALL;

            case "email":
            case "mail":
                return LeadSource.EMAIL;

            case "advertisement":
            case "ads":
                return LeadSource.ADVERTISEMENT;

            default:
                return LeadSource.OTHER;
        }
    }

    // ================= PREVIEW IMPORT =================


@Override
public LeadImportPreviewDTO previewImport(MultipartFile file) {

    List<Map<String, String>> originalRows = readFile(file);

    LeadImportPreviewDTO preview = new LeadImportPreviewDTO();

    List<String> headers = new ArrayList<>();
    Map<String, String> autoMappings = new LinkedHashMap<>();
    List<String> unknownColumns = new ArrayList<>();

    if (!originalRows.isEmpty()) {

        for (String header : originalRows.get(0).keySet()) {

            headers.add(header);

            String field = identifyColumn(header);

            if (field != null) {
                autoMappings.put(header, field);
            } else {
                unknownColumns.add(header);
            }
        }
    }

    // Preview only first 20 rows
    List<Map<String, String>> previewRows =
            originalRows.stream()
                    .limit(20)
                    .toList();

    preview.setHeaders(headers);
    preview.setAutoMappings(autoMappings);
    preview.setUnknownColumns(unknownColumns);
    preview.setPreviewRows(previewRows);

    // Actual total rows
    preview.setTotalRows(originalRows.size());

    // No validation
    preview.setValidRows(originalRows.size());
    preview.setInvalidRows(0);
    preview.setDuplicateRows(0);
    preview.setWarnings(new ArrayList<>());

    return preview;
}

    // ================= SAVE IMPORT =================

    @Override
    @Transactional
    public LeadImportResultDTO saveImport(MultipartFile file) {

        List<Map<String, String>> originalRows = readFile(file);

        LeadImportResultDTO result = new LeadImportResultDTO();

        List<String> errors = new ArrayList<>();

        int savedCount = 0;
        int failedCount = 0;
        int rowNumber = 1;

        // ================= CREATE BATCH =================

        LeadImportBatch batch = new LeadImportBatch();

        batch.setFileName(file.getOriginalFilename());
        batch.setTotalRows(originalRows.size());

        List<String> headers = originalRows.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(originalRows.get(0).keySet());

        try {

            batch.setHeadersJson(
                    objectMapper.writeValueAsString(headers)
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to save Excel headers.", e);
        }

        batch = batchRepository.save(batch);

        // ================= SAVE ALL ROWS =================

        for (Map<String, String> originalRow : originalRows) {

            rowNumber++;

            try {

                Map<String, String> mapped = mapRow(originalRow);

                Lead lead = new Lead();

                // Automatically mapped fields
                lead.setFirstName(mapped.get("firstName"));
                lead.setLastName(mapped.get("lastName"));
                lead.setEmail(mapped.get("email"));
                lead.setPhone(mapped.get("phone"));

                lead.setCompanyName(mapped.get("companyName"));

                lead.setAddressLine1(mapped.get("addressLine1"));
                lead.setAddressLine2(mapped.get("addressLine2"));

                lead.setCity(mapped.get("city"));
                lead.setState(mapped.get("state"));
                lead.setCountry(mapped.get("country"));
                lead.setPincode(mapped.get("pincode"));

                lead.setDescription(mapped.get("description"));

                lead.setSource(
                        getLeadSource(mapped.get("source"))
                );

                // Default CRM status
                lead.setStatus(LeadStatus.NEW);

                // Mark as RAW imported lead
                lead.setRaw(true);
                lead.setImportStatus(LeadImportStatus.RAW);

                // Connect with uploaded batch
                lead.setImportBatch(batch);

                // Store complete original row
                lead.setRawData(
                        objectMapper.writeValueAsString(originalRow)
                );

                leadRepository.save(lead);

                savedCount++;

            } catch (Exception e) {

                failedCount++;

                errors.add(
                        "Row " + rowNumber + ": " + e.getMessage()
                );
            }
        }

        // ================= UPDATE BATCH =================

        batch.setSavedCount(savedCount);

        batchRepository.save(batch);

        // ================= RESPONSE =================

        result.setTotalRows(originalRows.size());
        result.setSavedCount(savedCount);

        // No duplicate checking
        result.setDuplicateCount(0);

        result.setFailedCount(failedCount);
        result.setSkippedCount(0);

        result.setErrors(errors);

        result.setBatchId(batch.getId());

        result.setMessage(
                "Import completed. " + savedCount + " rows saved successfully."
        );

        return result;
    }
}
