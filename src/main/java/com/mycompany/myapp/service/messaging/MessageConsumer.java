package com.mycompany.myapp.service.messaging;

import com.mycompany.myapp.config.RabbitMQConfig;
import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.domain.Ve;
import com.mycompany.myapp.repository.HoaDonRepository;
import com.mycompany.myapp.repository.VeRepository;
import com.mycompany.myapp.service.DistributedLockService;
import com.mycompany.myapp.service.MailService;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer – Lắng nghe và xử lý các messages từ RabbitMQ Queue.
 *
 * Worker 1: emailQueue  → Render QR Code + Gửi Email vé điện tử cho khách
 * Worker 2: statsQueue  → Cập nhật thống kê doanh thu Dashboard Admin
 * Worker 3: timeoutQueue→ Kiểm tra và tự động hủy hóa đơn hết hạn thanh toán
 */
@Service
public class MessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    private final HoaDonRepository hoaDonRepository;
    private final VeRepository veRepository;
    private final DistributedLockService lockService;
    private final MailService mailService;

    public MessageConsumer(
        HoaDonRepository hoaDonRepository,
        VeRepository veRepository,
        DistributedLockService lockService,
        MailService mailService
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.veRepository = veRepository;
        this.lockService = lockService;
        this.mailService = mailService;
    }

    // ======================== WORKER 1: EMAIL + QR CODE ========================

    /**
     * Nhận message sau khi khách thanh toán thành công.
     * Giả lập: render QR Code từ ID hóa đơn và log ra console.
     * (Tích hợp Gmail SMTP thực tế sẽ thêm ở bước sau.)
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    @Transactional(readOnly = true)
    public void handleEmailTicket(Map<String, Object> message) {
        Long hoaDonId = Long.valueOf(message.get("hoaDonId").toString());
        log.info("📧 [EMAIL WORKER] Nhận yêu cầu gửi vé cho Hóa đơn #{}", hoaDonId);

        try {
            // Bước 1: Lấy thông tin hóa đơn
            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findOneWithEagerRelationships(hoaDonId);
            if (hoaDonOpt.isEmpty()) {
                log.error("❌ [EMAIL WORKER] Không tìm thấy hóa đơn #{}", hoaDonId);
                return;
            }
            HoaDon hoaDon = hoaDonOpt.get();
            List<Ve> veList = List.copyOf(veRepository.findByHoaDonId(hoaDonId));

            // Bước 2: Giả lập render QR Code (sẽ tích hợp ZXing thực tế sau)
            String qrData = "CINEMATICK|HD:" + hoaDonId + "|TOTAL:" + hoaDon.getTongTien();
            log.info("🎫 [EMAIL WORKER] QR Code Data: {}", qrData);

            String customerEmail = hoaDon.getNguoiDung() != null ? hoaDon.getNguoiDung().getEmail() : null;
            String customerName = hoaDon.getNguoiDung() != null &&
                hoaDon.getNguoiDung().getHoTen() != null &&
                !hoaDon.getNguoiDung().getHoTen().isBlank()
                ? hoaDon.getNguoiDung().getHoTen()
                : customerEmail;
            String invoiceCode = "#" + hoaDonId;
            String totalAmount = hoaDon.getTongTien() != null
                ? hoaDon.getTongTien().setScale(2, RoundingMode.HALF_UP).toPlainString()
                : "0";
            String paymentMethod = hoaDon.getPhuongThucThanhToan() != null ? hoaDon.getPhuongThucThanhToan() : "VNPAY";
            String movieTitle = veList
                .stream()
                .map(Ve::getSuatChieu)
                .filter(sc -> sc != null && sc.getPhim() != null && sc.getPhim().getTenPhim() != null)
                .map(sc -> sc.getPhim().getTenPhim())
                .findFirst()
                .orElse("Phim");
            String roomName = veList
                .stream()
                .map(Ve::getSuatChieu)
                .filter(sc -> sc != null && sc.getPhongChieu() != null && sc.getPhongChieu().getTenPhong() != null)
                .map(sc -> sc.getPhongChieu().getTenPhong())
                .findFirst()
                .orElse("Phòng");
            String showtimeText = veList
                .stream()
                .map(Ve::getSuatChieu)
                .filter(sc -> sc != null && sc.getThoiGianBatDau() != null)
                .map(sc -> sc.getThoiGianBatDau().toString())
                .findFirst()
                .orElse("Chưa cập nhật");
            List<String> ticketLines = veList
                .stream()
                .map(ve -> {
                    String seat = ve.getGhe() != null && ve.getGhe().getMaGhe() != null ? ve.getGhe().getMaGhe() : "N/A";
                    String ticketCode = ve.getMaVe() != null ? ve.getMaVe() : "N/A";
                    String seatPrice = ve.getGiaVe() != null
                        ? NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(ve.getGiaVe())
                        : "0";
                    return ticketCode + " | " + seat + " | " + seatPrice + " VND";
                })
                .collect(Collectors.toList());

            // Bước 3: Gửi mail thông báo đặt vé
            if (customerEmail != null) {
                mailService.sendBookingNotificationEmail(
                    customerEmail,
                    customerName,
                    invoiceCode,
                    totalAmount,
                    paymentMethod,
                    movieTitle,
                    roomName,
                    showtimeText,
                    ticketLines
                );
            }
            log.info(
                "✅ [EMAIL WORKER] ĐÃ XỬ LÝ THÔNG BÁO ĐẶT VÉ → Email khách: {} | Hóa đơn: #{} | Tổng tiền: {}đ",
                customerEmail != null ? customerEmail : "không có dữ liệu email",
                hoaDonId,
                hoaDon.getTongTien()
            );
        } catch (Exception e) {
            log.error("❌ [EMAIL WORKER] Lỗi khi xử lý hóa đơn #{}", hoaDonId, e);
        }
    }

    // ======================== WORKER 2: STATS / DASHBOARD ========================

    /**
     * Nhận sự kiện "vé vừa bán" để cập nhật số liệu thống kê Dashboard.
     */
    @RabbitListener(queues = RabbitMQConfig.STATS_QUEUE)
    public void handleStatUpdate(Map<String, Object> message) {
        Long hoaDonId = Long.valueOf(message.get("hoaDonId").toString());
        log.info("📊 [STATS WORKER] Nhận sự kiện cập nhật doanh thu – Hóa đơn #{}", hoaDonId);

        try {
            // Trong thực tế: cộng dồn tongTien vào một counter trong Redis hoặc cập nhật bảng thống kê
            Object tongTien = message.get("tongTien");
            log.info("📊 [STATS WORKER] Cập nhật dashboard thành công – +{}đ doanh thu", tongTien);
        } catch (Exception e) {
            log.error("❌ [STATS WORKER] Lỗi cập nhật thống kê cho hóa đơn #{}", hoaDonId, e);
        }
    }

    // ======================== WORKER 3: AUTO-CANCEL TIMEOUT ========================

    /**
     * Nhận message từ Dead Letter Queue sau khi Delay Queue hết TTL (5 phút).
     * Kiểm tra hóa đơn:
     *   - Vẫn "CHO_THANH_TOAN" → Hủy hóa đơn + Giải phóng khóa ghế Redis
     *   - Đã "DA_THANH_TOAN"  → Bỏ qua, không làm gì
     */
    @RabbitListener(queues = RabbitMQConfig.TIMEOUT_QUEUE)
    @Transactional
    public void handlePaymentTimeout(Map<String, Object> message) {
        Long hoaDonId = Long.valueOf(message.get("hoaDonId").toString());
        log.info("⏰ [TIMEOUT WORKER] Kiểm tra hóa đơn #{} – đã hết 5 phút chờ thanh toán", hoaDonId);

        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(hoaDonId);
            if (hoaDonOpt.isEmpty()) {
                log.warn("⚠️ [TIMEOUT WORKER] Không tìm thấy hóa đơn #{} – bỏ qua", hoaDonId);
                return;
            }

            HoaDon hoaDon = hoaDonOpt.get();

            // Chỉ hủy nếu vẫn đang CHỜ thanh toán (1)
            if (hoaDon.getTrangThai() != null && "1".equals(hoaDon.getTrangThai())) {
                // 1. Đánh dấu hóa đơn là HET_HAN (3)
                hoaDon.setTrangThai("3");
                hoaDonRepository.save(hoaDon);

                // 2. Giải phóng các ghế đã bị khóa trong Redis
                // Lấy danh sách ghế từ các Ve của HoaDon
                if (hoaDon.getVes() != null) {
                    hoaDon
                        .getVes()
                        .forEach(ve -> {
                            if (ve.getSuatChieu() != null && ve.getGhe() != null) {
                                Long suatChieuId = ve.getSuatChieu().getId();
                                String maGhe = ve.getGhe().getMaGhe();
                                lockService.unlockSeat(suatChieuId, maGhe);
                                log.info("🔓 [TIMEOUT WORKER] Đã nhả ghế {} - Suất {}", maGhe, suatChieuId);
                            }
                        });
                }

                log.info("🚫 [TIMEOUT WORKER] HÓA ĐƠN #{} → ĐÃ HỦY DO HẾT HẠN THANH TOÁN", hoaDonId);
            } else {
                log.info("✅ [TIMEOUT WORKER] Hóa đơn #{} đã ở trạng thái '{}' – không cần xử lý", hoaDonId, hoaDon.getTrangThai());
            }
        } catch (Exception e) {
            log.error("❌ [TIMEOUT WORKER] Lỗi khi xử lý timeout hóa đơn #{}", hoaDonId, e);
        }
    }
}
