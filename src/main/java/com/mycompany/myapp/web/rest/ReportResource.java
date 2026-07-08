package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.ReportService;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportResource {

    private final Logger log = LoggerFactory.getLogger(ReportResource.class);

    private final ReportService reportService;

    public ReportResource(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard/excel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportDashboardToExcel(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.debug("REST request to export dashboard to Excel from {} to {}", startDate, endDate);
        byte[] excelFile = reportService.exportDashboardToExcel(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "dashboard-report.xlsx");

        return ResponseEntity.ok().headers(headers).body(excelFile);
    }

    @GetMapping("/revenue/excel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportRevenueToExcel(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.debug("REST request to export revenue report to Excel from {} to {}", startDate, endDate);
        byte[] excelFile = reportService.exportRevenueReport(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "revenue-report.xlsx");

        return ResponseEntity.ok().headers(headers).body(excelFile);
    }
}
