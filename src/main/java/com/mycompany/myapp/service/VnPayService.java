package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.HoaDon;
import com.mycompany.myapp.repository.HoaDonRepository;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class VnPayService {

    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String DEFAULT_SANDBOX_TMN_CODE = "PP8HV24H";
    private static final String DEFAULT_SANDBOX_HASH_SECRET = "BGOV8Y2RLVOODSDDWRJQQ4992FCZTZCM";
    private static final String DEFAULT_SANDBOX_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String DEFAULT_SANDBOX_RETURN_URL = "http://localhost:9000/api/payment/vnpay/return";

    private final HoaDonRepository hoaDonRepository;
    private final BookingService bookingService;

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    public VnPayService(HoaDonRepository hoaDonRepository, BookingService bookingService) {
        this.hoaDonRepository = hoaDonRepository;
        this.bookingService = bookingService;
    }

    public String createPaymentUrl(Long hoaDonId, String clientIp, boolean qrOnly) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElseThrow(() -> new IllegalArgumentException("Hóa đơn không tồn tại"));

        if (hoaDon.getTongTien() == null || hoaDon.getTongTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hóa đơn không hợp lệ để thanh toán");
        }

        TreeMap<String, String> params = new TreeMap<>();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime expireAt = now.plusMinutes(15);

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", resolveTmnCode());
        params.put("vnp_Amount", hoaDon.getTongTien().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CreateDate", now.format(VNPAY_DATE_FORMAT));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", normalizeClientIp(clientIp));
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", sanitizeOrderInfo("Thanh toan ve xem phim hoa don " + hoaDonId));
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", resolveReturnUrl());
        params.put("vnp_TxnRef", buildTxnRef(hoaDon));
        params.put("vnp_ExpireDate", expireAt.format(VNPAY_DATE_FORMAT));
        if (qrOnly) {
            params.put("vnp_BankCode", "VNPAYQR");
        }

        String query = buildEncodedQuery(params);
        String secureHash = VNPayUtil.hmacSHA512(resolveHashSecret(), query);
        return resolvePayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public boolean isValidSignature(Map<String, String> params) {
        String providedHash = params.get("vnp_SecureHash");
        if (providedHash == null || providedHash.isBlank()) {
            return false;
        }

        TreeMap<String, String> data = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || !key.startsWith("vnp_")) {
                continue;
            }
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key) || value == null || value.isBlank()) {
                continue;
            }
            data.put(key, value);
        }

        String signData = buildEncodedQuery(data);
        String expectedHash = VNPayUtil.hmacSHA512(resolveHashSecret(), signData);
        return expectedHash.equalsIgnoreCase(providedHash);
    }

    public Map<String, String> handleIpn(Map<String, String> params) {
        try {
            if (!isValidSignature(params)) {
                return Map.of("RspCode", "97", "Message", "Invalid signature");
            }

            Long hoaDonId = parseInvoiceId(params.get("vnp_TxnRef"));
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
            if (hoaDon == null) {
                return Map.of("RspCode", "01", "Message", "Order not found");
            }

            BigDecimal amount = parseVnpAmount(params.get("vnp_Amount"));
            if (hoaDon.getTongTien() == null || hoaDon.getTongTien().compareTo(amount) != 0) {
                return Map.of("RspCode", "04", "Message", "invalid amount");
            }

            if (hoaDon.getTrangThai() != null && "2".equals(hoaDon.getTrangThai())) {
                return Map.of("RspCode", "02", "Message", "Order already confirmed");
            }

            boolean success = "00".equals(params.get("vnp_ResponseCode")) && "00".equals(params.get("vnp_TransactionStatus"));
            if (success) {
                String transactionNo = params.get("vnp_TransactionNo");
                if (transactionNo != null && !transactionNo.isBlank()) {
                    hoaDon.setMaGiaoDich(transactionNo);
                    hoaDonRepository.save(hoaDon);
                }
                bookingService.confirmPayment(hoaDonId);
            } else {
                bookingService.cancelBooking(hoaDonId);
            }

            return Map.of("RspCode", "00", "Message", "Confirm Success");
        } catch (IllegalArgumentException e) {
            return Map.of("RspCode", "01", "Message", "Order not found");
        } catch (Exception e) {
            return Map.of("RspCode", "99", "Message", "Unknown error");
        }
    }

    public String buildReturnRedirectUrl(Map<String, String> params) {
        Long hoaDonId = extractInvoiceIdSafely(params.get("vnp_TxnRef"));
        boolean validSignature = isValidSignature(params);
        boolean success = validSignature && "00".equals(params.get("vnp_ResponseCode")) && "00".equals(params.get("vnp_TransactionStatus"));

        if (hoaDonId != null && validSignature) {
            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
            if (hoaDon != null && hoaDon.getTongTien() != null) {
                BigDecimal amount = parseVnpAmount(params.get("vnp_Amount"));
                if (hoaDon.getTongTien().compareTo(amount) == 0) {
                    if (success) {
                        String transactionNo = params.get("vnp_TransactionNo");
                        if (transactionNo != null && !transactionNo.isBlank()) {
                            hoaDon.setMaGiaoDich(transactionNo);
                            hoaDonRepository.save(hoaDon);
                        }
                        bookingService.confirmPayment(hoaDonId);
                    } else {
                        bookingService.cancelBooking(hoaDonId);
                    }
                }
            }
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(frontendResultUrl()).queryParam(
            "status",
            success ? "success" : "failed"
        );

        if (hoaDonId != null) {
            builder.queryParam("hoaDonId", hoaDonId);
        }
        if (!success) {
            builder.queryParam("message", resolveFailureMessage(params, validSignature));
        }
        return builder.toUriString();
    }

    private String resolveFailureMessage(Map<String, String> params, boolean validSignature) {
        if (!validSignature) {
            return "Chữ ký phản hồi từ VNPAY không hợp lệ.";
        }
        String responseCode = params.get("vnp_ResponseCode");
        if ("24".equals(responseCode)) {
            return "Bạn đã hủy giao dịch trên VNPAY.";
        }
        if ("51".equals(responseCode)) {
            return "Tài khoản không đủ số dư để thanh toán.";
        }
        return "Thanh toán chưa thành công. Mã phản hồi: " + (responseCode == null ? "không xác định" : responseCode);
    }

    private String frontendResultUrl() {
        String configuredReturnUrl = resolveReturnUrl();
        if (configuredReturnUrl.endsWith("/api/payment/vnpay/return")) {
            return (
                configuredReturnUrl.substring(0, configuredReturnUrl.length() - "/api/payment/vnpay/return".length()) + "/dat-ve/ket-qua"
            );
        }
        return "http://localhost:9000/dat-ve/ket-qua";
    }

    private String resolveTmnCode() {
        if (tmnCode == null || tmnCode.isBlank() || "DEMO1234".equalsIgnoreCase(tmnCode.trim())) {
            return DEFAULT_SANDBOX_TMN_CODE;
        }
        return tmnCode.trim();
    }

    private String resolveHashSecret() {
        if (hashSecret == null || hashSecret.isBlank() || hashSecret.contains("VNPAY")) {
            return DEFAULT_SANDBOX_HASH_SECRET;
        }
        return hashSecret.trim();
    }

    private String resolvePayUrl() {
        if (payUrl == null || payUrl.isBlank()) {
            return DEFAULT_SANDBOX_PAY_URL;
        }
        return payUrl.trim();
    }

    private String resolveReturnUrl() {
        if (returnUrl == null || returnUrl.isBlank() || returnUrl.endsWith("/payment-return")) {
            return DEFAULT_SANDBOX_RETURN_URL;
        }
        return returnUrl.trim();
    }

    private String buildTxnRef(HoaDon hoaDon) {
        return String.valueOf(hoaDon.getId());
    }

    private Long parseInvoiceId(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) {
            throw new IllegalArgumentException("TxnRef missing");
        }
        return Long.parseLong(txnRef);
    }

    private Long extractInvoiceIdSafely(String txnRef) {
        try {
            return parseInvoiceId(txnRef);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseVnpAmount(String amount) {
        if (amount == null || amount.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(amount).divide(BigDecimal.valueOf(100));
    }

    private String normalizeClientIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "127.0.0.1";
        }
        String ip = clientIp.contains(",") ? clientIp.split(",")[0].trim() : clientIp.trim();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

    private String sanitizeOrderInfo(String value) {
        String ascii = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        ascii = ascii.replaceAll("[^A-Za-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        return ascii.isBlank() ? "Thanh toan don hang" : ascii;
    }

    private String buildEncodedQuery(Map<String, String> params) {
        return params
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
            .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
            .collect(java.util.stream.Collectors.joining("&"));
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }
}
