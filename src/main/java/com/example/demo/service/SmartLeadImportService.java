
package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.LeadImportPreviewDTO;
import com.example.demo.dto.LeadImportResultDTO;

public interface SmartLeadImportService {

    LeadImportPreviewDTO previewImport(MultipartFile file);

    LeadImportResultDTO saveImport(MultipartFile file);
}
