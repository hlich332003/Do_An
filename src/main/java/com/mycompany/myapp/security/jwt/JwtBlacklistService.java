package com.mycompany.myapp.security.jwt;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * JWT Blacklist Service – Chặn các Token đã bị đăng xuất.
 *
 * Khi User đăng xuất:
 *   1. Token JWT được ném vào Redis với TTL = thời gian sống còn lại của Token.
 *   2. Mỗi request đến, JwtFilter sẽ check Redis trước → nếu Token có trong blacklist → từ chối ngay.
 *
 * Lợi ích:
 *   - Token bị đánh cắp sau khi đăng xuất sẽ vô dụng.
 *   - Redis tự xóa key sau khi Token hết hạn → không tốn bộ nhớ.
 */
@Service
public class JwtBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(JwtBlacklistService.class);
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public JwtBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Thêm token vào blacklist khi người dùng đăng xuất.
     * TTL = thời gian sống còn lại của token (tự động xóa sau khi hết hạn).
     *
     * @param jwt Token JWT cần blacklist
     */
    public void blacklistToken(Jwt jwt) {
        String tokenId = jwt.getId() != null ? jwt.getId() : jwt.getTokenValue();
        String key = BLACKLIST_PREFIX + tokenId;

        // Tính TTL còn lại
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            log.warn("⚠️ [JWT BLACKLIST] Token không có thời gian hết hạn – bỏ qua");
            return;
        }

        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            log.info("ℹ️ [JWT BLACKLIST] Token đã hết hạn – không cần blacklist");
            return;
        }

        redisTemplate.opsForValue().set(key, "BLACKLISTED", ttl);
        log.info("🚫 [JWT BLACKLIST] Token đã bị blacklist – TTL còn lại: {}s", ttl.getSeconds());
    }

    /**
     * Kiểm tra token có trong blacklist không.
     * Gọi hàm này trong JwtFilter trước khi xử lý request.
     *
     * @param jwt Token cần kiểm tra
     * @return true nếu token đã bị blacklist (đã đăng xuất)
     */
    public boolean isBlacklisted(Jwt jwt) {
        String tokenId = jwt.getId() != null ? jwt.getId() : jwt.getTokenValue();
        String key = BLACKLIST_PREFIX + tokenId;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Overload cho token dạng String (lấy từ request header).
     */
    public boolean isTokenStringBlacklisted(String tokenValue) {
        String key = BLACKLIST_PREFIX + tokenValue;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
