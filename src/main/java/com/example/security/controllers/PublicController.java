package com.example.security.controllers;

import com.example.security.dto.CustomerLogin;
import com.example.security.entity.Customer;
import com.example.security.services.UserService;
import com.example.security.models.ApiResponse;
import com.example.security.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
public class PublicController {

    @Value("${cookie.expire.time}")
    private int COOKIE_EXPIRE;

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse> loginUser(@RequestBody CustomerLogin customerRequest) {
        Optional<Customer> optCustomer = userService.login(customerRequest.getUsername());
        if (optCustomer.isPresent() && StringUtils.equals(optCustomer.get().getPassword(), customerRequest.getPassword())) {
            Customer customer = optCustomer.get();

            ApiResponse apiResponse = new ApiResponse(true, jwtUtils.getAuthResponse(customer),
                    "Login success");
            return ResponseEntity.status(200).body(apiResponse);
        } else {
            ApiResponse apiResponse = new ApiResponse(false, null, "User not found.");
            return ResponseEntity.status(400).body(apiResponse);
        }
    }
}
