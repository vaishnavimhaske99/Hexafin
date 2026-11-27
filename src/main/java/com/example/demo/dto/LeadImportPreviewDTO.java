
package com.example.demo.dto;

import java.util.List;
import java.util.Map;

public class LeadImportPreviewDTO {

    private List<String> headers;

    private Map<String, String> autoMappings;

    private List<String> unknownColumns;

    private List<Map<String, String>> previewRows;

    private int totalRows;

    private int validRows;

    private int invalidRows;

    private int duplicateRows;

    private List<String> warnings;

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getAutoMappings() {
        return autoMappings;
    }

    public void setAutoMappings(Map<String, String> autoMappings) {
        this.autoMappings = autoMappings;
    }

    public List<String> getUnknownColumns() {
        return unknownColumns;
    }

    public void setUnknownColumns(List<String> unknownColumns) {
        this.unknownColumns = unknownColumns;
    }

    public List<Map<String, String>> getPreviewRows() {
        return previewRows;
    }

    public void setPreviewRows(List<Map<String, String>> previewRows) {
        this.previewRows = previewRows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getValidRows() {
        return validRows;
    }

    public void setValidRows(int validRows) {
        this.validRows = validRows;
    }

    public int getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(int invalidRows) {
        this.invalidRows = invalidRows;
    }

    public int getDuplicateRows() {
        return duplicateRows;
    }

    public void setDuplicateRows(int duplicateRows) {
        this.duplicateRows = duplicateRows;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
