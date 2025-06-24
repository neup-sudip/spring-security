package com.example.security.controllers;

import com.example.security.dto.CustomerLogin;
import com.example.security.entity.Customer;
import com.example.security.entity.UserActivity;
import com.example.security.events.UserActivityEvent;
import com.example.security.models.ActivityProperty;
import com.example.security.services.UserService;
import com.example.security.models.ApiResponse;
import com.example.security.utils.HttpHelpers;
import com.example.security.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
public class PublicController {

    private final HttpServletRequest request;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse> loginUser(@RequestBody CustomerLogin customerRequest) {
        ApiResponse apiResponse;
        Optional<Customer> optCustomer = userService.login(customerRequest.getUsername());
        if (optCustomer.isPresent()) {
            apiResponse = new ApiResponse(false, null, "Username/password did not match.");
            Customer customer = optCustomer.get();
            if(StringUtils.equals(optCustomer.get().getPassword(), customerRequest.getPassword())){
                apiResponse = new ApiResponse(true, jwtUtils.getAuthResponse(customer), "Login success");
            }
        } else {
            apiResponse = new ApiResponse(false, null, "User not found.");
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
}
