package com.example.security.controllers;

import com.example.security.services.TestService;
import com.example.security.utils.ApiResponse;
import com.example.security.utils.Translator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("public/test/")
public class TestController {
    private final TestService testService;
    private final Translator translator;

    @GetMapping("cache/{key}")
    private ResponseEntity<ApiResponse> getTest(@PathVariable String key){
        return ResponseEntity.ok(new ApiResponse(true, testService.testCache(key), "Cache data."));
    }

    @GetMapping()
    public String getString() {
        return translator.getMessage("hello.sir");
    }

}