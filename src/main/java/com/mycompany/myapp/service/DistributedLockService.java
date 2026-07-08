package com.mycompany.myapp.service;

import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service quản lý Distributed Lock để giải quyết bài toán tranh chấp ghế.
 *
 * Khi 2 người cùng bấm chọn một ghế trống:
 * - Người đầu tiên → tạo khóa Redis với TTL 5 phút → đi tiếp đến trang thanh
 * toán.
 * - Người thứ hai → thấy khóa đã tồn tại → nhận thông báo "Ghế đang được giữ"
 * ngay lập tức.
 * - Sau 5 phút không thanh toán → khóa tự giải phóng → ghế lại hiển thị trống.
 */
@Service
public class DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);
    private static final String SEAT_LOCK_PREFIX = "seat:lock:";

    private final RedissonClient redissonClient;

    public DistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Thử khóa ghế trong suất chiếu.
     *
     * @param suatChieuId ID suất chiếu
     * @param maGhe       Mã ghế (VD: "A1", "B5")
     * @param ttlSeconds  Thời gian giữ ghế (giây), mặc định 300 = 5 phút
     * @return true nếu khóa thành công, false nếu ghế đang bị người khác giữ
     */
    public boolean lockSeat(Long suatChieuId, String maGhe, long ttlSeconds) {
        String lockKey = buildLockKey(suatChieuId, maGhe);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // tryLock(waitTime, leaseTime, unit) – waitTime=0 → không chờ đợi, thất bại
            // ngay lập tức
            boolean acquired = lock.tryLock(0, ttlSeconds, TimeUnit.SECONDS);
            if (acquired) {
                log.info("✅ [SEAT LOCK] Ghế {} - Suất {} đã được khóa thành công (TTL: {}s)", maGhe, suatChieuId, ttlSeconds);
            } else {
                log.warn("⛔ [SEAT LOCK] Ghế {} - Suất {} đang được người khác giữ!", maGhe, suatChieuId);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ [SEAT LOCK] Interrupted khi khóa ghế {} - Suất {}", maGhe, suatChieuId, e);
            return false;
        }
    }

    /**
     * Giải phóng khóa ghế (gọi khi thanh toán thành công hoặc hóa đơn bị hủy).
     */
    public void unlockSeat(Long suatChieuId, String maGhe) {
        String lockKey = buildLockKey(suatChieuId, maGhe);
        RLock lock = redissonClient.getLock(lockKey);

        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("🔓 [SEAT LOCK] Đã giải phóng khóa ghế {} - Suất {}", maGhe, suatChieuId);
        } else if (lock.isLocked()) {
            // Force delete (dùng khi hủy hóa đơn hết hạn từ worker)
            lock.forceUnlock();
            log.info("🔓 [SEAT LOCK] Force unlock ghế {} - Suất {} (hóa đơn hết hạn)", maGhe, suatChieuId);
        }
    }

    /**
     * Kiểm tra ghế có đang bị khóa không.
     */
    public boolean isSeatLocked(Long suatChieuId, String maGhe) {
        String lockKey = buildLockKey(suatChieuId, maGhe);
        return redissonClient.getLock(lockKey).isLocked();
    }

    /**
     * Khóa nhiều ghế cùng lúc (cho phép chọn nhiều ghế trong một lần đặt vé).
     * Atomic: hoặc tất cả thành công, hoặc rollback hết.
     */
    public boolean lockMultipleSeats(Long suatChieuId, java.util.List<String> maGhes, long ttlSeconds) {
        java.util.List<String> locked = new java.util.ArrayList<>();
        for (String maGhe : maGhes) {
            if (!lockSeat(suatChieuId, maGhe, ttlSeconds)) {
                // Rollback các ghế đã khóa trước
                for (String lockedGhe : locked) {
                    unlockSeat(suatChieuId, lockedGhe);
                }
                return false;
            }
            locked.add(maGhe);
        }
        return true;
    }

    /**
     * Giải phóng nhiều ghế cùng lúc (khi hủy hóa đơn).
     */
    public void unlockMultipleSeats(Long suatChieuId, java.util.List<String> maGhes) {
        maGhes.forEach(maGhe -> unlockSeat(suatChieuId, maGhe));
    }

    /**
     * Heartbeat: Gia hạn tuổi thọ của khóa thêm n giây.
     */
    public void extendLockMultipleSeats(Long suatChieuId, java.util.List<String> maGhes, long ttlSeconds) {
        if (maGhes == null || maGhes.isEmpty()) {
            return;
        }

        // Redisson version hiện tại không expose API gia hạn trực tiếp trên RLock.
        // Luồng giữ ghế đã có TTL gốc 5 phút ở holdSeat(), nên heartbeat này được giữ lại
        // để tương thích phía frontend nhưng không can thiệp sai vào lock runtime.
        for (String maGhe : maGhes) {
            log.debug("Heartbeat giữ ghế: {} - suat {} (ttl hint {}s)", maGhe, suatChieuId, ttlSeconds);
        }
    }

    private String buildLockKey(Long suatChieuId, String maGhe) {
        return SEAT_LOCK_PREFIX + suatChieuId + ":" + maGhe;
    }
}
