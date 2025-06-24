package com.example.security.controllers;

import com.example.security.dto.CustomerDto;
import com.example.security.entity.Customer;
import com.example.security.services.UserService;
import com.example.security.models.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('GET_ALL_USER')")
    public ResponseEntity<ApiResponse> getAllUsers() {
        ApiResponse apiResponse = ApiResponse.success(userService.getUsers(), "All users fetched");
        return ResponseEntity.status(200).body(apiResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET_SINGLE_USER')")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable long id) {
        Optional<CustomerDto> customer = userService.getUserById(id);
        if (customer.isEmpty()) {
            ApiResponse apiResponse = ApiResponse.failed("User not found.");
            return ResponseEntity.status(400).body(apiResponse);
        } else {
            ApiResponse apiResponse = ApiResponse.success(customer.get(), "User fetched successfully");
            return ResponseEntity.status(200).body(apiResponse);
        }
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<ApiResponse> createUser(@RequestBody Customer newCustomer) {
        CustomerDto customer = userService.addNewUser(newCustomer);
        ApiResponse apiResponse = ApiResponse.success(customer, "User created successfully !");
        return ResponseEntity.status(200).body(apiResponse);
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<ApiResponse> editUser(@RequestBody Customer newCustomer, @PathVariable long id) {
        return userService.updateUser(newCustomer, id);
    }
}
