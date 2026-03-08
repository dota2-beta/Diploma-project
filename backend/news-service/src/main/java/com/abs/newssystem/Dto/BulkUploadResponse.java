package com.abs.newssystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkUploadResponse {
    private int successCount;
    private int errorCount;
    private int skippedCount;
    private List<String> failedTitles;
}
