package com.testai.ai_api_tester.service;

import com.testai.ai_api_tester.dto.TestResultDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Service
public class ExcelExportService {

    private static final String[] HEADERS = {
            "Test Name", "Endpoint", "Method", "Payload",
            "Expected Status", "Actual Status", "Passed",
            "Response Time (ms)", "Error Message"
    };

    /**
     * Generate an Excel report from test results.
     */
    public byte[] generateReport(List<TestResultDto> results, String runId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test Results");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Passed style (green)
            CellStyle passedStyle = workbook.createCellStyle();
            passedStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            passedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Failed style (red)
            CellStyle failedStyle = workbook.createCellStyle();
            failedStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            failedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            for (int i = 0; i < results.size(); i++) {
                TestResultDto r = results.get(i);
                Row row = sheet.createRow(i + 1);
                CellStyle rowStyle = Boolean.TRUE.equals(r.getPassed()) ? passedStyle : failedStyle;

                createCell(row, 0, r.getName(), rowStyle);
                createCell(row, 1, r.getEndpoint(), rowStyle);
                createCell(row, 2, r.getMethod(), rowStyle);
                createCell(row, 3, r.getPayload() != null ? r.getPayload().toString() : "—", rowStyle);
                createCell(row, 4, String.valueOf(r.getExpectedStatus()), rowStyle);
                createCell(row, 5, String.valueOf(r.getActualStatus()), rowStyle);
                createCell(row, 6, Boolean.TRUE.equals(r.getPassed()) ? "✓ PASS" : "✗ FAIL", rowStyle);
                createCell(row, 7, String.valueOf(r.getResponseTimeMs()), rowStyle);
                createCell(row, 8, r.getErrorMessage() != null ? r.getErrorMessage() : "—", rowStyle);
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                // Set minimum width
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("Generated Excel report with {} rows for runId={}", results.size(), runId);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate Excel report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel report: " + e.getMessage());
        }
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
