package com.mycompany.myapp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QRCodeService {

    private final Logger log = LoggerFactory.getLogger(QRCodeService.class);

    private static final int QR_CODE_SIZE = 200;

    public String generateQRCode(String data) {
        log.debug("Generating QR Code for data: {}", data);
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            return Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) {
            log.error("Error generating QR Code", e);
            throw new RuntimeException("Lỗi tạo mã QR Code", e);
        }
    }

    public String generateTicketQRCode(Long ticketId, String movieName, String showTime) {
        String data = String.format("TICKET|ID:%d|MOVIE:%s|TIME:%s", ticketId, movieName, showTime);
        return generateQRCode(data);
    }
}
