package com.example.security.controllers;

import com.example.security.dto.QrAuthDto;
import com.example.security.entity.QrAuth;
import com.example.security.enums.QrAuthState;
import com.example.security.services.QrCodeService;
import com.example.security.utils.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/public/auth/qr")
@RequiredArgsConstructor
@Slf4j
public class QrAuthController {

    private final QrCodeService qrCodeService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HttpServletRequest httpServletRequest;

    @PostMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQr() {
        try {
            String uniqueToken = UUID.randomUUID().toString();

            byte[] qrImageBytes = qrCodeService.generateQrCodeImage(uniqueToken);

            QrAuth auth = QrAuth.builder()
                    .id(uniqueToken)
                    .createAt(LocalDateTime.now())
                    .status(QrAuthState.PENDING.name())
                    .build();

            qrCodeService.addOrUpdate(auth);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.set("X-Token", uniqueToken);

            return new ResponseEntity<>(qrImageBytes, headers, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Exception qr generation: ", ex);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyQr(@RequestBody QrAuthDto request) {
        try {
            Optional<QrAuth> optionalQrAuth = qrCodeService.getQrAuth(request.getToken());

            if (optionalQrAuth.isEmpty()) {
                return ResponseEntity.status(400).body(ApiResponse.failed("Invalid login request."));
            }

            QrAuth qrAuth = optionalQrAuth.get();
            long minutesBefore = Math.abs(Duration.between(qrAuth.getCreateAt(), LocalDateTime.now()).toMinutes());

            if (!StringUtils.equals(qrAuth.getStatus(), QrAuthState.PENDING.name()) || minutesBefore >= 2) {
                return ResponseEntity.status(400).body(ApiResponse.failed("Session expired. Please create new one"));
            }

            qrAuth.setStatus(QrAuthState.VERIFIED.name());
            qrAuth.setUpdateAt(LocalDateTime.now());
            qrCodeService.addOrUpdate(qrAuth);

            // Send a WebSocket message to notify successful authentication
            messagingTemplate.convertAndSend("/topic/authenticate", request.getToken());

            return ResponseEntity.status(200).body(ApiResponse.success(null, "User authenticated successfully."));
        } catch (Exception ex) {
            log.error("Exception qr generation: ", ex);
            return ResponseEntity.status(500).body(ApiResponse.failed("Error generating QR."));
        }
    }
}