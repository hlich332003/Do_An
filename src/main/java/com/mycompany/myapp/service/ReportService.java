package com.mycompany.myapp.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final DashboardService dashboardService;

    public ReportService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public byte[] exportDashboardToExcel(LocalDate startDate, LocalDate endDate) {
        log.debug("Exporting dashboard report to Excel from {} to {}", startDate, endDate);
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Báo Cáo Doanh Thu");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());

            // Tiêu đề
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO DOANH THU");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

            // Thông tin thời gian
            Row dateRow = sheet.createRow(2);
            dateRow.createCell(0).setCellValue("Từ ngày:");
            dateRow.createCell(1).setCellValue(startDate.toString());
            dateRow.createCell(2).setCellValue("Đến ngày:");
            dateRow.createCell(3).setCellValue(endDate.toString());

            // Column headers
            Row headerRow = sheet.createRow(4);
            String[] headers = { "Chỉ Tiêu", "Giá Trị", "Đơn Vị" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 5;
            Object dashboardData = dashboardService.getDashboardData(startDate.toString(), endDate.toString());

            String[][] data = {
                { "Tổng Doanh Thu", "0", "VND" },
                { "Tổng Vé Bán", "0", "Vé" },
                { "Tổng Đơn F&B", "0", "Đơn" },
                { "Tổng Người Dùng", "0", "Người" },
            };

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);

            for (String[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(rowData[i]);
                    cell.setCellStyle(dataStyle);
                }
            }

            // Auto size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error exporting to Excel", e);
            throw new RuntimeException("Lỗi xuất báo cáo Excel", e);
        }
    }

    public byte[] exportRevenueReport(LocalDate startDate, LocalDate endDate) {
        log.debug("Exporting revenue report from {} to {}", startDate, endDate);
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Doanh Thu Chi Tiết");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = { "Ngày", "Tổng Vé Bán", "Tổng Doanh Thu", "F&B" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add data rows (placeholder)
            for (int i = 0; i < 30; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error exporting revenue report", e);
            throw new RuntimeException("Lỗi xuất báo cáo doanh thu", e);
        }
    }
}
