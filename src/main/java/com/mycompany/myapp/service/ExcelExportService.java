package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.DashboardDTO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelExportService {

    public byte[] exportDashboardToExcel(DashboardDTO dashboardDTO, String fromDate, String toDate) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("BaoCao");
            createReportSheet(sheet, workbook, dashboardDTO, fromDate, toDate);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createReportSheet(Sheet sheet, Workbook workbook, DashboardDTO data, String fromDate, String toDate) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle moneyStyle = createMoneyStyle(workbook);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BAO CAO TONG HOP CINEMATICK");
        titleCell.setCellStyle(titleStyle);

        Row rangeRow = sheet.createRow(1);
        rangeRow.createCell(0).setCellValue("Tu ngay");
        rangeRow.createCell(1).setCellValue(fromDate != null ? fromDate : "Tat ca");
        rangeRow.createCell(2).setCellValue("Den ngay");
        rangeRow.createCell(3).setCellValue(toDate != null ? toDate : "Tat ca");

        int rowIdx = 3;
        Row metricHeader = sheet.createRow(rowIdx++);
        metricHeader.createCell(0).setCellValue("Chi tieu");
        metricHeader.createCell(1).setCellValue("Gia tri");
        metricHeader.getCell(0).setCellStyle(headerStyle);
        metricHeader.getCell(1).setCellStyle(headerStyle);

        BigDecimal totalRevenue = data.getTotalRevenue() != null ? data.getTotalRevenue() : BigDecimal.ZERO;
        BigDecimal fbRevenue = data.getTotalFbRevenue() != null ? data.getTotalFbRevenue() : BigDecimal.ZERO;
        BigDecimal ticketRevenue = totalRevenue.subtract(fbRevenue);
        if (ticketRevenue.compareTo(BigDecimal.ZERO) < 0) {
            ticketRevenue = BigDecimal.ZERO;
        }

        rowIdx = addMetricRow(sheet, rowIdx, "Tong doanh thu", totalRevenue, moneyStyle);
        rowIdx = addMetricRow(sheet, rowIdx, "Doanh thu ve", ticketRevenue, moneyStyle);
        rowIdx = addMetricRow(sheet, rowIdx, "Doanh thu F&B", fbRevenue, moneyStyle);
        rowIdx = addMetricRow(sheet, rowIdx, "So ve da ban", data.getTotalTicketsSold(), null);
        rowIdx = addMetricRow(sheet, rowIdx, "Ty le lap day trung binh", data.getAverageOccupancyRate(), null);
        rowIdx = addMetricRow(sheet, rowIdx, "Ty le mua kem combo", data.getComboAttachRate(), null);
        rowIdx = addMetricRow(sheet, rowIdx, "So hoa don da thanh toan", data.getPaidInvoicesCount(), null);
        rowIdx = addMetricRow(sheet, rowIdx, "So hoa don co combo", data.getInvoicesWithCombo(), null);
        rowIdx = addMetricRow(sheet, rowIdx, "So luot danh gia", data.getReviewCount(), null);
        rowIdx = addMetricRow(sheet, rowIdx, "Diem danh gia trung binh", data.getAverageMovieRating(), null);

        rowIdx++;
        rowIdx = writeRankingSection(
            sheet,
            workbook,
            rowIdx,
            "Top 5 phim hot",
            "Phim",
            "Ve ban",
            "Doanh thu",
            data.getTopMovies(),
            headerStyle
        );
        rowIdx++;
        writeRankingSection(
            sheet,
            workbook,
            rowIdx,
            "Top 5 combo bap nuoc",
            "Combo",
            "So luong",
            "Doanh thu",
            data.getTopCombos(),
            headerStyle
        );

        sheet.setColumnWidth(0, 24000);
        sheet.setColumnWidth(1, 12000);
        sheet.setColumnWidth(2, 12000);
    }

    private int writeRankingSection(
        Sheet sheet,
        Workbook workbook,
        int startRow,
        String title,
        String col1,
        String col2,
        String col3,
        java.util.List<DashboardDTO.DashboardItemDTO> items,
        CellStyle headerStyle
    ) {
        int rowIdx = startRow;
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(headerStyle);

        Row headerRow = sheet.createRow(rowIdx++);
        headerRow.createCell(0).setCellValue(col1);
        headerRow.createCell(1).setCellValue(col2);
        headerRow.createCell(2).setCellValue(col3);
        for (int i = 0; i < 3; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }

        if (items != null) {
            for (DashboardDTO.DashboardItemDTO item : items) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getName() != null ? item.getName() : "N/A");
                row.createCell(1).setCellValue(item.getCount());
                row.createCell(2).setCellValue(item.getRevenue() != null ? item.getRevenue().doubleValue() : 0);
            }
        }

        return rowIdx;
    }

    private int addMetricRow(Sheet sheet, int rowIdx, String label, Object value, CellStyle style) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        Cell cell = row.createCell(1);
        if (style == null && label.toLowerCase().contains("ty le")) {
            if (value instanceof BigDecimal bigDecimal) {
                cell.setCellValue(bigDecimal.stripTrailingZeros().toPlainString() + "%");
            } else if (value instanceof Number number) {
                cell.setCellValue(number.doubleValue() + "%");
            } else {
                cell.setCellValue(value != null ? value.toString() : "0%");
            }
        } else if (value instanceof BigDecimal bigDecimal) {
            cell.setCellValue(bigDecimal.doubleValue());
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value != null ? value.toString() : "0");
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
        return rowIdx + 1;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = createHeaderStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
}
