package com.example.security.controllers;

import com.example.security.entity.Customer;
import com.example.security.services.UserService;
import com.example.security.utils.ApiResponse;
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
        ApiResponse apiResponse = new ApiResponse(true, userService.getUsers(), "All users fetched");
        return ResponseEntity.status(200).body(apiResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GET_SINGLE_USER')")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable long id) {
        Optional<Customer> customer = userService.getUserById(id);
        if (customer.isEmpty()) {
            ApiResponse apiResponse = new ApiResponse(true, null, "Error fetching user");
            return ResponseEntity.status(400).body(apiResponse);
        } else {
            ApiResponse apiResponse = new ApiResponse(true, customer.get(), "User fetched successfully");
            return ResponseEntity.status(200).body(apiResponse);
        }
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<ApiResponse> createUser(@RequestBody Customer newCustomer) {
        Customer customer = userService.addNewUser(newCustomer);
        ApiResponse apiResponse = new ApiResponse(true, customer, "User created successfully !");
        return ResponseEntity.status(200).body(apiResponse);
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<ApiResponse> editUser(@RequestBody Customer newCustomer, @PathVariable long id) {
        return userService.updateUser(newCustomer, id);
    }
}
