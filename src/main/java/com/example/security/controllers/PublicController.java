package com.example.security.controllers;

import com.example.security.dto.CustomerLogin;
import com.example.security.entity.Customer;
import com.example.security.services.UserService;
import com.example.security.utils.ApiResponse;
import com.example.security.utils.CryptoConverter;
import com.example.security.utils.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ApiResponse> loginUser(@RequestBody CustomerLogin customerRequest, HttpServletResponse response) {
        CryptoConverter converter = new CryptoConverter();
        String password = customerRequest.getPassword();
        String encryptPass = converter.convertToDatabaseColumn(password);

        Optional<Customer> optCustomer = userService.login(customerRequest.getUsername(), encryptPass);
        if (optCustomer.isPresent()) {
            Customer customer = optCustomer.get();

            String token = jwtUtils.generateToken(customer);
            final Cookie cookie = new Cookie("auth", token);

            cookie.setSecure(false);
            cookie.setHttpOnly(false);
            cookie.setMaxAge(COOKIE_EXPIRE);
            cookie.setPath("/");
            response.addCookie(cookie);

            ApiResponse apiResponse = new ApiResponse(true, token, "Login success");
            return ResponseEntity.status(200).body(apiResponse);
        } else {
            ApiResponse apiResponse = new ApiResponse(false, null, "user not found");
            return ResponseEntity.status(400).body(apiResponse);
        }
    }
}
