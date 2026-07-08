package com.mycompany.myapp.service.messaging;

import com.mycompany.myapp.config.RabbitMQConfig;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Producer – Đẩy messages vào các Queue RabbitMQ.
 *
 * Các hàm chính:
 *  - sendEmailTicketMessage  → Kích hoạt worker gửi QR Code + Email sau thanh toán thành công
 *  - sendPaymentTimeoutMessage → Hẹn giờ 5 phút → tự hủy hóa đơn nếu chưa thanh toán
 *  - sendStatUpdateMessage   → Bắn sự kiện doanh thu để Dashboard Admin cập nhật
 */
@Service
public class MessageProducer {

    private static final Logger log = LoggerFactory.getLogger(MessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Gửi yêu cầu xử lý Email + QR Code cho hóa đơn vừa được thanh toán.
     * Worker phía sau sẽ nhặt message, render ảnh QR và gửi mail cho khách.
     */
    public void sendEmailTicketMessage(Long hoaDonId) {
        Map<String, Object> message = new HashMap<>();
        message.put("hoaDonId", hoaDonId);
        message.put("timestamp", System.currentTimeMillis());
        message.put("type", "EMAIL_QR_TICKET");

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, message);
            log.info("📧 [PRODUCER] Đã đẩy yêu cầu gửi Email + QR Code cho hóa đơn #{}", hoaDonId);
        } catch (Exception e) {
            log.error("Lỗi kết nối RabbitMQ (Bỏ qua gửi mail): {}", e.getMessage());
        }
    }

    /**
     * Đẩy hóa đơn chờ thanh toán vào Delay Queue (TTL 5 phút).
     * Sau 5 phút nếu chưa thanh toán, message tự động chuyển sang Timeout Queue
     * để worker kiểm tra và hủy hóa đơn.
     */
    public void sendPaymentTimeoutMessage(Long hoaDonId) {
        Map<String, Object> message = new HashMap<>();
        message.put("hoaDonId", hoaDonId);
        message.put("timestamp", System.currentTimeMillis());
        message.put("type", "PAYMENT_TIMEOUT_CHECK");

        try {
            // Đẩy thẳng vào Delay Queue (không qua exchange, gửi thẳng theo tên queue)
            rabbitTemplate.convertAndSend(RabbitMQConfig.DELAY_QUEUE, message);
            log.info("⏳ [PRODUCER] Hóa đơn #{} được đặt vào Delay Queue – sẽ kiểm tra sau 5 phút", hoaDonId);
        } catch (Exception e) {
            log.error("Lỗi kết nối RabbitMQ (Bỏ qua timeout): {}", e.getMessage());
        }
    }

    /**
     * Bắn sự kiện "vé vừa bán thành công" để module thống kê cập nhật Dashboard.
     */
    public void sendStatUpdateMessage(Long hoaDonId, java.math.BigDecimal tongTien) {
        Map<String, Object> message = new HashMap<>();
        message.put("hoaDonId", hoaDonId);
        message.put("tongTien", tongTien);
        message.put("timestamp", System.currentTimeMillis());
        message.put("type", "STAT_UPDATE");

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.STATS_ROUTING_KEY, message);
            log.info("📊 [PRODUCER] Bắn sự kiện cập nhật doanh thu cho hóa đơn #{} – Số tiền: {}", hoaDonId, tongTien);
        } catch (Exception e) {
            log.error("Lỗi kết nối RabbitMQ (Bỏ qua cập nhật thống kê): {}", e.getMessage());
        }
    }
}
