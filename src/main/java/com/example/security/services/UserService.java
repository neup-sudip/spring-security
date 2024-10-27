package com.example.security.services;

import com.example.security.entity.Customer;
import com.example.security.repos.UserRepository;
import com.example.security.utils.ApiResponse;
import com.example.security.utils.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<Customer> getUsers() {
        return userRepository.findAll(Sort.by("id"));
    }

    public Optional<Customer> getUserByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public Optional<Customer> getUserById(long id) {
        return userRepository.findById(id);
    }

    public Customer addNewUser(Customer customer) {
        Optional<Customer> emailOrUsernameExist = userRepository.findByUsername(customer.getUsername());
        if (emailOrUsernameExist.isPresent()) {
            throw new CustomException("UserName is taken", 400);
        }
        return userRepository.save(customer);
    }

    public ResponseEntity<ApiResponse> updateUser(Customer customer, long id) {
        Customer prevCustomer = userRepository.findById(id).orElse(null);
        if (prevCustomer == null) {
            ApiResponse apiResponse = new ApiResponse(false, null, "User not found !");
            return ResponseEntity.status(400).body(apiResponse);
        }

        Customer emailOrUsernameExist = userRepository.findByNotIdAndUsername(id, customer.getUsername());
        if (emailOrUsernameExist != null) {
            ApiResponse apiResponse = new ApiResponse(false, null, "Email/Username already exist !");
            return ResponseEntity.status(400).body(apiResponse);
        }

        prevCustomer.setRole(customer.getRole());
        prevCustomer.setUsername(customer.getUsername());
        prevCustomer.setPassword(customer.getPassword());
        Customer newCustomer = userRepository.save(prevCustomer);

        ApiResponse apiResponse = new ApiResponse(true, newCustomer, "User updated successfully !");
        return ResponseEntity.status(200).body(apiResponse);
    }

    public Optional<Customer> login(String username, String password){
        return Optional.ofNullable(userRepository.findByUsernameAndPassword(username, password));
    }

}
