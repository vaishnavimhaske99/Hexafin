
package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.dto.LeadImportPreviewDTO;
import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadImportStatus;
import com.example.demo.entity.LeadSource;
import com.example.demo.entity.LeadStatus;
import com.example.demo.entity.LoanProductType;
import com.example.demo.repository.LeadRepository;

@Service
public class LeadImportService {

    private static final int PREVIEW_LIMIT = 10;

    private final LeadRepository leadRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LeadImportService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    private static final Map<String, String> COLUMN_ALIASES = Map.ofEntries(
        Map.entry("firstname", "firstName"),
        Map.entry("fname", "firstName"),
        Map.entry("givenname", "firstName"),
        Map.entry("lastname", "lastName"),
        Map.entry("lname", "lastName"),
        Map.entry("surname", "lastName"),
        Map.entry("company", "companyName"),
        Map.entry("companyname", "companyName"),
        Map.entry("organization", "companyName"),
        Map.entry("organisation", "companyName"),
        Map.entry("email", "email"),
        Map.entry("emailaddress", "email"),
        Map.entry("mail", "email"),
        Map.entry("phone", "phone"),
        Map.entry("mobile", "phone"),
        Map.entry("mobileno", "phone"),
        Map.entry("phonenumber", "phone"),
        Map.entry("contactnumber", "phone"),
        Map.entry("address", "addressLine1"),
        Map.entry("addressline1", "addressLine1"),
        Map.entry("addressline2", "addressLine2"),
        Map.entry("city", "city"),
        Map.entry("state", "state"),
        Map.entry("country", "country"),
        Map.entry("pincode", "pincode"),
        Map.entry("zipcode", "pincode"),
        Map.entry("postalcode", "pincode"),
        Map.entry("source", "source"),
        Map.entry("leadsource", "source"),
        Map.entry("product", "product"),
        Map.entry("loanproduct", "product"),
        Map.entry("producttype", "product"),
        Map.entry("description", "description"),
        Map.entry("remarks", "description"),
        Map.entry("notes", "description")
    );

    // =========================================
    // PREVIEW FILE
    // =========================================

    public LeadImportPreviewDTO previewFile(MultipartFile file) {

        validateFile(file);

        String lowerName = file.getOriginalFilename()
                .toLowerCase(Locale.ROOT);

        try {
            if (lowerName.endsWith(".csv")) {
                return previewCsv(file);
            }

            return previewExcel(file);

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unable to read the uploaded file.");
        }
    }

    // =========================================
    // SAVE ALL ROWS AS RAW LEADS
    // =========================================

    @Transactional
    public Map<String, Object> saveRawLeads(MultipartFile file) {

        validateFile(file);

        List<Lead> rawLeads;

        String lowerName = file.getOriginalFilename()
                .toLowerCase(Locale.ROOT);

        try {
            if (lowerName.endsWith(".csv")) {
                rawLeads = readCsvForSaving(file);
            } else {
                rawLeads = readExcelForSaving(file);
            }

            if (rawLeads.isEmpty()) {
                throw new IllegalArgumentException(
                        "No data rows found in the uploaded file.");
            }

            List<Lead> savedLeads = leadRepository.saveAll(rawLeads);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Raw leads saved successfully.");
            response.put("totalRows", rawLeads.size());
            response.put("savedCount", savedLeads.size());
            response.put("importStatus", LeadImportStatus.RAW);
            response.put("raw", true);

            return response;

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unable to read the uploaded file.");
        }
    }

    // =========================================
    // IMPORT DIRECTLY INTO NORMAL LEADS
    // =========================================

