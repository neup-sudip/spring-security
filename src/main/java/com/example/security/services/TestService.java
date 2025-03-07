package com.example.security.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class TestService {

    @Cacheable(value = "defaultCache", key = "#key", unless="#result == null")
    public String testCache(String key){
        Date date = new Date();
        log.info("Date: {}", date);
        return key + "-"+date;
    }
}
