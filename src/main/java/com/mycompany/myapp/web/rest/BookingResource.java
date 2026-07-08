package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.service.BookingService;
import com.mycompany.myapp.service.DistributedLockService;
import com.mycompany.myapp.service.dto.HoaDonDTO;
import com.mycompany.myapp.service.messaging.MessageProducer;
import com.mycompany.myapp.web.rest.dto.BookingCreateRequestDTO;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API cho luồng đặt vé.
 * Tích hợp Redis Distributed Lock (giữ ghế) và RabbitMQ (gửi email/QR bất đồng bộ).
 */
@RestController
@RequestMapping("/api/booking")
public class BookingResource {

    private static final Logger LOG = LoggerFactory.getLogger(BookingResource.class);
    private static final long SEAT_HOLD_TTL = 300L; // 5 phút, khớp với timeout thanh toán

    private final BookingService bookingService;
    private final DistributedLockService lockService;
    private final MessageProducer messageProducer;

    public BookingResource(BookingService bookingService, DistributedLockService lockService, MessageProducer messageProducer) {
        this.bookingService = bookingService;
        this.lockService = lockService;
        this.messageProducer = messageProducer;
    }

    /** Tạo hóa đơn đặt vé (luồng cũ). */
    @PostMapping("/create")
    public ResponseEntity<HoaDonDTO> createBooking(@RequestBody BookingCreateRequestDTO request) {
        LOG.debug("REST request tạo hóa đơn đặt vé: {}", request);
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    /**
     * Giữ ghế tạm thời (Redis Lock – 5 phút).
     * Body: { "suatChieuId": 1, "maGhes": ["A1","A2"] }
     */
    @PostMapping("/hold-seats")
    public ResponseEntity<?> holdSeats(@RequestBody Map<String, Object> request) {
        Long suatChieuId = Long.valueOf(request.get("suatChieuId").toString());
        @SuppressWarnings("unchecked")
        List<String> maGhes = (List<String>) request.get("maGhes");

        LOG.info("🎟️ [BOOKING] Giữ ghế – Suất #{} | Ghế: {}", suatChieuId, maGhes);

        // Cố gắng khóa toàn bộ danh sách ghế trong Redis.
        // TTL (Time To Live) là 300s (5 phút). Nếu có bất kỳ ghế nào đã bị người khác khóa trước, hàm sẽ trả về false.
        boolean success = lockService.lockMultipleSeats(suatChieuId, maGhes, SEAT_HOLD_TTL);

        if (success) {
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "Đã giữ ghế thành công trong 5 phút. Vui lòng hoàn tất thanh toán!");
            body.put("ttlSeconds", SEAT_HOLD_TTL);
            return ResponseEntity.ok(body);
        } else {
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "Một hoặc nhiều ghế đang được người khác giữ. Vui lòng chọn ghế khác!");
            return ResponseEntity.status(409).body(body);
        }
    }

    /**
     * Giải phóng ghế (khi người dùng bấm hủy hoặc thoát trang).
     * Body: { "suatChieuId": 1, "maGhes": ["A1","A2"] }
     */
    @PostMapping("/release-seats")
    public ResponseEntity<?> releaseSeats(@RequestBody Map<String, Object> request) {
        Long suatChieuId = Long.valueOf(request.get("suatChieuId").toString());
        @SuppressWarnings("unchecked")
        List<String> maGhes = (List<String>) request.get("maGhes");

        // Gọi sang DistributedLockService để xóa (del) các key đang khóa ghế trong Redis
        // Việc này lập tức biến trạng thái ghế từ "Đang giữ" thành "Trống" trên màn hình đặt vé
        lockService.unlockMultipleSeats(suatChieuId, maGhes);
        LOG.info("🔓 [BOOKING] Giải phóng {} ghế – Suất #{}", maGhes.size(), suatChieuId);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Đã giải phóng ghế thành công");
        return ResponseEntity.ok(body);
    }

    /**
     * Heartbeat API: Gia hạn khóa ghế (được gọi từ Angular mỗi 10 giây).
     * Body: { "suatChieuId": 1, "maGhes": ["A1","A2"] }
     */
    @PostMapping("/extend-lock")
    public ResponseEntity<?> extendLock(@RequestBody Map<String, Object> request) {
        Long suatChieuId = Long.valueOf(request.get("suatChieuId").toString());
        @SuppressWarnings("unchecked")
        List<String> maGhes = (List<String>) request.get("maGhes");

        lockService.extendLockMultipleSeats(suatChieuId, maGhes, SEAT_HOLD_TTL);
        return ResponseEntity.ok().build();
    }

    /**
     * Kiểm tra trạng thái ghế trong Redis (đang bị giữ hay không).
     * Params: suatChieuId, maGhes (danh sách mã ghế)
     */
    @GetMapping("/seat-status")
    public ResponseEntity<?> getSeatStatus(@RequestParam Long suatChieuId, @RequestParam List<String> maGhes) {
        Map<String, Boolean> statusMap = new HashMap<>();
        maGhes.forEach(maGhe -> statusMap.put(maGhe, lockService.isSeatLocked(suatChieuId, maGhe)));
        return ResponseEntity.ok(statusMap);
    }

    /**
     * Xác nhận thanh toán thành công – đẩy job gửi email + QR vào RabbitMQ.
     * Thực tế: endpoint này được callback từ VNPay/MoMo sau khi thanh toán xong.
     */
    @PostMapping("/confirm-payment/{hoaDonId}")
    public ResponseEntity<?> confirmPayment(@PathVariable Long hoaDonId) {
        bookingService.confirmPayment(hoaDonId);

        LOG.info("💳 [BOOKING] Xác nhận thanh toán hóa đơn #{} và đã kích hoạt gửi mail/thống kê", hoaDonId);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Thanh toán thành công! Vé điện tử sẽ được gửi đến email của bạn.");
        return ResponseEntity.ok(body);
    }
}
