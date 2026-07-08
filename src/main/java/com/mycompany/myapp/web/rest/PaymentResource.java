package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/payment")
public class PaymentResource {

    private final VnPayService vnPayService;

    public PaymentResource(VnPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @PostMapping("/vnpay/create-url/{hoaDonId}")
    public ResponseEntity<Map<String, String>> createVnpayPaymentUrl(
        @PathVariable Long hoaDonId,
        @RequestParam(name = "qrOnly", defaultValue = "true") boolean qrOnly,
        HttpServletRequest request
    ) {
        String paymentUrl = vnPayService.createPaymentUrl(hoaDonId, extractClientIp(request), qrOnly);
        Map<String, String> body = new HashMap<>();
        body.put("paymentUrl", paymentUrl);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> handleVnpayIpn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(vnPayService.handleIpn(params));
    }

    @GetMapping(value = "/vnpay/return", produces = MediaType.TEXT_HTML_VALUE)
    public RedirectView handleVnpayReturn(@RequestParam Map<String, String> params) {
        return new RedirectView(vnPayService.buildReturnRedirectUrl(params));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor;
        }
        return request.getRemoteAddr();
    }
}
