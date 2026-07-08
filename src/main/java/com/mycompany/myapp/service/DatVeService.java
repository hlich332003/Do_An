package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Ve;
import com.mycompany.myapp.repository.VeRepository;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatVeService {

    private static final Logger LOG = LoggerFactory.getLogger(DatVeService.class);
    private static final String SEAT_LOCK_KEY_PREFIX = "ghe:lock:";
    private static final long LOCK_TTL_MINUTES = 5;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    private VeRepository veRepository;

    public DatVeService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean holdSeat(Long suatChieuId, Long gheId) {
        String lockKey = buildLockKey(suatChieuId, gheId);
        LOG.debug("Cố gắng khóa ghế với key: {}", lockKey);
        Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", LOCK_TTL_MINUTES, TimeUnit.MINUTES);
        if (result != null && result) {
            LOG.info("Khóa ghế thành công: {}", lockKey);
            return true;
        } else {
            LOG.warn("Không thể khóa ghế, đã có người giữ: {}", lockKey);
            return false;
        }
    }

    public boolean isSeatHeld(Long suatChieuId, Long gheId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildLockKey(suatChieuId, gheId)));
    }

    public boolean isSeatSoldForShowtime(Long suatChieuId, Long gheId) {
        return veRepository
            .findBySuatChieuIdAndGheId(suatChieuId, gheId)
            .stream()
            .anyMatch(ve -> ve.getTrangThai() != null && ("DA_DAT".equals(ve.getTrangThai()) || "DANG_GIU_CHO".equals(ve.getTrangThai())));
    }

    public void releaseSeat(Long suatChieuId, Long gheId) {
        String lockKey = buildLockKey(suatChieuId, gheId);
        LOG.debug("Giải phóng khóa ghế: {}", lockKey);
        redisTemplate.delete(lockKey);
    }

    public String buildLockKey(Long suatChieuId, Long gheId) {
        return SEAT_LOCK_KEY_PREFIX + suatChieuId + ":" + gheId;
    }

    public void releaseAllSeatsForBooking(Long hoaDonId) {
        LOG.debug("Giải phóng tất cả ghế cho hóa đơn: {}", hoaDonId);
        try {
            List<Ve> ves = veRepository.findByHoaDonId(hoaDonId).stream().toList();

            for (Ve ve : ves) {
                if (ve.getSuatChieu() != null && ve.getGhe() != null) {
                    releaseSeat(ve.getSuatChieu().getId(), ve.getGhe().getId());
                }
            }
            LOG.info("Giải phóng {} ghế cho hóa đơn {}", ves.size(), hoaDonId);
        } catch (Exception e) {
            LOG.error("Lỗi giải phóng ghế", e);
        }
    }
}
