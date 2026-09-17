package com.eventmgmt.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates QR code images using the ZXing library.
 *
 * Flow:
 * 1. Receive ticketId (UUID)
 * 2. Encode ticketId into a QR image (300x300 PNG)
 * 3. Save PNG to disk at: static/qr-codes/{ticketId}.png
 * 4. Return the public URL: /qr-codes/{ticketId}.png
 *
 * The QR image encodes only the ticketId string.
 * When scanned by the organizer's device, the ticketId is sent
 * to POST /api/attendance/scan for validation.
 */
@Slf4j
@Service
public class QrCodeService {

    @Value("${app.file.qr-code-dir}")
    private String qrCodeDir;

    @Value("${app.file.base-url}")
    private String baseUrl;

    private static final int QR_WIDTH  = 300;
    private static final int QR_HEIGHT = 300;
    private static final String FORMAT  = "PNG";

    /**
     * Generate a QR code PNG for the given ticketId.
     *
     * @param ticketId UUID string to encode
     * @return public URL to access the QR image
     */
    public String generateQrCode(String ticketId) {
        try {
            // Ensure output directory exists
            Path dirPath = Paths.get(qrCodeDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("Created QR code directory: {}", dirPath.toAbsolutePath());
            }

            // Configure QR encoding hints
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Encode ticketId into BitMatrix
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    ticketId,
                    BarcodeFormat.QR_CODE,
                    QR_WIDTH,
                    QR_HEIGHT,
                    hints
            );

            // Write PNG file to disk
            String fileName  = ticketId + ".png";
            Path   filePath  = dirPath.resolve(fileName);
            MatrixToImageWriter.writeToPath(bitMatrix, FORMAT, filePath);

            log.info("QR code generated for ticket: {}", ticketId);

            // Return the public URL
            return baseUrl + "/qr-codes/" + fileName;

        } catch (WriterException e) {
            log.error("Failed to encode QR code for ticket {}: {}", ticketId, e.getMessage());
            throw new RuntimeException("QR code generation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to save QR code file for ticket {}: {}", ticketId, e.getMessage());
            throw new RuntimeException("QR code save failed: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a QR code file from disk.
     * Called when a registration is cancelled.
     */
    public void deleteQrCode(String ticketId) {
        try {
            Path filePath = Paths.get(qrCodeDir).resolve(ticketId + ".png");
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted QR code for ticket: {}", ticketId);
            }
        } catch (IOException e) {
            log.warn("Could not delete QR code for ticket {}: {}", ticketId, e.getMessage());
        }
    }
}