package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.DashboardService;
import com.mycompany.myapp.service.ExcelExportService;
import com.mycompany.myapp.service.dto.DashboardDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/* REST controller for Dashboard Statistics.
 * API: GET /api/admin/dashboard - Lấy thống kê dashboard cho admin
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardResource.class);

    private final DashboardService dashboardService;
    private final ExcelExportService excelExportService;

    public DashboardResource(DashboardService dashboardService, ExcelExportService excelExportService) {
        this.dashboardService = dashboardService;
        this.excelExportService = excelExportService;
    }

    /**
     * {@code GET /api/admin/dashboard} : Lấy dữ liệu dashboard thống kê.
     * Chỉ admin mới có thể truy cập.
     *
     * @return {@link ResponseEntity} với status {@code 200 (OK)} và dashboard data.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DashboardDTO> getDashboard(
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate
    ) {
        LOG.debug("REST request để lấy dashboard statistics từ {} đến {}", fromDate, toDate);
        DashboardDTO dashboard = dashboardService.getDashboard(fromDate, toDate);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/test-email")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> testEmail() {
        LOG.info("REST request để kiểm tra email");
        return ResponseEntity.ok("Email service is operational");
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportDashboard(
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate
    ) {
        try {
            DashboardDTO dashboard = dashboardService.getDashboard(fromDate, toDate);
            byte[] excelContent = excelExportService.exportDashboardToExcel(dashboard, fromDate, toDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "ThongKe_Dashboard.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok().headers(headers).body(excelContent);
        } catch (Exception e) {
            LOG.error("Lỗi khi xuất Excel", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
