package com.example.security.controllers;

import com.example.security.dto.CustomerLogin;
import com.example.security.entity.Customer;
import com.example.security.entity.UserActivity;
import com.example.security.events.UserActivityEvent;
import com.example.security.models.ActivityProperty;
import com.example.security.entity.QrAuth;
import com.example.security.enums.QrAuthState;
import com.example.security.services.QrCodeService;
import com.example.security.services.UserService;
import com.example.security.models.ApiResponse;
import com.example.security.utils.HttpHelpers;
import com.example.security.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
@Slf4j
public class PublicController {

    private final HttpServletRequest request;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final ApplicationEventPublisher eventPublisher;
    private final QrCodeService qrCodeService;

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse> loginUser(@RequestBody CustomerLogin customerRequest) {
        ApiResponse apiResponse;
        Optional<Customer> optCustomer = userService.login(customerRequest.getUsername());
        if (optCustomer.isPresent()) {
            apiResponse = ApiResponse.failed("Username/password did not match.");
            Customer customer = optCustomer.get();
            if(StringUtils.equals(optCustomer.get().getPassword(), customerRequest.getPassword())){
                apiResponse = ApiResponse.success(jwtUtils.getAuthResponse(customer), "Login success");
            }
        } else {
            apiResponse = ApiResponse.failed("User not found.");
        }
        publishActivity(apiResponse, customerRequest.getUsername());
        return ResponseEntity.status(200).body(apiResponse);
    }

    private void publishActivity(ApiResponse response, String username){

        ActivityProperty prop = HttpHelpers.getActivity(request);

        UserActivity activity = UserActivity.builder()
                .user(username)
                .recordedAt(LocalDateTime.now())
                .ip(prop.getIp())
                .agent(prop.getAgent())
                .uri(prop.getUri())
                .responseCode(response.isResult() ? "000" : "001")
                .responseMessage(response.getMessage())
                .build();

        eventPublisher.publishEvent(new UserActivityEvent(this, activity));
    }

    @PostMapping(value = "/auth/qr/generate", produces = MediaType.IMAGE_PNG_VALUE)
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
}
