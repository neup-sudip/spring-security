package com.example.security.controllers;

import com.example.security.services.TestService;
import com.example.security.models.ApiResponse;
import com.example.security.utils.Translator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("public/test/")
public class TestController {
    private final TestService testService;
    private final Translator translator;

    @GetMapping("cache/{key}")
    private ResponseEntity<ApiResponse> getTest(@PathVariable String key){
        return ResponseEntity.ok(ApiResponse.success(testService.testCache(key), "Cache data."));
    }

    @PostMapping()
    public ResponseEntity<ApiResponse> getString() {
        log.info("Hit test API");
        return ResponseEntity.ok(new ApiResponse(true, null, translator.getMessage("hello.sir")));
    }

}