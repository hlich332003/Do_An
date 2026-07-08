package com.mycompany.myapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.domain.ChiTietFB;
import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.domain.LichSuChatAi;
import com.mycompany.myapp.domain.Ve;
import com.mycompany.myapp.repository.HoaDonRepository;
import com.mycompany.myapp.repository.LichSuChatAiRepository;
import com.mycompany.myapp.repository.NguoiDungRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.DashboardDTO;
import com.mycompany.myapp.service.dto.DashboardDTO.DashboardItemDTO;
import com.mycompany.myapp.service.dto.DichVuFBDTO;
import com.mycompany.myapp.service.dto.PhimDTO;
import com.mycompany.myapp.service.dto.SuatChieuDTO;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class ChatbotService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatbotService.class);
    private static final DateTimeFormatter SHOWTIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final PhimService phimService;
    private final SuatChieuService suatChieuService;
    private final DashboardService dashboardService;
    private final DichVuFBService dichVuFBService;
    private final HoaDonRepository hoaDonRepository;
    private final LichSuChatAiRepository lichSuChatAiRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public ChatbotService(
        PhimService phimService,
        SuatChieuService suatChieuService,
        DashboardService dashboardService,
        DichVuFBService dichVuFBService,
        HoaDonRepository hoaDonRepository,
        LichSuChatAiRepository lichSuChatAiRepository,
        NguoiDungRepository nguoiDungRepository
    ) {
        this.phimService = phimService;
        this.suatChieuService = suatChieuService;
        this.dashboardService = dashboardService;
        this.dichVuFBService = dichVuFBService;
        this.hoaDonRepository = hoaDonRepository;
        this.lichSuChatAiRepository = lichSuChatAiRepository;
        this.nguoiDungRepository = nguoiDungRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    // =========================================================================
    // Entry point
    // =========================================================================

    public String reply(String message) {
        String normalized = normalize(message);
        if (normalized.isBlank()) {
            return "Bạn hãy nhập câu hỏi về phim, suất chiếu, đặt vé, combo hoặc thống kê nhé.";
        }

        boolean adminUser = isCurrentUserAdmin();

        if (isOutOfScope(normalized)) {
            return "Mình chỉ hỗ trợ các nội dung của CinemaTick như phim, lịch chiếu, đặt vé, combo F&B và thống kê quản trị.";
        }

        // Load dữ liệu chung — dùng từ đầu ngày hôm nay để bao gồm suất đang chiếu
        List<PhimDTO> showing = phimService.findShowing(PageRequest.of(0, 50)).getContent();
        List<PhimDTO> comingSoon = phimService.findComingSoon(PageRequest.of(0, 50)).getContent();
        List<SuatChieuDTO> todayAndUpcoming = loadTodayAndUpcomingShowtimes();
        List<SuatChieuDTO> upcomingOnly = filterUpcomingOnly(todayAndUpcoming);
        List<DichVuFBDTO> activeCombos = dichVuFBService.findActive();

        boolean todayQuery = isTodayQuery(normalized);
        boolean weekQuery = isWeekQuery(normalized);
        boolean monthQuery = isMonthQuery(normalized);

        // Nạp dữ liệu thống kê nếu user là Admin
        DashboardDTO dashboard = null;
        if (adminUser) {
            String today = LocalDate.now(ZoneId.systemDefault()).toString();
            if (weekQuery) {
                String weekStart = LocalDate.now(ZoneId.systemDefault()).minusDays(6).toString();
                dashboard = dashboardService.getDashboard(weekStart, today);
            } else if (monthQuery) {
                String monthStart = LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1).toString();
                dashboard = dashboardService.getDashboard(monthStart, today);
            } else {
                dashboard = dashboardService.getDashboard(todayQuery ? today : null, todayQuery ? today : null);
            }
        }

        // Xây dựng prompt RAG và gửi tới Gemini API
        String prompt = buildGeminiPrompt(message, showing, comingSoon, todayAndUpcoming, activeCombos, dashboard, adminUser);
        String reply = callGeminiApi(prompt);

        // Fallback về bộ xử lý rule-based cũ nếu Gemini lỗi hoặc trả về rỗng
        if (reply == null || reply.isBlank()) {
            reply = buildReply(
                normalized,
                showing,
                comingSoon,
                todayAndUpcoming,
                upcomingOnly,
                activeCombos,
                dashboard,
                adminUser,
                todayQuery,
                weekQuery,
                monthQuery
            );
            if (reply.isBlank()) {
                reply = buildFallbackReply(showing, comingSoon, todayAndUpcoming, adminUser);
            }
        }

        saveChatLog(message, reply);
        return reply;
    }

    private String callGeminiApi(String promptText) {
        if (apiKey == null || apiKey.isBlank()) {
            LOG.warn("Gemini API key is not configured or blank. Falling back to rule-based.");
            return "";
        }
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey.trim();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Construct payload: {"contents": [{"parts": [{"text": promptText}]}]}
            Map<String, Object> part = new HashMap<>();
            part.put("text", promptText);

            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(part);

            Map<String, Object> contentNode = new HashMap<>();
            contentNode.put("parts", parts);

            List<Map<String, Object>> contents = new ArrayList<>();
            contents.add(contentNode);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", contents);

            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                if (!textNode.isMissingNode()) {
                    return textNode.asText().trim();
                }
            } else {
                LOG.error("Failed to call Gemini API, status code: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            LOG.error("Exception when calling Gemini API: {}", e.getMessage(), e);
        }
        return "";
    }

    private String buildGeminiPrompt(
        String userQuery,
        List<PhimDTO> showing,
        List<PhimDTO> comingSoon,
        List<SuatChieuDTO> todayAndUpcoming,
        List<DichVuFBDTO> activeCombos,
        DashboardDTO dashboard,
        boolean adminUser
    ) {
        StringBuilder context = new StringBuilder();
        context.append("=== THÔNG TIN HỆ THỐNG CINEMATICK ===\n");
        context
            .append("Thời gian hiện tại của hệ thống: ")
            .append(ZonedDateTime.now(ZoneId.systemDefault()).format(SHOWTIME_FORMATTER))
            .append("\n\n");

        // 1. Thông tin tài khoản đăng nhập
        context.append("--- THÔNG TIN NGƯỜI DÙNG ĐANG CHAT ---\n");
        var userLoginOpt = SecurityUtils.getCurrentUserLogin();
        if (userLoginOpt.isPresent()) {
            String email = userLoginOpt.get();
            context.append("- Trạng thái: Đã đăng nhập\n");
            context.append("- Email/Tài khoản: ").append(email).append("\n");
            context.append("- Vai trò (Role): ").append(adminUser ? "ADMIN" : "USER").append("\n");

            nguoiDungRepository
                .findOneByEmailIgnoreCase(email)
                .ifPresent(user -> {
                    context.append("- Họ tên: ").append(user.getHoTen() != null ? user.getHoTen() : "Chưa cập nhật").append("\n");
                    context
                        .append("- Số điện thoại: ")
                        .append(user.getSoDienThoai() != null ? user.getSoDienThoai() : "Chưa cập nhật")
                        .append("\n");
                    context
                        .append("- Điểm tích lũy thành viên: ")
                        .append(user.getDiemTichLuy() != null ? user.getDiemTichLuy() : 0)
                        .append(" điểm\n");
                });

            // Danh sách vé đã mua của người dùng
            String bookings = buildMyBookingReply();
            context.append("- Lịch sử đặt vé cá nhân:\n").append(bookings).append("\n");
        } else {
            context.append("- Trạng thái: Chưa đăng nhập (Khách vãng lai)\n");
            context.append("- Vai trò (Role): USER\n");
        }
        context.append("\n");

        // 2. Danh sách phim đang chiếu
        context.append("--- DANH SÁCH PHIM ĐANG CHIẾU ---\n");
        if (showing.isEmpty()) {
            context.append("Không có phim nào đang chiếu.\n");
        } else {
            for (PhimDTO p : showing) {
                context.append(
                    String.format(
                        "• %s | Thể loại: %s | Thời lượng: %s phút | Đạo diễn: %s | Khởi chiếu: %s\n",
                        p.getTenPhim(),
                        p.getTheLoai() != null ? p.getTheLoai() : "Chưa rõ",
                        p.getThoiLuong() != null ? p.getThoiLuong() : "Chưa rõ",
                        p.getDaoDien() != null ? p.getDaoDien() : "Chưa rõ",
                        p.getNgayKhoiChieu() != null ? p.getNgayKhoiChieu().format(DATE_ONLY_FORMATTER) : "Chưa rõ"
                    )
                );
                if (p.getMoTa() != null && !p.getMoTa().isBlank()) {
                    context.append("  Mô tả: ").append(p.getMoTa()).append("\n");
                }
            }
        }
        context.append("\n");

        // 3. Danh sách phim sắp chiếu
        context.append("--- DANH SÁCH PHIM SẮP CHIẾU ---\n");
        if (comingSoon.isEmpty()) {
            context.append("Không có phim nào sắp chiếu.\n");
        } else {
            for (PhimDTO p : comingSoon) {
                context.append(
                    String.format(
                        "• %s | Thể loại: %s | Khởi chiếu: %s\n",
                        p.getTenPhim(),
                        p.getTheLoai() != null ? p.getTheLoai() : "Chưa rõ",
                        p.getNgayKhoiChieu() != null ? p.getNgayKhoiChieu().format(DATE_ONLY_FORMATTER) : "Chưa rõ"
                    )
                );
            }
        }
        context.append("\n");

        // 4. Lịch chiếu & Suất chiếu hôm nay/ngày tới
        context.append("--- LỊCH CHIẾU & SUẤT CHIẾU HÔM NAY/SẮP TỚI ---\n");
        if (todayAndUpcoming.isEmpty()) {
            context.append("Hiện chưa có suất chiếu mới nào được lên lịch.\n");
        } else {
            for (SuatChieuDTO s : todayAndUpcoming) {
                context.append("• ").append(formatShowtimeSummary(s)).append("\n");
            }
        }
        context.append("\n");

        // 5. Các combo F&B
        context.append("--- COMBO BẮP NƯỚC F&B ĐANG BÁN ---\n");
        if (activeCombos.isEmpty()) {
            context.append("Không có combo nào đang bán.\n");
        } else {
            for (DichVuFBDTO c : activeCombos) {
                context.append(String.format("• %s — Giá: %s đ\n", c.getTenCombo(), formatMoney(c.getGia())));
            }
        }
        context.append("\n");

        // 6. Các quy định và FAQ của rạp
        context.append("--- QUY ĐỊNH & CHÍNH SÁCH RẠP ---\n");
        context.append(
            "- Bảng giá vé tham khảo: Ghế thường: 60.000đ - 90.000đ/vé; Ghế VIP: 80.000đ - 110.000đ/vé; Ghế đôi (Sweetbox): 140.000đ - 180.000đ/cặp. Giá thực tế hiển thị lúc chọn ghế.\n"
        );
        context.append("- Chính sách đổi trả vé: Vé đã thanh toán thành công KHÔNG THỂ HOÀN TRẢ hoặc THAY ĐỔI.\n");
        context.append("- Quy định ăn uống: Không được phép mang đồ ăn thức uống từ bên ngoài vào rạp.\n");
        context.append(
            "- Phân loại độ tuổi: P (Mọi độ tuổi), K (Dưới 13 xem cùng giám hộ), T13 (từ đủ 13 tuổi trở lên), T16 (từ đủ 16 tuổi trở lên), T18 (từ đủ 18 tuổi trở lên, cần CCCD).\n"
        );
        context.append(
            "- Hướng dẫn đặt vé: Chọn phim đang chiếu -> Chọn suất chiếu -> Chọn ghế -> Thêm combo F&B nếu muốn -> Thanh toán. Nhận vé QR qua email.\n"
        );
        context.append("\n");

        // 7. Thống kê quản trị (Chỉ load và add khi user là ADMIN)
        if (adminUser && dashboard != null) {
            context.append("--- THỐNG KÊ QUẢN TRỊ (CHỈ DÀNH CHO ADMIN) ---\n");
            context.append("- Doanh thu bán vé: ").append(formatMoney(dashboard.getTotalRevenue())).append(" đ\n");
            if (dashboard.getTotalFbRevenue() != null) {
                context.append("- Doanh thu F&B: ").append(formatMoney(dashboard.getTotalFbRevenue())).append(" đ\n");
            }
            context.append("- Số vé đã bán: ").append(dashboard.getTotalTicketsSold()).append(" vé\n");
            context.append("- Số hóa đơn thành công: ").append(dashboard.getPaidInvoicesCount()).append(" đơn\n");
            context.append("- Tỷ lệ lấp đầy ghế trung bình: ").append(formatPercent(dashboard.getAverageOccupancyRate())).append("\n");
            context.append("- Tỷ lệ mua combo F&B kèm theo: ").append(formatPercent(dashboard.getComboAttachRate())).append("\n");
            if (dashboard.getReviewCount() > 0) {
                context
                    .append("- Đánh giá trung bình: ")
                    .append(dashboard.getAverageMovieRating())
                    .append("/5 (")
                    .append(dashboard.getReviewCount())
                    .append(" lượt đánh giá)\n");
            }

            // Top phim theo doanh thu
            if (dashboard.getTopMovies() != null && !dashboard.getTopMovies().isEmpty()) {
                context.append("- Top phim doanh thu cao:\n");
                dashboard
                    .getTopMovies()
                    .forEach(item ->
                        context.append(
                            String.format("  • %s: %s đ (%s vé)\n", item.getName(), formatMoney(item.getRevenue()), item.getCount())
                        )
                    );
            }

            // Phòng chiếu hiệu suất
            if (dashboard.getTopRooms() != null && !dashboard.getTopRooms().isEmpty()) {
                context.append("- Hiệu suất phòng chiếu:\n");
                dashboard
                    .getTopRooms()
                    .forEach(room ->
                        context.append(
                            String.format(
                                "  • %s: Lấp đầy %s | %s vé | Doanh thu %s đ\n",
                                room.getName(),
                                formatPercent(room.getPercentage()),
                                room.getCount(),
                                formatMoney(room.getRevenue())
                            )
                        )
                    );
            }

            // Combo F&B bán chạy
            if (dashboard.getTopCombos() != null && !dashboard.getTopCombos().isEmpty()) {
                context.append("- Thống kê combo F&B bán chạy:\n");
                dashboard
                    .getTopCombos()
                    .forEach(combo ->
                        context.append(
                            String.format(
                                "  • %s: Bán được %s combo | Doanh thu %s đ\n",
                                combo.getName(),
                                combo.getCount(),
                                formatMoney(combo.getRevenue())
                            )
                        )
                    );
            }

            // Khung giờ cao điểm
            if (dashboard.getPeakHours() != null && !dashboard.getPeakHours().isEmpty()) {
                context.append("- Khung giờ cao điểm (phân bổ vé):\n");
                dashboard
                    .getPeakHours()
                    .forEach(slot ->
                        context.append(
                            String.format(
                                "  • %s: %s vé | Doanh thu %s đ\n",
                                slot.getName(),
                                slot.getCount(),
                                formatMoney(slot.getRevenue())
                            )
                        )
                    );
            }

            // Phân bổ loại vé
            if (dashboard.getTicketTypeBreakdown() != null && !dashboard.getTicketTypeBreakdown().isEmpty()) {
                context.append("- Phân bổ loại vé:\n");
                dashboard
                    .getTicketTypeBreakdown()
                    .forEach(type ->
                        context.append(
                            String.format("  • %s: %s vé (%s)\n", type.getName(), type.getCount(), formatPercent(type.getPercentage()))
                        )
                    );
            }

            // Trạng thái hóa đơn
            if (dashboard.getStatusBreakdown() != null && !dashboard.getStatusBreakdown().isEmpty()) {
                context.append("- Trạng thái đơn hàng:\n");
                dashboard
                    .getStatusBreakdown()
                    .forEach(status ->
                        context.append(
                            String.format(
                                "  • %s: %s đơn (%s)\n",
                                status.getName(),
                                status.getCount(),
                                formatPercent(status.getPercentage())
                            )
                        )
                    );
            }

            // So sánh kỳ trước
            if (dashboard.getRevenueComparison() != null) {
                context
                    .append("- So sánh doanh thu kỳ trước: Thay đổi ")
                    .append(dashboard.getRevenueComparison().getDeltaPercent())
                    .append("%\n");
            }
            if (dashboard.getTicketsComparison() != null) {
                context
                    .append("- So sánh vé bán kỳ trước: Thay đổi ")
                    .append(dashboard.getTicketsComparison().getDeltaPercent())
                    .append("%\n");
            }
            context.append("\n");
        }

        // Xây dựng chỉ dẫn prompt chi tiết cho mô hình ngôn ngữ
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là trợ lý AI thông minh của rạp phim CinemaTick.\n");
        prompt.append("NHIỆM VỤ:\n");
        prompt.append(
            "1. Trả lời câu hỏi của người dùng dựa TRỰC TIẾP và CHỈ dựa trên phần \"=== THÔNG TIN HỆ THỐNG CINEMATICK ===\" ở bên dưới.\n"
        );
        prompt.append(
            "2. Hãy trả lời tự nhiên, lịch sự, trôi chảy, chuyên nghiệp bằng tiếng Việt. Tránh lặp lại nguyên văn các câu lệnh hệ thống.\n"
        );
        prompt.append("3. CHÍNH SÁCH BẢO MẬT PHÂN QUYỀN (Cực kỳ quan trọng):\n");
        if (adminUser) {
            prompt.append(
                "   - Người dùng hiện tại có vai trò là ADMIN. Bạn ĐƯỢC PHÉP trả lời các câu hỏi về doanh thu, số vé bán được, các thống kê phòng chiếu, F&B, giờ cao điểm, so sánh kỳ trước dựa trên dữ liệu thống kê trong context.\n"
            );
        } else {
            prompt.append(
                "   - Người dùng hiện tại có vai trò là USER thường. Bạn TUYỆT ĐỐI KHÔNG ĐƯỢC tiết lộ, thảo luận hoặc trả lời các thông tin liên quan đến doanh thu, số vé bán được, tỷ lệ lấp đầy, hay bất cứ thống kê, báo cáo quản trị nào. Nếu người dùng hỏi các câu hỏi thống kê/doanh thu, hãy trả lời lịch sự: \"Xin lỗi bạn, thông tin thống kê doanh thu và báo cáo quản lý chỉ dành cho Admin. Mình có thể hỗ trợ bạn thông tin về phim đang chiếu, lịch chiếu, giá vé và combo bắp nước nhé!\".\n"
            );
        }
        prompt.append(
            "4. Nếu người dùng hỏi thông tin không có trong Context, hãy trả lời lịch sự rằng bạn chưa có thông tin đó và hướng dẫn họ liên hệ với nhân viên tại quầy hoặc hotline của CinemaTick để được hỗ trợ chi tiết.\n"
        );
        prompt.append(
            "5. Không nhắc đến các từ khóa như \"Context\", \"Ngữ cảnh\", \"Cơ sở dữ liệu\", \"Hệ thống cung cấp\" hay cách bạn nhận dữ liệu trong câu trả lời của mình.\n\n"
        );
        prompt.append(context);
        prompt.append("\n=== CÂU HỎI CỦA NGƯỜI DÙNG ===\n");
        prompt.append(userQuery);

        return prompt.toString();
    }

    // =========================================================================
    // Core reply builder
    // =========================================================================

    private String buildReply(
        String normalized,
        List<PhimDTO> showing,
        List<PhimDTO> comingSoon,
        List<SuatChieuDTO> todayAndUpcoming,
        List<SuatChieuDTO> upcomingOnly,
        List<DichVuFBDTO> activeCombos,
        DashboardDTO dashboard,
        boolean adminUser,
        boolean todayQuery,
        boolean weekQuery,
        boolean monthQuery
    ) {
        // 1. Chào hỏi
        if (isGreeting(normalized)) {
            return adminUser
                ? "Chào admin! 👋 Tôi là trợ lý CinemaTick. Bạn có thể hỏi tôi:\n" +
                "📊 Doanh thu hôm nay / tuần này / tháng này\n" +
                "🎟️ Số vé bán được, tỷ lệ lấp đầy ghế\n" +
                "🏆 Phòng chiếu hiệu quả nhất, combo bán chạy\n" +
                "⏰ Giờ cao điểm, phân loại vé, trạng thái đơn hàng\n" +
                "⭐ Đánh giá phim, phim hot nhất\n" +
                "🎬 Lịch chiếu, phim đang chiếu, thao tác quản trị"
                : "Chào bạn! Tôi là trợ lý CinemaTick. Tôi có thể hỗ trợ các thông tin:\n" +
                "🎬 Phim đang chiếu, phim sắp chiếu\n" +
                "⏰ Lịch chiếu phim, suất chiếu hôm nay, suất gần nhất\n" +
                "🎫 Tra cứu giá vé, hướng dẫn đặt vé trực tuyến\n" +
                "🍿 Combo bắp nước F&B hiện có\n" +
                "👤 Lịch sử đặt vé cá nhân, điểm tích lũy thành viên\n" +
                "ℹ️ Quy định rạp (hủy vé, độ tuổi, đồ ăn ngoài)\n" +
                "Bạn cần hỏi thông tin nào?";
        }

        // 2. Câu hỏi thống kê tổng hợp / doanh thu / số vé (chỉ admin)
        if (isStatsQuery(normalized)) {
            return adminUser ? formatDashboardReply(dashboard, todayQuery, weekQuery, monthQuery) : denyAdminStats();
        }

        // 2b. Thống kê tuần / tháng (chỉ admin)
        if (isWeekStatsQuery(normalized)) {
            return adminUser ? formatDashboardReply(dashboard, false, true, false) : denyAdminStats();
        }
        if (isMonthStatsQuery(normalized)) {
            return adminUser ? formatDashboardReply(dashboard, false, false, true) : denyAdminStats();
        }

        // 2c. Thống kê phòng chiếu (chỉ admin)
        if (isRoomStatsQuery(normalized)) {
            return adminUser ? formatRoomStatsReply(dashboard) : denyAdminStats();
        }

        // 2d. Thống kê combo F&B (chỉ admin)
        if (isComboStatsQuery(normalized)) {
            return adminUser ? formatComboStatsReply(dashboard) : denyAdminStats();
        }

        // 2e. Giờ cao điểm (chỉ admin)
        if (isPeakHourQuery(normalized)) {
            return adminUser ? formatPeakHourReply(dashboard) : denyAdminStats();
        }

        // 2f. Phân loại vé (chỉ admin)
        if (isTicketTypeQuery(normalized)) {
            return adminUser ? formatTicketTypeReply(dashboard) : denyAdminStats();
        }

        // 2g. So sánh kỳ trước (chỉ admin)
        if (isCompareQuery(normalized)) {
            return adminUser ? formatCompareReply(dashboard, todayQuery, weekQuery, monthQuery) : denyAdminStats();
        }

        // 2h. Đánh giá / review phim (chỉ admin)
        if (isReviewStatsQuery(normalized)) {
            return adminUser ? formatReviewStatsReply(dashboard) : denyAdminStats();
        }

        // 2i. Phân tích trạng thái đơn hàng (chỉ admin)
        if (isStatusBreakdownQuery(normalized)) {
            return adminUser ? formatStatusBreakdownReply(dashboard) : denyAdminStats();
        }

        // 3. Hướng dẫn quản trị (chỉ admin)
        if (isAdminWorkflowQuery(normalized)) {
            return adminUser ? formatAdminWorkflowReply(normalized) : denyAdminStats();
        }

        // 3a. Tra cứu lịch sử đặt vé cá nhân
        if (isMyBookingQuery(normalized)) {
            return buildMyBookingReply();
        }

        // 3b. Tra cứu điểm tích lũy thành viên
        if (isPointsQuery(normalized)) {
            return buildPointsReply();
        }

        // 3c. Lọc phim theo thể loại
        if (isGenreQuery(normalized)) {
            return buildGenreReply(normalized, showing, comingSoon);
        }

        // 3d. Tra cứu bảng giá vé
        if (isPriceQuery(normalized)) {
            return buildPriceReply();
        }

        // 3e. Quy định rạp & FAQs
        if (isPolicyQuery(normalized)) {
            return buildPolicyReply(normalized);
        }

        // 4. Phim hot / bán chạy
        if (isHotMovieQuery(normalized)) {
            return formatHotMovieReply(showing, dashboard, adminUser);
        }

        // 5. Suất chiếu gần nhất
        if (isNearestShowtimeQuery(normalized)) {
            return describeNearestShowtime(upcomingOnly, todayAndUpcoming);
        }

        // 6. Câu hỏi có tên phim cụ thể → tìm suất của phim đó
        String movieReply = buildMovieSpecificReply(normalized, showing, comingSoon, todayAndUpcoming);
        if (!movieReply.isBlank()) {
            return movieReply;
        }

        // 7. Suất chiếu hôm nay / tổng quát
        if (isShowtimeQuery(normalized)) {
            return formatShowtimeList(todayAndUpcoming, upcomingOnly, todayQuery);
        }

        // 8. Combo F&B
        if (isComboQuery(normalized)) {
            return formatComboReply(activeCombos);
        }

        // 9. Hướng dẫn đặt vé
        if (isBookingGuideQuery(normalized)) {
            return (
                "Để đặt vé tại CinemaTick:\n" +
                "① Chọn phim đang chiếu → ② Chọn suất chiếu → ③ Chọn ghế → " +
                "④ Thêm combo F&B nếu muốn → ⑤ Thanh toán.\n" +
                "Ghế được giữ tạm trong vài phút trong lúc thanh toán. " +
                "Sau khi thanh toán thành công, bạn nhận mã QR vé qua email."
            );
        }

        // 10. Phim đang chiếu
        if (isShowingMovieQuery(normalized)) {
            return formatShowingMoviesReply(showing, todayAndUpcoming);
        }

        // 11. Phim sắp chiếu
        if (isComingSoonQuery(normalized)) {
            return formatComingSoonReply(comingSoon);
        }

        // 12. Câu hỏi về ghế / phòng chiếu
        if (isSeatQuery(normalized)) {
            return (
                "CinemaTick có 3 loại ghế: Ghế thường, Ghế VIP và Ghế đôi (Sweetbox). " +
                "Giá vé tùy suất chiếu, bạn có thể xem khi chọn suất. " +
                "Ghế màu xanh = còn trống, màu đỏ = đã đặt, màu vàng = đang giữ."
            );
        }

        return "";
    }

    // =========================================================================
    // Load dữ liệu suất chiếu
    // =========================================================================

    /**
     * Lấy tất cả suất chiếu từ đầu ngày hôm nay trở đi (bao gồm cả suất đang
     * chiếu).
     * Điều này giúp chatbot trả lời được "hôm nay có suất gì" dù suất đã bắt đầu.
     */
    private List<SuatChieuDTO> loadTodayAndUpcomingShowtimes() {
        ZonedDateTime startOfToday = LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault());
        return suatChieuService
            .search(null, null, null, null, null, null, null)
            .stream()
            .filter(s -> s.getThoiGianBatDau() != null)
            .filter(s -> !s.getThoiGianBatDau().isBefore(startOfToday))
            .sorted(Comparator.comparing(SuatChieuDTO::getThoiGianBatDau))
            .limit(100)
            .toList();
    }

    /**
     * Lọc chỉ những suất chiếu chưa bắt đầu (tính từ thời điểm hiện tại).
     */
    private List<SuatChieuDTO> filterUpcomingOnly(List<SuatChieuDTO> showtimes) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        return showtimes.stream().filter(s -> s.getThoiGianBatDau() != null && !s.getThoiGianBatDau().isBefore(now)).toList();
    }

    // =========================================================================
    // Tìm kiếm phim cụ thể
    // =========================================================================

    private String buildMovieSpecificReply(
        String normalized,
        List<PhimDTO> showing,
        List<PhimDTO> comingSoon,
        List<SuatChieuDTO> showtimes
    ) {
        PhimDTO movie = findMovieByTitle(normalized, showing);
        if (movie == null) movie = findMovieByTitle(normalized, comingSoon);
        if (movie == null) movie = findMovieByTitleFromShowtimes(normalized, showtimes);
        if (movie == null) return "";

        final PhimDTO selectedMovie = movie;
        if (isMovieDetailQuery(normalized)) {
            StringBuilder detail = new StringBuilder("🎬 **Thông tin phim \"").append(selectedMovie.getTenPhim()).append("\":**\n");
            detail
                .append("• **Thể loại:** ")
                .append(selectedMovie.getTheLoai() != null ? selectedMovie.getTheLoai() : "Chưa rõ")
                .append("\n");
            detail
                .append("• **Thời lượng:** ")
                .append(selectedMovie.getThoiLuong() != null ? selectedMovie.getThoiLuong() + " phút" : "Chưa rõ")
                .append("\n");
            detail
                .append("• **Đạo diễn:** ")
                .append(selectedMovie.getDaoDien() != null ? selectedMovie.getDaoDien() : "Chưa rõ")
                .append("\n");
            detail
                .append("• **Diễn viên:** ")
                .append(selectedMovie.getDienVien() != null ? selectedMovie.getDienVien() : "Chưa rõ")
                .append("\n");
            if (selectedMovie.getNgayKhoiChieu() != null) {
                detail.append("• **Khởi chiếu:** ").append(selectedMovie.getNgayKhoiChieu().format(DATE_ONLY_FORMATTER)).append("\n");
            }
            detail
                .append("• **Nội dung:** ")
                .append(selectedMovie.getMoTa() != null ? selectedMovie.getMoTa() : "Chưa có mô tả chi tiết.")
                .append("\n");
            return detail.toString().trim();
        }
        String movieKey = normalize(selectedMovie.getTenPhim());

        List<SuatChieuDTO> movieShowtimes = showtimes
            .stream()
            .filter(s -> s.getPhim() != null && s.getPhim().getTenPhim() != null)
            .filter(s -> normalize(s.getPhim().getTenPhim()).contains(movieKey))
            .sorted(Comparator.comparing(SuatChieuDTO::getThoiGianBatDau, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        if (movieShowtimes.isEmpty()) {
            boolean isShowing = showing.stream().anyMatch(p -> p.getId() != null && p.getId().equals(selectedMovie.getId()));
            if (isShowing) {
                return (
                    "Phim \"" +
                    selectedMovie.getTenPhim() +
                    "\" đang trong danh sách chiếu, nhưng hiện chưa có suất chiếu mới nào được lên lịch."
                );
            }
            return "Phim \"" + selectedMovie.getTenPhim() + "\" hiện đang trong danh sách sắp chiếu, chưa có lịch chiếu cụ thể.";
        }

        // Nhóm theo ngày để hiển thị rõ hơn
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        List<SuatChieuDTO> todayShowtimes = movieShowtimes
            .stream()
            .filter(s -> s.getThoiGianBatDau().toLocalDate().equals(today))
            .limit(5)
            .toList();
        List<SuatChieuDTO> otherShowtimes = movieShowtimes
            .stream()
            .filter(s -> !s.getThoiGianBatDau().toLocalDate().equals(today))
            .limit(3)
            .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("Phim \"").append(selectedMovie.getTenPhim()).append("\"");

        if (!todayShowtimes.isEmpty()) {
            sb.append(" — Suất hôm nay: ");
            sb.append(todayShowtimes.stream().map(this::formatShowtimeSummary).collect(Collectors.joining("; ")));
        }
        if (!otherShowtimes.isEmpty()) {
            if (!todayShowtimes.isEmpty()) sb.append(". Suất sắp tới: ");
            else sb.append(" — Suất sắp tới: ");
            sb.append(otherShowtimes.stream().map(this::formatShowtimeSummary).collect(Collectors.joining("; ")));
        }
        sb.append(".");
        return sb.toString();
    }

    private PhimDTO findMovieByTitle(String normalized, List<PhimDTO> movies) {
        // Tìm khớp dài nhất để tránh nhầm phim ngắn tên
        PhimDTO best = null;
        int bestLen = 0;
        for (PhimDTO movie : movies) {
            if (movie.getTenPhim() == null) continue;
            String movieName = normalize(movie.getTenPhim());
            if (!movieName.isBlank() && containsWord(normalized, movieName) && movieName.length() > bestLen) {
                best = movie;
                bestLen = movieName.length();
            }
        }
        return best;
    }

    private PhimDTO findMovieByTitleFromShowtimes(String normalized, List<SuatChieuDTO> showtimes) {
        PhimDTO best = null;
        int bestLen = 0;
        for (SuatChieuDTO showtime : showtimes) {
            if (showtime.getPhim() == null || showtime.getPhim().getTenPhim() == null) continue;
            String movieName = normalize(showtime.getPhim().getTenPhim());
            if (!movieName.isBlank() && containsWord(normalized, movieName) && movieName.length() > bestLen) {
                best = showtime.getPhim();
                bestLen = movieName.length();
            }
        }
        return best;
    }

    // =========================================================================
    // Format responses
    // =========================================================================

    private String describeNearestShowtime(List<SuatChieuDTO> upcoming, List<SuatChieuDTO> todayAndUpcoming) {
        // Ưu tiên suất chưa bắt đầu
        if (!upcoming.isEmpty()) {
            SuatChieuDTO nearest = upcoming
                .stream()
                .filter(s -> s.getThoiGianBatDau() != null)
                .min(Comparator.comparing(SuatChieuDTO::getThoiGianBatDau))
                .orElse(null);
            if (nearest != null) {
                return "Suất chiếu gần nhất sắp diễn ra: " + formatShowtimeSummary(nearest) + ".";
            }
        }
        // Fallback: suất đang chiếu hoặc đã chiếu hôm nay
        if (!todayAndUpcoming.isEmpty()) {
            SuatChieuDTO latest = todayAndUpcoming.get(0);
            return "Suất chiếu gần nhất hôm nay: " + formatShowtimeSummary(latest) + " (có thể đã bắt đầu rồi).";
        }
        return "Hiện chưa có lịch chiếu mới nào được cập nhật. Vui lòng kiểm tra lại sau.";
    }

    private String formatShowtimeList(List<SuatChieuDTO> todayAndUpcoming, List<SuatChieuDTO> upcomingOnly, boolean todayQuery) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        if (todayQuery) {
            // Lấy tất cả suất hôm nay (kể cả đã bắt đầu)
            List<SuatChieuDTO> todayShowtimes = todayAndUpcoming
                .stream()
                .filter(s -> s.getThoiGianBatDau() != null && s.getThoiGianBatDau().toLocalDate().equals(today))
                .limit(8)
                .toList();

            if (todayShowtimes.isEmpty()) {
                return "Hôm nay chưa có suất chiếu nào được lên lịch.";
            }

            // Phân nhóm đang chiếu / sắp chiếu
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            List<SuatChieuDTO> nowShowing = todayShowtimes.stream().filter(s -> !s.getThoiGianBatDau().isAfter(now)).toList();
            List<SuatChieuDTO> upcoming = todayShowtimes.stream().filter(s -> s.getThoiGianBatDau().isAfter(now)).toList();

            StringBuilder sb = new StringBuilder("📅 Lịch chiếu hôm nay (" + today.format(DATE_ONLY_FORMATTER) + "):\n");
            if (!nowShowing.isEmpty()) {
                sb
                    .append("🎬 Đang chiếu: ")
                    .append(nowShowing.stream().map(this::formatShowtimeSummary).collect(Collectors.joining("; ")))
                    .append(".\n");
            }
            if (!upcoming.isEmpty()) {
                sb
                    .append("⏰ Sắp chiếu: ")
                    .append(upcoming.stream().map(this::formatShowtimeSummary).collect(Collectors.joining("; ")))
                    .append(".");
            }
            return sb.toString();
        }

        // Tổng quát — lấy suất sắp tới
        if (upcomingOnly.isEmpty()) {
            if (!todayAndUpcoming.isEmpty()) {
                String text = todayAndUpcoming.stream().limit(6).map(this::formatShowtimeSummary).collect(Collectors.joining("; "));
                return "Các suất chiếu hôm nay: " + text + ".";
            }
            return "Hiện chưa có suất chiếu mới nào. Vui lòng quay lại sau!";
        }

        String text = upcomingOnly.stream().limit(6).map(this::formatShowtimeSummary).collect(Collectors.joining("; "));
        return "Một số suất chiếu sắp tới: " + text + ".";
    }

    private String formatShowingMoviesReply(List<PhimDTO> showing, List<SuatChieuDTO> todayAndUpcoming) {
        if (showing.isEmpty()) {
            return "Hiện chưa có phim đang chiếu. Bạn có thể xem danh sách phim sắp chiếu!";
        }

        // Thu thập tên phim có suất chiếu hôm nay/sắp tới
        java.util.Set<String> moviesWithShowtimes = todayAndUpcoming
            .stream()
            .filter(s -> s.getPhim() != null && s.getPhim().getTenPhim() != null)
            .map(s -> s.getPhim().getTenPhim())
            .collect(Collectors.toSet());

        String showingText = showing
            .stream()
            .limit(8)
            .map(p -> {
                if (p.getTenPhim() == null) return null;
                return moviesWithShowtimes.contains(p.getTenPhim()) ? "🎬 " + p.getTenPhim() : p.getTenPhim();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", "));

        return (
            "Phim đang chiếu tại CinemaTick: " +
            showingText +
            ". (🎬 = có suất chiếu hôm nay/sắp tới. Hỏi thêm tên phim để xem lịch cụ thể!)"
        );
    }

    private String formatComingSoonReply(List<PhimDTO> comingSoon) {
        if (comingSoon.isEmpty()) {
            return "Hiện chưa có thông tin phim sắp chiếu mới.";
        }
        java.time.format.DateTimeFormatter localDateFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String text = comingSoon
            .stream()
            .limit(6)
            .map(p -> {
                if (p.getTenPhim() == null) return null;
                String date = p.getNgayKhoiChieu() != null ? " (" + p.getNgayKhoiChieu().format(localDateFmt) + ")" : "";
                return p.getTenPhim() + date;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", "));
        return "Phim sắp ra mắt tại CinemaTick: " + text + ".";
    }

    private String formatComboReply(List<DichVuFBDTO> activeCombos) {
        if (activeCombos.isEmpty()) {
            return "Hiện hệ thống chưa có combo F&B đang bán.";
        }
        String comboText = activeCombos
            .stream()
            .limit(5)
            .map(combo -> "• " + combo.getTenCombo() + " — " + formatMoney(combo.getGia()) + " đ")
            .collect(Collectors.joining("\n"));
        return "🍿 Combo F&B hiện có:\n" + comboText + "\nBạn có thể chọn combo khi đặt vé, trước bước thanh toán.";
    }

    private String buildFallbackReply(
        List<PhimDTO> showing,
        List<PhimDTO> comingSoon,
        List<SuatChieuDTO> todayAndUpcoming,
        boolean adminUser
    ) {
        if (adminUser) {
            return (
                "Mình chưa hiểu câu hỏi của bạn. Admin có thể hỏi:\n" +
                "📊 **Thống kê:** doanh thu hôm nay/tuần/tháng, so sánh kỳ trước\n" +
                "🎟️ **Vé & đơn hàng:** số vé bán, trạng thái đơn hàng, phân loại vé\n" +
                "🏟️ **Phòng chiếu:** phòng nào hiệu quả nhất, tỷ lệ lấp đầy\n" +
                "🍿 **Combo F&B:** combo nào bán chạy, tỷ lệ mua combo\n" +
                "⏰ **Giờ cao điểm:** khung giờ nào bán nhiều vé nhất\n" +
                "⭐ **Đánh giá:** phim nào được đánh giá cao nhất\n" +
                "🎬 **Vận hành:** thêm suất chiếu, quản lý phim, người dùng"
            );
        }

        StringBuilder sb = new StringBuilder("Mình chưa rõ câu hỏi của bạn. Bạn có thể hỏi về:\n");
        if (!showing.isEmpty()) {
            String s = showing.stream().limit(4).map(PhimDTO::getTenPhim).filter(Objects::nonNull).collect(Collectors.joining(", "));
            sb.append("🎬 Phim đang chiếu: ").append(s).append("\n");
        }
        if (!comingSoon.isEmpty()) {
            String s = comingSoon.stream().limit(3).map(PhimDTO::getTenPhim).filter(Objects::nonNull).collect(Collectors.joining(", "));
            sb.append("🔜 Sắp chiếu: ").append(s).append("\n");
        }
        if (!todayAndUpcoming.isEmpty()) {
            sb.append("⏰ Suất chiếu gần nhất, giá vé, cách đặt vé, combo F&B");
        }
        return sb.toString().trim();
    }

    private String formatShowtimeSummary(SuatChieuDTO showtime) {
        String movie = showtime.getPhim() != null && showtime.getPhim().getTenPhim() != null
            ? showtime.getPhim().getTenPhim()
            : "Chưa rõ phim";
        String room = showtime.getPhongChieu() != null && showtime.getPhongChieu().getTenPhong() != null
            ? showtime.getPhongChieu().getTenPhong()
            : "Phòng";
        String time = showtime.getThoiGianBatDau() != null ? showtime.getThoiGianBatDau().format(SHOWTIME_FORMATTER) : "Chưa rõ giờ";

        StringBuilder sb = new StringBuilder();
        sb.append(movie).append(" [").append(room).append("] ").append(time);

        if (showtime.getGiaThuong() != null || showtime.getGiaVip() != null) {
            String thuong = showtime.getGiaThuong() != null ? formatMoney(showtime.getGiaThuong()) + " đ" : "?";
            String vip = showtime.getGiaVip() != null ? formatMoney(showtime.getGiaVip()) + " đ" : "?";
            sb.append(" | Thường: ").append(thuong).append(", VIP: ").append(vip);
        }
        return sb.toString();
    }

    private String formatDashboardReply(DashboardDTO dashboard, boolean todayQuery, boolean weekQuery, boolean monthQuery) {
        if (dashboard == null) {
            return "Hiện mình chưa lấy được dữ liệu thống kê. Vui lòng thử lại.";
        }

        String scope;
        if (todayQuery) scope = "hôm nay";
        else if (weekQuery) scope = "7 ngày gần đây";
        else if (monthQuery) scope = "tháng này";
        else scope = "toàn bộ";

        String topMovie = dashboard.getTopMovies() != null && !dashboard.getTopMovies().isEmpty()
            ? dashboard.getTopMovies().get(0).getName()
            : "chưa có dữ liệu";
        String topRatedMovie = dashboard.getTopRatedMovies() != null && !dashboard.getTopRatedMovies().isEmpty()
            ? dashboard.getTopRatedMovies().get(0).getName()
            : "chưa có dữ liệu";
        long distinctBookers = countDistinctBookers(todayQuery);

        StringBuilder sb = new StringBuilder();
        sb.append("📊 **Thống kê ").append(scope).append("**\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("💰 Doanh thu vé: ").append(formatMoney(dashboard.getTotalRevenue())).append(" đ\n");
        if (dashboard.getTotalFbRevenue() != null && dashboard.getTotalFbRevenue().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("🍿 Doanh thu F&B: ").append(formatMoney(dashboard.getTotalFbRevenue())).append(" đ\n");
        }
        sb.append("🎟️ Vé đã bán: ").append(dashboard.getTotalTicketsSold()).append(" vé\n");
        sb.append("✅ Đơn thành công: ").append(dashboard.getPaidInvoicesCount()).append(" đơn\n");
        sb.append("👥 Khách đặt vé: ").append(distinctBookers).append(" người\n");
        sb.append("📈 Tỷ lệ lấp đầy ghế: ").append(formatPercent(dashboard.getAverageOccupancyRate())).append("\n");
        sb.append("🔀 Tỷ lệ mua combo: ").append(formatPercent(dashboard.getComboAttachRate())).append("\n");
        if (dashboard.getReviewCount() > 0) {
            sb
                .append("⭐ Đánh giá TB: ")
                .append(dashboard.getAverageMovieRating())
                .append("/5 (")
                .append(dashboard.getReviewCount())
                .append(" lượt)\n");
        }
        sb.append("🔥 Phim bán chạy nhất: ").append(topMovie).append("\n");
        sb.append("🏆 Phim đánh giá cao nhất: ").append(topRatedMovie);

        // Top 5 phim theo doanh thu
        if (dashboard.getTopMovies() != null && !dashboard.getTopMovies().isEmpty()) {
            sb.append("\n\n🎬 **Top phim theo doanh thu:**\n");
            dashboard
                .getTopMovies()
                .stream()
                .limit(5)
                .forEach(item ->
                    sb
                        .append("  • ")
                        .append(item.getName())
                        .append(" — ")
                        .append(formatMoney(item.getRevenue()))
                        .append(" đ")
                        .append(" (")
                        .append(item.getCount())
                        .append(" vé)\n")
                );
        }

        sb.append("\n💡 Gợi ý: Hỏi \"phòng chiếu nào hiệu quả nhất?\", \"combo nào bán chạy?\", \"giờ cao điểm nào?\"");
        return sb.toString();
    }

    private String formatRoomStatsReply(DashboardDTO dashboard) {
        if (dashboard == null || dashboard.getTopRooms() == null || dashboard.getTopRooms().isEmpty()) {
            return "Hiện chưa có dữ liệu thống kê phòng chiếu.";
        }
        StringBuilder sb = new StringBuilder("🏟️ **Hiệu suất phòng chiếu:**\n━━━━━━━━━━━━━━━━━━━━━━\n");
        dashboard
            .getTopRooms()
            .forEach(room ->
                sb
                    .append("  📍 ")
                    .append(room.getName())
                    .append(" — Lấp đầy: ")
                    .append(formatPercent(room.getPercentage()))
                    .append(" | Vé bán: ")
                    .append(room.getCount())
                    .append(" | DT: ")
                    .append(formatMoney(room.getRevenue()))
                    .append(" đ\n")
            );
        return sb.toString().trim();
    }

    private String formatComboStatsReply(DashboardDTO dashboard) {
        if (dashboard == null || dashboard.getTopCombos() == null || dashboard.getTopCombos().isEmpty()) {
            return "Hiện chưa có dữ liệu combo F&B.";
        }
        StringBuilder sb = new StringBuilder("🍿 **Combo F&B bán chạy:**\n━━━━━━━━━━━━━━━━━━━━━━\n");
        dashboard
            .getTopCombos()
            .forEach(combo ->
                sb
                    .append("  • ")
                    .append(combo.getName())
                    .append(" — Số lượng: ")
                    .append(combo.getCount())
                    .append(" | DT: ")
                    .append(formatMoney(combo.getRevenue()))
                    .append(" đ\n")
            );
        if (dashboard.getComboAttachRate() != null) {
            sb.append("\n📊 Tỷ lệ đơn có combo: ").append(formatPercent(dashboard.getComboAttachRate()));
            if (dashboard.getInvoicesWithCombo() > 0) {
                sb
                    .append(" (")
                    .append(dashboard.getInvoicesWithCombo())
                    .append("/")
                    .append(dashboard.getPaidInvoicesCount())
                    .append(" đơn)");
            }
        }
        return sb.toString();
    }

    private String formatPeakHourReply(DashboardDTO dashboard) {
        if (dashboard == null || dashboard.getPeakHours() == null || dashboard.getPeakHours().isEmpty()) {
            return "Hiện chưa có dữ liệu giờ cao điểm.";
        }
        StringBuilder sb = new StringBuilder("⏰ **Phân bổ vé theo khung giờ:**\n━━━━━━━━━━━━━━━━━━━━━━\n");
        long totalTickets = dashboard.getPeakHours().stream().mapToLong(DashboardItemDTO::getCount).sum();
        dashboard
            .getPeakHours()
            .forEach(slot -> {
                long pct = totalTickets > 0 ? ((slot.getCount() * 100) / totalTickets) : 0;
                sb
                    .append("  🕐 ")
                    .append(slot.getName())
                    .append(" — ")
                    .append(slot.getCount())
                    .append(" vé (")
                    .append(pct)
                    .append("%")
                    .append(") | DT: ")
                    .append(formatMoney(slot.getRevenue()))
                    .append(" đ\n");
            });
        return sb.toString().trim();
    }

    private String formatTicketTypeReply(DashboardDTO dashboard) {
        if (dashboard == null || dashboard.getTicketTypeBreakdown() == null || dashboard.getTicketTypeBreakdown().isEmpty()) {
            return "Hiện chưa có dữ liệu phân loại vé.";
        }
        StringBuilder sb = new StringBuilder("🎫 **Phân loại vé đã bán:**\n━━━━━━━━━━━━━━━━━━━━━━\n");
        dashboard
            .getTicketTypeBreakdown()
            .forEach(type ->
                sb
                    .append("  • ")
                    .append(type.getName())
                    .append(" — ")
                    .append(type.getCount())
                    .append(" vé")
                    .append(" (")
                    .append(formatPercent(type.getPercentage()))
                    .append(")\n")
            );
        sb.append("\n📊 Tổng vé: ").append(dashboard.getTotalTicketsSold());
        return sb.toString();
    }

    private String formatCompareReply(DashboardDTO dashboard, boolean todayQuery, boolean weekQuery, boolean monthQuery) {
        if (dashboard == null) {
            return "Hiện chưa có dữ liệu để so sánh.";
        }
        String scope;
        if (todayQuery) scope = "hôm nay so với hôm qua";
        else if (weekQuery) scope = "tuần này so với tuần trước";
        else if (monthQuery) scope = "tháng này so với tháng trước";
        else scope = "kỳ hiện tại so với kỳ trước";

        StringBuilder sb = new StringBuilder("📈 **So sánh " + scope + ":**\n━━━━━━━━━━━━━━━━━━━━━━\n");
        if (dashboard.getRevenueComparison() != null) {
            var r = dashboard.getRevenueComparison();
            String trend = r.getDeltaPercent() != null && r.getDeltaPercent().compareTo(BigDecimal.ZERO) >= 0 ? "▲" : "▼";
            sb
                .append("💰 Doanh thu: ")
                .append(formatMoney(dashboard.getTotalRevenue()))
                .append(" đ ")
                .append(trend)
                .append(" ")
                .append(r.getDeltaPercent())
                .append("%\n");
        }
        if (dashboard.getTicketsComparison() != null) {
            var t = dashboard.getTicketsComparison();
            String trend = t.getDeltaPercent() != null && t.getDeltaPercent().compareTo(BigDecimal.ZERO) >= 0 ? "▲" : "▼";
            sb
                .append("🎟️ Vé bán: ")
                .append(dashboard.getTotalTicketsSold())
                .append(" " + trend + " ")
                .append(t.getDeltaPercent())
                .append("%\n");
        }
        if (dashboard.getUsersComparison() != null) {
            var u = dashboard.getUsersComparison();
            String trend = u.getDeltaPercent() != null && u.getDeltaPercent().compareTo(BigDecimal.ZERO) >= 0 ? "▲" : "▼";
            sb
                .append("👥 Người dùng: ")
                .append(dashboard.getTotalUsers())
                .append(" " + trend + " ")
                .append(u.getDeltaPercent())
                .append("%\n");
        }
        return sb.toString().trim();
    }

    private String formatReviewStatsReply(DashboardDTO dashboard) {
        if (dashboard == null) {
            return "Hiện chưa có dữ liệu đánh giá.";
        }
        StringBuilder sb = new StringBuilder("⭐ **Thống kê đánh giá phim:**\n━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("📝 Tổng lượt đánh giá: ").append(dashboard.getReviewCount()).append("\n");
        sb
            .append("⭐ Điểm TB toàn hệ thống: ")
            .append(dashboard.getAverageMovieRating() != null ? dashboard.getAverageMovieRating() : "N/A")
            .append("/5\n");
        if (dashboard.getTopRatedMovies() != null && !dashboard.getTopRatedMovies().isEmpty()) {
            sb.append("\n🏆 **Top phim được đánh giá cao:**\n");
            dashboard
                .getTopRatedMovies()
                .forEach(movie ->
                    sb
                        .append("  • ")
                        .append(movie.getName())
                        .append(" — ⭐ ")
                        .append(movie.getAverageRating())
                        .append("/5")
                        .append(" (")
                        .append(movie.getCount())
                        .append(" lượt)\n")
                );
        }
        return sb.toString().trim();
    }

    private String formatStatusBreakdownReply(DashboardDTO dashboard) {
        if (dashboard == null || dashboard.getStatusBreakdown() == null || dashboard.getStatusBreakdown().isEmpty()) {
            return "Hiện chưa có dữ liệu trạng thái đơn hàng.";
        }
        StringBuilder sb = new StringBuilder("📋 **Trạng thái đơn hàng:**\n━━━━━━━━━━━━━━━━━━━━━━\n");
        dashboard
            .getStatusBreakdown()
            .forEach(status ->
                sb
                    .append("  • ")
                    .append(status.getName())
                    .append(": ")
                    .append(status.getCount())
                    .append(" đơn")
                    .append(" (")
                    .append(formatPercent(status.getPercentage()))
                    .append(")\n")
            );
        return sb.toString().trim();
    }

    private long countDistinctBookers(boolean todayQuery) {
        List<HoaDon> invoices;
        if (todayQuery) {
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            ZonedDateTime from = today.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime to = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1);
            invoices = hoaDonRepository.findAllByCreatedAtBetweenWithUser(from, to);
        } else {
            invoices = hoaDonRepository.findAllWithToOneRelationships();
        }

        long withAccount = invoices
            .stream()
            .filter(this::isPaidInvoice)
            .map(HoaDon::getNguoiDung)
            .filter(Objects::nonNull)
            .map(nguoiDung -> nguoiDung.getId())
            .filter(Objects::nonNull)
            .distinct()
            .count();

        long guestInvoices = invoices.stream().filter(this::isPaidInvoice).filter(invoice -> invoice.getNguoiDung() == null).count();

        return withAccount + guestInvoices;
    }

    private boolean isPaidInvoice(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getTrangThai() == null) return false;
        String s = hoaDon.getTrangThai().trim().toUpperCase(Locale.ROOT);
        return "2".equals(s) || "PAID".equals(s) || "DONE".equals(s) || "SUCCESS".equals(s) || "DA_THANH_TOAN".equals(s);
    }

    private String formatHotMovieReply(List<PhimDTO> showing, DashboardDTO dashboard, boolean adminUser) {
        if (adminUser && dashboard != null && dashboard.getTopMovies() != null && !dashboard.getTopMovies().isEmpty()) {
            String hotMovies = dashboard
                .getTopMovies()
                .stream()
                .limit(5)
                .map(item -> item.getName() + " (" + formatMoney(item.getRevenue()) + " đ doanh thu)")
                .collect(Collectors.joining("\n• "));
            return "🔥 Top phim bán chạy:\n• " + hotMovies + ".";
        }

        if (showing.isEmpty()) {
            return "Hiện chưa có phim đang chiếu.";
        }

        String hotMovies = showing.stream().limit(5).map(PhimDTO::getTenPhim).filter(Objects::nonNull).collect(Collectors.joining(", "));
        return "🔥 Một số phim đang được xem nhiều tại CinemaTick: " + hotMovies + ". Hỏi tên phim cụ thể để xem lịch chiếu!";
    }

    private String formatAdminWorkflowReply(String normalized) {
        if (containsAny(normalized, "them suat chieu", "tao suat chieu", "quan ly lich chieu", "trung lich", "chong lich")) {
            return (
                "Để thêm suất chiếu: Admin → Quản lý suất chiếu → Tạo mới → chọn Phim, Phòng chiếu, Giờ bắt đầu & Kết thúc.\n" +
                "Lưu ý: Hệ thống tự động chặn trùng lịch trong cùng phòng. " +
                "Sau khi tạo, hệ thống tự sinh phôi vé cho toàn bộ ghế của phòng đó."
            );
        }
        if (containsAny(normalized, "quan ly phim", "sua phim", "poster phim", "cap nhat phim")) {
            return (
                "Quản lý phim: Admin → Phim → có thể cập nhật tên, mô tả, thời lượng, poster, trailer, thể loại.\n" +
                "Lưu ý: Không nên xóa phim đã có suất chiếu, hãy chuyển trạng thái INACTIVE nếu muốn ẩn."
            );
        }
        if (containsAny(normalized, "huy ve", "booking", "don dat ve", "quan ly dat ve", "hoa don")) {
            return (
                "Quản lý đặt vé: Vào mục Hóa đơn để theo dõi trạng thái thanh toán và danh sách vé.\n" +
                "Khi xử lý vé, kiểm tra trạng thái hóa đơn trước để tránh lệch dữ liệu ghế."
            );
        }
        if (containsAny(normalized, "nguoi dung", "tai khoan", "phan quyen", "khoa tai khoan", "quan ly nguoi dung")) {
            return (
                "Quản lý người dùng: Admin → Người dùng → xem tài khoản, vai trò, trạng thái.\n" +
                "Chatbot chỉ hỗ trợ hướng dẫn, không thay đổi trực tiếp dữ liệu người dùng."
            );
        }
        if (containsAny(normalized, "combo", "dich vu", "quan ly combo", "them combo")) {
            return "Quản lý combo F&B: Admin → Dịch vụ F&B → thêm/sửa combo, đặt giá và trạng thái bán.";
        }
        return "Mình hỗ trợ hướng dẫn quản trị: phim, suất chiếu, đặt vé, người dùng, combo và thống kê. Bạn muốn hỏi phần nào?";
    }

    private String denyAdminStats() {
        return (
            "Thông tin doanh thu và thống kê chỉ dành cho admin. " +
            "Mình có thể hỗ trợ bạn về phim đang chiếu, lịch chiếu, giá vé và combo F&B nhé!"
        );
    }

    // =========================================================================
    // Intent detection — phát hiện ý định người dùng
    // =========================================================================

    private boolean isGreeting(String normalized) {
        return (
            containsPhrase(normalized, "hello") ||
            containsPhrase(normalized, "hi") ||
            containsPhrase(normalized, "xin chao") ||
            containsPhrase(normalized, "chao") ||
            containsPhrase(normalized, "chao ban") ||
            containsPhrase(normalized, "oi") ||
            containsPhrase(normalized, "hey")
        );
    }

    private boolean isBookingGuideQuery(String normalized) {
        return containsAny(
            normalized,
            "dat ve",
            "mua ve",
            "thanh toan",
            "huong dan dat ve",
            "toi can dat ve",
            "chon ghe",
            "qr ve",
            "ve cua toi",
            "lich su dat ve",
            "cach dat ve",
            "lam sao de dat",
            "mua ve o dau",
            "dat ve nhu the nao",
            "quy trinh dat ve"
        );
    }

    private boolean isComboQuery(String normalized) {
        return containsAny(
            normalized,
            "combo",
            "bap",
            "nuoc",
            "f b",
            "fb",
            "do an",
            "do uong",
            "bap rang bo",
            "nuoc ngot",
            "snack",
            "an gi",
            "uong gi",
            "co combo gi",
            "combo gia bao nhieu"
        );
    }

    private boolean isSeatQuery(String normalized) {
        return containsAny(
            normalized,
            "ghe vip",
            "ghe thuong",
            "ghe doi",
            "sweetbox",
            "loai ghe",
            "ghe o hang nao",
            "chon ghe",
            "ghe nao con trong",
            "ghe nao con"
        );
    }

    private boolean isStatsQuery(String normalized) {
        boolean askRevenue = containsAny(
            normalized,
            "thong ke",
            "bao cao",
            "doanh thu",
            "tong doanh thu",
            "doanh so",
            "tong ket",
            "ket qua kinh doanh"
        );
        boolean askTickets =
            (containsAny(normalized, "ve") && containsAny(normalized, "ban", "ban duoc", "ban chay", "da ban")) ||
            containsAny(normalized, "so ve", "tong so ve", "luot ve");
        boolean askOrders = containsAny(normalized, "nguoi dat", "don hang", "so don", "don thanh cong");
        boolean askOccupancy = containsAny(normalized, "lap day", "ty le lap day", "occupancy", "lap day ghe");
        return askRevenue || askTickets || askOrders || askOccupancy;
    }

    private boolean isWeekStatsQuery(String normalized) {
        return containsAny(
            normalized,
            "doanh thu tuan",
            "thong ke tuan",
            "tuan nay",
            "7 ngay",
            "tuan qua",
            "trong tuan",
            "bao cao tuan",
            "ve ban tuan nay"
        );
    }

    private boolean isMonthStatsQuery(String normalized) {
        return containsAny(
            normalized,
            "doanh thu thang",
            "thong ke thang",
            "thang nay",
            "30 ngay",
            "trong thang",
            "bao cao thang",
            "ve ban thang nay",
            "thang qua"
        );
    }

    private boolean isRoomStatsQuery(String normalized) {
        boolean hasRoomKeyword = containsAny(normalized, "phong", "phong chieu");
        boolean hasStatsKeyword = containsAny(
            normalized,
            "hieu qua",
            "nhieu ve",
            "ban chay",
            "hieu suat",
            "lap day",
            "doanh thu",
            "tot nhat",
            "thong ke",
            "xep hang"
        );
        return hasRoomKeyword && hasStatsKeyword;
    }

    private boolean isComboStatsQuery(String normalized) {
        boolean hasComboKeyword = containsAny(normalized, "combo", "bap nuoc", "do an", "do uong", "f b", "fb");
        boolean hasStatsKeyword = containsAny(
            normalized,
            "ban chay",
            "nhieu nhat",
            "thong ke",
            "doanh thu",
            "xep hang",
            "chay nhat",
            "mua nhieu"
        );
        return hasComboKeyword && hasStatsKeyword;
    }

    private boolean isPeakHourQuery(String normalized) {
        return (
            containsAny(normalized, "gio cao diem", "khung gio", "gio vang", "peak hour") ||
            (containsAny(normalized, "khung gio", "gio nao", "ca nao", "buoi nao") &&
                containsAny(normalized, "nhieu ve", "ban chay", "dong khach", "thong ke", "doanh thu"))
        );
    }

    private boolean isTicketTypeQuery(String normalized) {
        return containsAny(
            normalized,
            "phan loai ve",
            "loai ve nao nhieu nhat",
            "ve thuong hay vip nhieu hon",
            "phan bo loai ghe",
            "thong ke loai ve",
            "ghe vip ban bao nhieu",
            "ghe thuong ban bao nhieu",
            "ghe doi ban bao nhieu",
            "ty le ghe vip",
            "ve vip bao nhieu"
        );
    }

    private boolean isCompareQuery(String normalized) {
        return containsAny(
            normalized,
            "so sanh",
            "ky truoc",
            "hom qua",
            "tuan truoc",
            "thang truoc",
            "tang hay giam",
            "bien dong",
            "tang truong",
            "so voi",
            "change",
            "growth",
            "vs hom qua",
            "so voi hom qua",
            "bien dong doanh thu",
            "doanh thu tang hay giam"
        );
    }

    private boolean isReviewStatsQuery(String normalized) {
        boolean hasReviewKeyword = containsAny(normalized, "danh gia", "review", "sao", "rating", "diem");
        boolean hasMovieKeyword = containsAny(normalized, "phim", "he thong");
        return hasReviewKeyword && hasMovieKeyword;
    }

    private boolean isStatusBreakdownQuery(String normalized) {
        return (
            containsAny(normalized, "trang thai don hang", "don hang status", "phan tich don hang") ||
            (containsAny(normalized, "don hang", "don") &&
                containsAny(normalized, "huy", "bi huy", "cho thanh toan", "da thanh toan", "qua han", "thong ke"))
        );
    }

    private boolean isWeekQuery(String normalized) {
        return containsAny(normalized, "tuan nay", "tuan qua", "7 ngay", "doanh thu tuan", "thong ke tuan");
    }

    private boolean isMonthQuery(String normalized) {
        return containsAny(normalized, "thang nay", "thang qua", "30 ngay", "doanh thu thang", "thong ke thang");
    }

    private boolean isHotMovieQuery(String normalized) {
        return containsAny(
            normalized,
            "phim hot",
            "phim hay",
            "phim nao hot",
            "co phim gi hay",
            "dang co phim gi hay",
            "phim nao dang hay",
            "phim nao hot nhat",
            "phim co doanh thu cao nhat",
            "phim doanh thu cao nhat",
            "phim ban chay",
            "phim ban chay nhat",
            "phim nhieu ve nhat",
            "phim duoc xem nhieu nhat",
            "phim duoc quan tam",
            "phim dang duoc xem nhieu",
            "recommend phim",
            "de xuat phim"
        );
    }

    private boolean isAdminWorkflowQuery(String normalized) {
        return containsAny(
            normalized,
            "quan ly nguoi dung",
            "quan ly phim",
            "quan ly lich chieu",
            "quan ly suat chieu",
            "them suat chieu",
            "tao suat chieu",
            "trung lich",
            "chong lich",
            "quan ly dat ve",
            "huy ve",
            "phan quyen",
            "khoa tai khoan",
            "cap nhat phim",
            "sua phim",
            "poster phim",
            "quan ly combo",
            "them combo",
            "hoa don",
            "don hang"
        );
    }

    private boolean isShowingMovieQuery(String normalized) {
        return containsAny(
            normalized,
            "phim dang chieu",
            "dang chieu",
            "co phim nao dang chieu",
            "phim gi dang chieu",
            "hien dang chieu",
            "dang phat hanh",
            "phim nao dang co mat",
            "co phim gi hom nay",
            "phim hom nay",
            "phim dang co"
        );
    }

    private boolean isComingSoonQuery(String normalized) {
        return containsAny(
            normalized,
            "sap chieu",
            "coming soon",
            "phim sap chieu",
            "phim sap ra mat",
            "phim moi sap ra",
            "phim sap toi",
            "sap xuat hien",
            "phim moi"
        );
    }

    private boolean isNearestShowtimeQuery(String normalized) {
        return containsAny(
            normalized,
            "gan nhat",
            "suat chieu gan nhat",
            "lich chieu gan nhat",
            "suat sap nhat",
            "suat tiep theo",
            "suat chieu tiep theo",
            "suất gần nhất",
            "xem ngay bay gio",
            "co the xem ngay",
            "co the xem luc nao",
            "suat chieu som nhat",
            "som nhat"
        );
    }

    private boolean isShowtimeQuery(String normalized) {
        return containsAny(
            normalized,
            "suat chieu",
            "lich chieu",
            "gio chieu",
            "hom nao co suat",
            "may gio",
            "may gio chieu",
            "chieu may gio",
            "co suat nao",
            "co suat chieu nao",
            "dang co suat nao",
            "suat nao con",
            "suat con trong",
            "lich phim hom nay",
            "phim chieu hom nay",
            "co gi hom nay",
            "cho phim nao",
            "phim nao co suat",
            "lich phat chieu",
            "bao gio chieu",
            "khi nao chieu"
        );
    }

    private boolean isTodayQuery(String normalized) {
        return containsAny(normalized, "hom nay", "today", "trong ngay", "ngay hom nay", "hôm nay", "nay");
    }

    private boolean isOutOfScope(String normalized) {
        return containsAny(
            normalized,
            "thoi tiet",
            "weather",
            "bong da",
            "football",
            "lap trinh",
            "code java",
            "hoc may",
            "chinh tri",
            "y te",
            "covid",
            "benh vien",
            "nau an",
            "cong thuc",
            "chung khoan",
            "bitcoin",
            "crypto"
        );
    }

    // =========================================================================
    // Utility helpers
    // =========================================================================

    private boolean containsAny(String text, String... patterns) {
        for (String pattern : patterns) {
            if (text.contains(pattern)) return true;
        }
        return false;
    }

    private boolean containsPhrase(String text, String phrase) {
        if (text == null || text.isBlank() || phrase == null || phrase.isBlank()) return false;
        String paddedText = " " + text.trim() + " ";
        String paddedPhrase = " " + phrase.trim() + " ";
        return paddedText.contains(paddedPhrase);
    }

    /**
     * Chuẩn hoá chuỗi: bỏ dấu, lowercase, chỉ giữ a-z0-9 và dấu cách.
     */
    private String normalize(String value) {
        if (value == null) return "";
        String s = removeAccent(value);
        s = s.toLowerCase(Locale.ROOT);
        s = s.replaceAll("[^a-z0-9]+", " ");
        return s.replaceAll("\\s+", " ").trim();
    }

    private String removeAccent(String s) {
        if (s == null) return "";
        s = s.replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a");
        s = s.replaceAll("[ÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬ]", "a");
        s = s.replaceAll("[éèẻẽẹêếềểễệ]", "e");
        s = s.replaceAll("[ÉÈẺẼẸÊẾỀỂỄỆ]", "e");
        s = s.replaceAll("[íìỉĩị]", "i");
        s = s.replaceAll("[ÍÌỈĨỊ]", "i");
        s = s.replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o");
        s = s.replaceAll("[ÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢ]", "o");
        s = s.replaceAll("[úùủũụưứừửữự]", "u");
        s = s.replaceAll("[ÚÙỦŨỤƯỨỪỬỮỰ]", "u");
        s = s.replaceAll("[ýỳỷỹỵ]", "y");
        s = s.replaceAll("[ÝỲỶỸỴ]", "y");
        s = s.replaceAll("[đ]", "d");
        s = s.replaceAll("[Đ]", "d");
        return s;
    }

    private boolean containsWord(String text, String word) {
        if (text == null || text.isBlank() || word == null || word.isBlank()) return false;
        return text.equals(word) || text.startsWith(word + " ") || text.endsWith(" " + word) || text.contains(" " + word + " ");
    }

    private boolean isCurrentUserAdmin() {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) return true;
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(nguoiDungRepository::findOneByEmailIgnoreCase)
            .map(nguoiDung -> normalize(nguoiDung.getVaiTro()))
            .map(role -> role.contains("admin"))
            .orElse(false);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0";
        return String.format(Locale.ROOT, "%,.0f", value.doubleValue());
    }

    private String formatPercent(BigDecimal value) {
        if (value == null) return "0%";
        return value.stripTrailingZeros().toPlainString() + "%";
    }

    // =========================================================================
    // User features helper methods
    // =========================================================================

    private boolean isMyBookingQuery(String normalized) {
        return containsAny(
            normalized,
            "lich su dat ve",
            "ve da mua",
            "ve cua toi",
            "lich su mua ve",
            "don hang cua toi",
            "lich su dat",
            "ve da dat"
        );
    }

    private boolean isPointsQuery(String normalized) {
        return containsAny(normalized, "diem tich luy", "diem cua toi", "diem thanh vien", "tich luy cua toi", "diem thuong", "tich diem");
    }

    private boolean isGenreQuery(String normalized) {
        return containsAny(
            normalized,
            "the loai",
            "phim hanh dong",
            "phim kinh di",
            "phim hai",
            "phim hoat hinh",
            "phim tinh cam",
            "phim vien tuong",
            "phim tam ly"
        );
    }

    private boolean isMovieDetailQuery(String normalized) {
        return containsAny(normalized, "thoi luong", "dao dien", "dien vien", "noi dung phim", "tom tat", "noi dung cua phim");
    }

    private boolean isPriceQuery(String normalized) {
        return containsAny(normalized, "gia ve", "gia ve vip", "ghe vip bao nhieu", "ve doi bao nhieu", "gia ve thuong");
    }

    private boolean isPolicyQuery(String normalized) {
        return containsAny(normalized, "huy ve", "doi ve", "mang do an", "mang nuoc", "do tuoi", "gio mo cua", "chinh sach");
    }

    private String buildMyBookingReply() {
        var userLoginOpt = SecurityUtils.getCurrentUserLogin();
        if (userLoginOpt.isEmpty()) {
            return "Bạn vui lòng đăng nhập để xem lịch sử đặt vé cá nhân nhé! 🎟️";
        }
        String email = userLoginOpt.get();
        List<HoaDon> invoices = hoaDonRepository.findByNguoiDungEmailOrderByIdDesc(email);
        if (invoices == null || invoices.isEmpty()) {
            return "Tài khoản của bạn chưa có lịch sử đặt vé nào trên hệ thống. Hãy thử đặt vé ngay nhé!";
        }

        StringBuilder sb = new StringBuilder("🎟️ **Lịch sử đặt vé của bạn (3 giao dịch gần nhất):**\n");
        invoices
            .stream()
            .limit(3)
            .forEach(invoice -> {
                sb.append("━━━━━━━━━━━━━━━━━━━━━━\n");
                sb
                    .append("• **Đơn hàng ngày:** ")
                    .append(invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(SHOWTIME_FORMATTER) : "Chưa rõ")
                    .append("\n");
                sb.append("• **Tổng tiền:** ").append(formatMoney(invoice.getTongTien())).append(" đ\n");

                String status = "Chờ thanh toán";
                if (isPaidInvoice(invoice)) {
                    status = "Đã thanh toán ✅";
                } else if (
                    "CANCELLED".equals(invoice.getTrangThai()) || "3".equals(invoice.getTrangThai()) || "HUY".equals(invoice.getTrangThai())
                ) {
                    status = "Đã hủy ❌";
                }
                sb.append("• **Trạng thái:** ").append(status).append("\n");

                if (invoice.getVes() != null && !invoice.getVes().isEmpty()) {
                    Ve firstVe = invoice.getVes().iterator().next();
                    if (firstVe.getSuatChieu() != null && firstVe.getSuatChieu().getPhim() != null) {
                        sb.append("• **Phim:** ").append(firstVe.getSuatChieu().getPhim().getTenPhim()).append("\n");
                        if (firstVe.getSuatChieu().getThoiGianBatDau() != null) {
                            sb
                                .append("• **Suất chiếu:** ")
                                .append(firstVe.getSuatChieu().getThoiGianBatDau().format(SHOWTIME_FORMATTER))
                                .append("\n");
                        }
                    }

                    String seats = invoice
                        .getVes()
                        .stream()
                        .filter(v -> v.getGhe() != null && v.getGhe().getMaGhe() != null)
                        .map(v -> v.getGhe().getMaGhe())
                        .collect(Collectors.joining(", "));
                    if (!seats.isBlank()) {
                        sb.append("• **Ghế:** ").append(seats).append("\n");
                    }
                }

                if (invoice.getChiTietFBS() != null && !invoice.getChiTietFBS().isEmpty()) {
                    String combos = invoice
                        .getChiTietFBS()
                        .stream()
                        .filter(c -> c.getDichVuFB() != null && c.getDichVuFB().getTenCombo() != null)
                        .map(c -> c.getDichVuFB().getTenCombo() + " (x" + c.getSoLuong() + ")")
                        .collect(Collectors.joining(", "));
                    if (!combos.isBlank()) {
                        sb.append("• **Combo F&B:** ").append(combos).append("\n");
                    }
                }
            });
        return sb.toString().trim();
    }

    private String buildPointsReply() {
        var userLoginOpt = SecurityUtils.getCurrentUserLogin();
        if (userLoginOpt.isEmpty()) {
            return "Bạn vui lòng đăng nhập để kiểm tra điểm tích lũy thành viên nhé! 👥";
        }
        String email = userLoginOpt.get();
        return nguoiDungRepository
            .findOneByEmailIgnoreCase(email)
            .map(nguoiDung -> {
                Integer points = nguoiDung.getDiemTichLuy() != null ? nguoiDung.getDiemTichLuy() : 0;
                return (
                    "👥 **Thông tin thành viên:**\n" +
                    "• Khách hàng: " +
                    (nguoiDung.getHoTen() != null ? nguoiDung.getHoTen() : email) +
                    "\n" +
                    "• Điểm tích lũy hiện tại: **" +
                    points +
                    " điểm**\n\n" +
                    "💡 **Quy định tích điểm:** Mỗi đơn đặt vé hoặc combo thành công, bạn sẽ nhận được điểm tích lũy tương ứng (10% giá trị đơn hàng được quy thành điểm). Bạn có thể dùng điểm tích lũy này để nâng cấp ghế, đổi bắp nước hoặc giảm giá vé trực tiếp tại quầy thanh toán của CinemaTick!"
                );
            })
            .orElse("Không tìm thấy thông tin tài khoản của bạn trên hệ thống.");
    }

    private String buildGenreReply(String normalized, List<PhimDTO> showing, List<PhimDTO> comingSoon) {
        String genre = "";
        if (normalized.contains("hanh dong")) genre = "hành động";
        else if (normalized.contains("kinh di")) genre = "kinh dị";
        else if (normalized.contains("hai")) genre = "hài";
        else if (normalized.contains("hoat hinh")) genre = "hoạt hình";
        else if (normalized.contains("tinh cam") || normalized.contains("lang man")) genre = "tình cảm";
        else if (normalized.contains("vien tuong")) genre = "viễn tưởng";
        else if (normalized.contains("tam ly") || normalized.contains("tinh cam tam ly")) genre = "tâm lý";

        if (genre.isBlank()) {
            return "Hệ thống hỗ trợ các thể loại phim: Hành động, Kinh dị, Hài, Hoạt hình, Tình cảm, Viễn tưởng, Tâm lý. Bạn muốn tìm thể loại nào?";
        }

        final String finalGenre = genre;
        List<PhimDTO> matchedShowing = showing
            .stream()
            .filter(p -> p.getTheLoai() != null && normalize(p.getTheLoai()).contains(normalize(finalGenre)))
            .toList();

        List<PhimDTO> matchedComing = comingSoon
            .stream()
            .filter(p -> p.getTheLoai() != null && normalize(p.getTheLoai()).contains(normalize(finalGenre)))
            .toList();

        if (matchedShowing.isEmpty() && matchedComing.isEmpty()) {
            return "Hiện tại CinemaTick chưa có phim nào thuộc thể loại **" + genre + "** đang hoặc sắp chiếu.";
        }

        StringBuilder sb = new StringBuilder("🎬 **Danh sách phim thể loại " + genre + ":**\n");
        if (!matchedShowing.isEmpty()) {
            sb.append("\n🎥 **Đang chiếu:**\n");
            matchedShowing.forEach(p -> sb.append("• ").append(p.getTenPhim()).append("\n"));
        }
        if (!matchedComing.isEmpty()) {
            sb.append("\n🔜 **Sắp chiếu:**\n");
            matchedComing.forEach(p ->
                sb
                    .append("• ")
                    .append(p.getTenPhim())
                    .append(p.getNgayKhoiChieu() != null ? " (Khởi chiếu: " + p.getNgayKhoiChieu().format(DATE_ONLY_FORMATTER) + ")" : "")
                    .append("\n")
            );
        }
        sb.append("\n💡 Hỏi tên phim cụ thể để xem lịch chiếu chi tiết nhé!");
        return sb.toString().trim();
    }

    private String buildPriceReply() {
        return (
            "🎫 **Bảng giá vé tham khảo tại CinemaTick:**\n" +
            "━━━━━━━━━━━━━━━━━━━━━━\n" +
            "• **Ghế thường:** 60.000đ - 90.000đ / vé\n" +
            "• **Ghế VIP:** 80.000đ - 110.000đ / vé\n" +
            "• **Ghế đôi (Sweetbox):** 140.000đ - 180.000đ / cặp\n\n" +
            "💡 *Lưu ý:* Giá vé thực tế có thể thay đổi tùy thuộc vào khung giờ chiếu (suất ngày thường, cuối tuần, ngày lễ) hoặc định dạng phòng chiếu. Khi bạn tiến hành chọn suất chiếu cụ thể trong quá trình đặt vé, hệ thống sẽ hiển thị giá vé chính xác cho từng loại ghế."
        );
    }

    private String buildPolicyReply(String normalized) {
        if (containsAny(normalized, "huy ve", "doi ve")) {
            return "❌ **Chính sách hoàn/hủy vé:**\nTheo quy định của CinemaTick, vé đã thanh toán thành công trực tuyến **không thể hoàn trả hoặc thay đổi** suất chiếu/ghế ngồi. Quý khách vui lòng kiểm tra kỹ thông tin phim, rạp, suất chiếu và ghế ngồi trước khi hoàn tất thanh toán.";
        }
        if (containsAny(normalized, "mang do an", "mang nuoc", "do an ngoai")) {
            return "🍿 **Quy định ăn uống tại rạp:**\nĐể đảm bảo vệ sinh phòng chiếu và trải nghiệm tốt nhất cho tất cả khán giả, CinemaTick **không cho phép mang đồ ăn thức uống từ bên ngoài vào rạp**. Quý khách có thể mua các combo bắp rang bơ, nước ngọt cao cấp trực tiếp tại quầy hoặc chọn mua kèm khi đặt vé online.";
        }
        if (containsAny(normalized, "do tuoi", "bao nhieu tuoi")) {
            return (
                "🔞 **Quy định phân loại độ tuổi xem phim:**\n" +
                "• **P:** Phim được phép phổ biến đến mọi độ tuổi.\n" +
                "• **K:** Phim được phổ biến đến người xem dưới 13 tuổi với điều kiện xem cùng cha, mẹ hoặc người giám hộ.\n" +
                "• **T13:** Phim dành cho khán giả từ đủ 13 tuổi trở lên.\n" +
                "• **T16:** Phim dành cho khán giả từ đủ 16 tuổi trở lên.\n" +
                "• **T18:** Phim dành cho khán giả từ đủ 18 tuổi trở lên (vui lòng xuất trình CCCD khi soát vé)."
            );
        }
        return (
            "ℹ️ **Chính sách & Quy định CinemaTick:**\n" +
            "• **Thời gian mở cửa:** Rạp mở cửa từ 8:00 đến 23:30 hàng ngày, kể cả ngày lễ.\n" +
            "• **Soát vé:** Quý khách vui lòng xuất trình mã QR vé nhận được từ email trước giờ chiếu 10-15 phút.\n" +
            "• **Hoàn/Hủy vé:** Không hỗ trợ hoàn/hủy hoặc đổi vé đã thanh toán thành công."
        );
    }

    private void saveChatLog(String message, String reply) {
        try {
            LichSuChatAi chatLog = new LichSuChatAi();
            chatLog.setCauHoiKhachHang(message);
            chatLog.setCauTraLoiBot(reply);
            chatLog.setThoiGianChat(ZonedDateTime.now());
            SecurityUtils.getCurrentUserLogin().flatMap(nguoiDungRepository::findOneByEmailIgnoreCase).ifPresent(chatLog::setNguoiDung);
            lichSuChatAiRepository.save(chatLog);
        } catch (Exception e) {
            LOG.error("Lỗi khi lưu lịch sử chat AI", e);
        }
    }
}
