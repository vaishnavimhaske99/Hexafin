
package com.example.demo.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import com.example.demo.entity.LeadImportStatus;
import com.example.demo.entity.LeadSource;
import com.example.demo.entity.LeadStatus;
import com.example.demo.repository.LeadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SmartLeadImportServiceImpl implements SmartLeadImportService {

    private final LeadRepository leadRepository;
    private final ObjectMapper objectMapper;

    public SmartLeadImportServiceImpl(
            LeadRepository leadRepository,
            ObjectMapper objectMapper) {
        this.leadRepository = leadRepository;
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


 // ================= AUTOMATIC COLUMN MAPPING =================

 private String identifyColumn(String header) {

     String key = normalizeHeader(header);

     if (key.isBlank()) {
         return null;
     }

     // 1. COMPANY NAME
     if (List.of(
             "company", "companyname", "businessname",
             "organization", "organisation",
             "organizationname", "organisationname",
             "firm", "firmname", "business"
     ).contains(key)) {
         return "companyName";
     }

     // 2. FULL / CUSTOMER / APPLICANT NAME
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

     // 3. FIRST NAME
     if (List.of(
             "firstname", "first", "givenname",
             "fname", "forename"
     ).contains(key)) {
         return "firstName";
     }

     // 4. LAST NAME
     if (List.of(
             "lastname", "last", "surname",
             "familyname", "lname"
     ).contains(key)) {
         return "lastName";
     }

     // 5. MOBILE / PHONE / CONTACT NUMBER
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

     // 6. EMAIL
     if (List.of(
             "email", "emailid", "emailaddress",
             "mail", "mailid", "emailaddr",
             "primaryemail", "contactemail"
     ).contains(key)
             || key.contains("email")) {
         return "email";
     }

     // 7. ADDRESS
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

     // 8. LOCATION
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

     // 9. LEAD SOURCE
     if (List.of(
             "source", "leadsource",
             "enquirysource", "referencesource"
     ).contains(key)) {
         return "source";
     }

     // 10. STATUS
     if (List.of(
             "status", "leadstatus",
             "enquirystatus"
     ).contains(key)) {
         return "status";
     }

     // 11. DESCRIPTION / REMARKS
     if (List.of(
             "description", "remarks", "notes",
             "comments", "remark", "comment",
             "requirement", "customerrequirement"
     ).contains(key)) {
         return "description";
     }

     // Unknown columns will be preserved in rawData
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

                        for (String header : headers) {
                            row.put(header,
                                    record.get(header).trim());
                        }

                        if (!row.isEmpty()) {
                            rows.add(row);
                        }
                    }
                }

            } else if (extension.equals("xlsx") ||
                       extension.equals("xls")) {

                try (
                    InputStream inputStream = file.getInputStream();
                    Workbook workbook = WorkbookFactory.create(inputStream)
                ) {
                    if (workbook.getNumberOfSheets() == 0) {
                        return rows;
                    }

                    Sheet sheet = workbook.getSheetAt(0);

                    if (sheet.getPhysicalNumberOfRows() == 0) {
                        return rows;
                    }

                    Row headerRow = sheet.getRow(0);

                    if (headerRow == null) {
                        return rows;
                    }

                    DataFormatter formatter = new DataFormatter();

                    List<String> headers = new ArrayList<>();

                    for (int i = 0;
                         i < headerRow.getLastCellNum(); i++) {

                        headers.add(formatter.formatCellValue(
                                headerRow.getCell(i)).trim());
                    }

                    for (int i = 1;
                         i <= sheet.getLastRowNum(); i++) {

                        Row dataRow = sheet.getRow(i);

                        if (dataRow == null) {
                            continue;
                        }

                        Map<String, String> row =
                                new LinkedHashMap<>();

                        for (int j = 0; j < headers.size(); j++) {

                            String header = headers.get(j);

                            if (header.isEmpty()) {
                                continue;
                            }

                            String value = formatter.formatCellValue(
                                    dataRow.getCell(j)).trim();

                            row.put(header, value);
                        }

                        if (row.values().stream()
                                .anyMatch(value -> !value.isBlank())) {
                            rows.add(row);
                        }
                    }
                }

            } else {
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

            if (mapped.get("firstName") == null ||
                    mapped.get("firstName").isBlank()) {
                mapped.put("firstName", nameParts[0]);
            }

            if ((mapped.get("lastName") == null ||
                    mapped.get("lastName").isBlank()) &&
                    nameParts.length > 1) {
                mapped.put("lastName", nameParts[1]);
            }
        }

        mapped.remove("fullName");

        return mapped;
    }

    // ================= UNKNOWN COLUMNS =================

    private Map<String, String> getUnknownColumns(
            Map<String, String> originalRow) {

        Map<String, String> unknown = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry :
                originalRow.entrySet()) {

            if (identifyColumn(entry.getKey()) == null) {
                unknown.put(entry.getKey(), entry.getValue());
            }
        }

        return unknown;
    }

    // ================= VALIDATION =================

    private String validateRow(Map<String, String> mapped) {

    	 return null;
    
    }

    // ================= DUPLICATE CHECK =================

    private boolean isDuplicate(
            Map<String, String> mapped,
            Set<String> importedEmails,
            Set<String> importedPhones) {

        String email = mapped.get("email");
        String phone = mapped.get("phone");

        if (email != null && !email.isBlank()) {
            email = email.trim().toLowerCase(Locale.ROOT);

            if (leadRepository.existsByEmail(email) ||
                    importedEmails.contains(email)) {
                return true;
            }
        }

        if (phone != null && !phone.isBlank()) {
            phone = phone.replaceAll("\\D", "");

            if (leadRepository.existsByPhone(phone) ||
                    importedPhones.contains(phone)) {
                return true;
            }
        }

        if (email != null && !email.isBlank()) {
            importedEmails.add(email);
        }

        if (phone != null && !phone.isBlank()) {
            importedPhones.add(phone);
        }

        return false;
    }

    // ================= PREVIEW IMPORT =================

    @Override
    public LeadImportPreviewDTO previewImport(MultipartFile file) {

        List<Map<String, String>> originalRows = readFile(file);

        LeadImportPreviewDTO preview = new LeadImportPreviewDTO();

        List<String> headers = new ArrayList<>();
        Map<String, String> autoMappings = new LinkedHashMap<>();
        List<String> unknownColumns = new ArrayList<>();
        List<Map<String, String>> previewRows = new ArrayList<>();
        List<String> warnings = new ArrayList<>();



        int validRows = 0;
        int invalidRows = 0;
        int duplicateRows = 0;
        int rowNumber = 1;

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

        for (Map<String, String> originalRow : originalRows) {

            rowNumber++;

            Map<String, String> mapped = mapRow(originalRow);

            previewRows.add(originalRow);

            String validationError = validateRow(mapped);

            if (validationError != null) {
                invalidRows++;
                warnings.add("Row " + rowNumber + ": " +
                        validationError);
                continue;
            }

   

            validRows++;
        }

        preview.setHeaders(headers);
        preview.setAutoMappings(autoMappings);
        preview.setUnknownColumns(unknownColumns);
        preview.setPreviewRows(previewRows);
        preview.setTotalRows(originalRows.size());
        preview.setValidRows(validRows);
        preview.setInvalidRows(invalidRows);
        preview.setDuplicateRows(duplicateRows);
        preview.setWarnings(warnings);

        return preview;
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

    // ================= SAVE IMPORT =================

    @Override
    @Transactional
    public LeadImportResultDTO saveImport(MultipartFile file) {

        List<Map<String, String>> originalRows = readFile(file);

        LeadImportResultDTO result = new LeadImportResultDTO();

        Set<String> importedEmails = new HashSet<>();
        Set<String> importedPhones = new HashSet<>();

        List<String> errors = new ArrayList<>();

        int savedCount = 0;
        int duplicateCount = 0;
        int failedCount = 0;
        int rowNumber = 1;

        for (Map<String, String> originalRow : originalRows) {

            rowNumber++;

            try {
                Map<String, String> mapped = mapRow(originalRow);

                String validationError = validateRow(mapped);

                if (validationError != null) {
                    failedCount++;
                    errors.add("Row " + rowNumber + ": " +
                            validationError);
                    continue;
                }

                if (isDuplicate(mapped, importedEmails, importedPhones)) {
                    duplicateCount++;
                    errors.add("Row " + rowNumber +
                            ": Duplicate lead");
                    continue;
                }

                Lead lead = new Lead();

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

                lead.setSource(getLeadSource(mapped.get("source")));

                lead.setStatus(LeadStatus.NEW);
                lead.setRaw(true);
                lead.setImportStatus(LeadImportStatus.RAW);

                Map<String, String> unknownColumns =
                        getUnknownColumns(originalRow);

                lead.setRawData(
                        objectMapper.writeValueAsString(unknownColumns));

                leadRepository.save(lead);

                savedCount++;

            } catch (Exception e) {
                failedCount++;
                errors.add("Row " + rowNumber + ": " +
                        e.getMessage());
            }
        }

        result.setTotalRows(originalRows.size());
        result.setSavedCount(savedCount);
        result.setDuplicateCount(duplicateCount);
        result.setFailedCount(failedCount);
        result.setSkippedCount(0);
        result.setErrors(errors);
        result.setMessage("Raw lead import completed.");

        return result;
    }
}