    @Transactional
    public Map<String, Object> importLeads(MultipartFile file) {

        validateFile(file);

        List<Lead> leads;

        String lowerName = file.getOriginalFilename()
                .toLowerCase(Locale.ROOT);

        try {

            if (lowerName.endsWith(".csv")) {
                leads = readCsvForSaving(file);
            } else {
                leads = readExcelForSaving(file);
            }

            if (leads.isEmpty()) {
                throw new IllegalArgumentException(
                        "No data rows found in the uploaded file.");
            }

            LocalDateTime now = LocalDateTime.now();

            for (Lead lead : leads) {

                // New record
                lead.setId(null);

                // Normal Lead settings
                lead.setStatus(LeadStatus.NEW);
                lead.setRaw(false);
                lead.setImportStatus(LeadImportStatus.IMPORTED);

                // Audit fields
                lead.setCreatedAt(now);
                lead.setUpdatedAt(now);

                // Assignment will be done later
                lead.setAssignedTo(null);
            }

            List<Lead> savedLeads = leadRepository.saveAll(leads);

            Map<String, Object> response = new LinkedHashMap<>();

            response.put("message", "Leads imported successfully.");
            response.put("totalRows", leads.size());
            response.put("savedCount", savedLeads.size());
            response.put("importStatus", LeadImportStatus.IMPORTED);
            response.put("raw", false);

            return response;

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "Unable to read the uploaded file.");
        }
    }

    // =========================================
    // READ EXCEL AND PREPARE RAW LEADS
    // =========================================

    private List<Lead> readExcelForSaving(MultipartFile file)
            throws IOException {

        List<Lead> leads = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Excel file has no sheets.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException(
                        "Header row is missing.");
            }

            DataFormatter formatter = new DataFormatter();

            List<String> headers = new ArrayList<>();

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                headers.add(formatter.formatCellValue(
                        headerRow.getCell(i)).trim());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                Map<String, String> rowData = new LinkedHashMap<>();
                boolean hasData = false;

                for (int j = 0; j < headers.size(); j++) {

                    String value = formatter.formatCellValue(
                            row.getCell(j)).trim();

                    rowData.put(headers.get(j), value);

                    if (!value.isBlank()) {
                        hasData = true;
                    }
                }

                if (hasData) {
                    leads.add(createRawLead(rowData));
                }
            }
        }

        return leads;
    }

    // =========================================
    // READ CSV AND PREPARE RAW LEADS
    // =========================================

    private List<Lead> readCsvForSaving(MultipartFile file)
            throws IOException {

        List<Lead> leads = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             CSVParser parser = CSVParser.parse(
                 inputStream,
                 java.nio.charset.StandardCharsets.UTF_8,
                 CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
             )) {

            List<String> headers = new ArrayList<>(
                    parser.getHeaderNames());

            for (CSVRecord record : parser) {

                Map<String, String> rowData = new LinkedHashMap<>();
                boolean hasData = false;

                for (String header : headers) {

                    String value = record.get(header).trim();

                    rowData.put(header, value);

                    if (!value.isBlank()) {
                        hasData = true;
                    }
                }

                if (hasData) {
                    leads.add(createRawLead(rowData));
                }
            }
        }

        return leads;
    }

    // =========================================
    // CREATE RAW LEAD ENTITY
    // =========================================

    private Lead createRawLead(Map<String, String> rowData)
            throws IOException {

        Lead lead = new Lead();

        Map<String, String> mappedData = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : rowData.entrySet()) {

            String normalized = entry.getKey()
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]", "");

            String field = COLUMN_ALIASES.get(normalized);

            if (field != null) {
                mappedData.put(field, entry.getValue());
            }
        }

        lead.setFirstName(mappedData.get("firstName"));
        lead.setLastName(mappedData.get("lastName"));
        lead.setCompanyName(mappedData.get("companyName"));
        lead.setEmail(mappedData.get("email"));
        lead.setPhone(mappedData.get("phone"));
        lead.setAddressLine1(mappedData.get("addressLine1"));
        lead.setAddressLine2(mappedData.get("addressLine2"));
        lead.setCity(mappedData.get("city"));
        lead.setState(mappedData.get("state"));
        lead.setCountry(mappedData.get("country"));
        lead.setPincode(mappedData.get("pincode"));
        lead.setDescription(mappedData.get("description"));

        lead.setSource(parseEnum(
                LeadSource.class, mappedData.get("source")));

        lead.setProduct(parseEnum(
                LoanProductType.class, mappedData.get("product")));

        lead.setStatus(null);

        lead.setRaw(true);
        lead.setImportStatus(LeadImportStatus.RAW);

        LocalDateTime now = LocalDateTime.now();

        lead.setCreatedAt(now);
        lead.setUpdatedAt(now);

        // Preserve every original Excel / CSV column
        lead.setRawData(objectMapper.writeValueAsString(rowData));

        return lead;
    }

    // =========================================
    // SAFE ENUM CONVERSION
    // =========================================

    private <E extends Enum<E>> E parseEnum(
            Class<E> enumClass, String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            String normalized = value.trim()
                    .toUpperCase(Locale.ROOT)
                    .replace(' ', '_')
                    .replace('-', '_');

            return Enum.valueOf(enumClass, normalized);

        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // =========================================
    // EXCEL PREVIEW
    // =========================================

    private LeadImportPreviewDTO previewExcel(MultipartFile file)
            throws IOException {

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Excel file has no sheets.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException(
                        "Header row is missing.");
            }

            DataFormatter formatter = new DataFormatter();

            List<String> headers = new ArrayList<>();

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                headers.add(formatter.formatCellValue(
                        headerRow.getCell(i)).trim());
            }

            List<Map<String, String>> rows = new ArrayList<>();

            for (int i = 1;
                 i <= sheet.getLastRowNum() && rows.size() < PREVIEW_LIMIT;
                 i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                Map<String, String> rowData = new LinkedHashMap<>();
                boolean hasData = false;

                for (int j = 0; j < headers.size(); j++) {

                    String value = formatter.formatCellValue(
                            row.getCell(j)).trim();

                    rowData.put(headers.get(j), value);

                    if (!value.isBlank()) {
                        hasData = true;
                    }
                }

                if (hasData) {
                    rows.add(rowData);
                }
            }

            return buildPreview(headers, rows, countExcelRows(sheet));
        }
    }

    private int countExcelRows(Sheet sheet) {

        int count = 0;

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            boolean hasData = false;

            for (int j = 0; j < row.getLastCellNum(); j++) {

                if (row.getCell(j) != null
                        && !row.getCell(j).toString().isBlank()) {

                    hasData = true;
                    break;
                }
            }

            if (hasData) {
                count++;
            }
        }

        return count;
    }

    // =========================================
    // CSV PREVIEW
    // =========================================

    private LeadImportPreviewDTO previewCsv(MultipartFile file)
            throws IOException {

        try (InputStream inputStream = file.getInputStream();
             CSVParser parser = CSVParser.parse(
                 inputStream,
                 java.nio.charset.StandardCharsets.UTF_8,
                 CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
             )) {

            List<String> headers = new ArrayList<>(
                    parser.getHeaderNames());

            List<Map<String, String>> rows = new ArrayList<>();
            int totalRows = 0;

            for (CSVRecord record : parser) {

                totalRows++;

                if (rows.size() >= PREVIEW_LIMIT) {
                    continue;
                }

                Map<String, String> rowData = new LinkedHashMap<>();

                for (String header : headers) {
                    rowData.put(header, record.get(header));
                }

                rows.add(rowData);
            }

            return buildPreview(headers, rows, totalRows);
        }
    }

    // =========================================
    // BUILD PREVIEW RESPONSE
    // =========================================

    private LeadImportPreviewDTO buildPreview(
            List<String> headers,
            List<Map<String, String>> rows,
            int totalRows) {

        Map<String, String> autoMappings = new LinkedHashMap<>();
        List<String> unknownColumns = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (String header : headers) {

            String normalized = header
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]", "");

            String mappedField = COLUMN_ALIASES.get(normalized);

            if (mappedField != null) {
                autoMappings.put(header, mappedField);
            } else {
                unknownColumns.add(header);
            }
        }

        if (!autoMappings.containsValue("firstName")) {
            warnings.add("First Name column was not detected.");
        }

        if (!autoMappings.containsValue("email")) {
            warnings.add("Email column was not detected.");
        }

        if (!autoMappings.containsValue("phone")) {
            warnings.add("Phone column was not detected.");
        }

        LeadImportPreviewDTO response = new LeadImportPreviewDTO();

        response.setHeaders(headers);
        response.setAutoMappings(autoMappings);
        response.setUnknownColumns(unknownColumns);
        response.setPreviewRows(rows);
        response.setTotalRows(totalRows);
        response.setWarnings(warnings);

        return response;
    }

    // =========================================
    // FILE VALIDATION
    // =========================================

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a file.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new IllegalArgumentException(
                    "Invalid file name.");
        }

        String lowerName = fileName.toLowerCase(Locale.ROOT);

        if (!lowerName.endsWith(".csv")
                && !lowerName.endsWith(".xlsx")
                && !lowerName.endsWith(".xls")) {

            throw new IllegalArgumentException(
                    "Only XLSX, XLS and CSV files are supported.");
        }
    }
}
