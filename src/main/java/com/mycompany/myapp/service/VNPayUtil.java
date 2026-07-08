package com.mycompany.myapp.service;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Tiện ích hỗ trợ mã hóa bảo mật cho VNPay
 */
public class VNPayUtil {

    /**
     * Tạo mã băm HMAC SHA512 cho chuỗi dữ liệu (dùng để xác thực giao dịch với VNPay)
     *
     * @param key  Khóa bí mật (Hash Secret) của Merchant
     * @param data Dữ liệu cần băm (Query String đã được sắp xếp)
     * @return Chuỗi mã băm Hex String
     */
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
