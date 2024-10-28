package com.example.security.utils;

import com.example.security.dto.CustomerDto;
import com.example.security.entity.Customer;
import com.example.security.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartUpService implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) {
        try {
            CustomerDto user = userService.addNewUser(Customer.builder()
                    .role(1)
                    .username("username")
                    .password("password").build());

            log.info("USER added: {}", user);
        }catch (Exception ex){
            log.error("Exception in adding user: {}", ex.getMessage());
        }
    }
}