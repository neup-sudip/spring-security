package com.example.security.services;

import com.example.security.dto.CustomerDto;
import com.example.security.entity.Customer;
import com.example.security.repos.UserRepository;
import com.example.security.models.ApiResponse;
import com.example.security.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.security.mapper.CustomMapper.dtoCustomMapper;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<CustomerDto> getUsers() {
        return userRepository.findAll(Sort.by("id")).stream()
                .map(dtoCustomMapper::cutomerToCustomerDto).collect(Collectors.toList());
    }

    public Optional<CustomerDto> getUserByUsername(String username){
        Optional<Customer> customer = userRepository.findByUsername(username);
        return customer.map(dtoCustomMapper::cutomerToCustomerDto);
    }

    public Optional<CustomerDto> getUserById(long id) {
        Optional<Customer> customer = userRepository.findById(id);
        return customer.map(dtoCustomMapper::cutomerToCustomerDto);
    }

    public CustomerDto addNewUser(Customer customer) {
        Optional<Customer> emailOrUsernameExist = userRepository.findByUsername(customer.getUsername());
        if (emailOrUsernameExist.isPresent()) {
            throw new CustomException("UserName is taken", 400);
        }
        return dtoCustomMapper.cutomerToCustomerDto(userRepository.save(customer));
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

        ApiResponse apiResponse = new ApiResponse(true, dtoCustomMapper.cutomerToCustomerDto(newCustomer),
                "User updated successfully !");
        return ResponseEntity.status(200).body(apiResponse);
    }

    public Optional<Customer> login(String username){
        return userRepository.findByUsername(username);
    }

}
