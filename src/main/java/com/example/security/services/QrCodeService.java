package com.example.security.services;

import com.example.security.entity.QrAuth;
import com.example.security.repos.QrAuthRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final QrAuthRepository qrAuthRepository;

    public String generateQrCodeImage(String token) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(token, BarcodeFormat.QR_CODE, 250, 250);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        byte[] qrCodeImage = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(qrCodeImage);
    }

    public void addOrUpdate(QrAuth qrAuth){
        qrAuthRepository.save(qrAuth);
    }

    public Optional<QrAuth> getQrAuth(String id){
        return qrAuthRepository.findById(id);
    }
}