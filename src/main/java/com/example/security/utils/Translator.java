package com.example.security.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Translator {

    private final MessageSource messageSource;
    private final HttpServletRequest request;

    public String getMessage(String code){
        String lang = request.getHeader("lang");
        return messageSource.getMessage(code, null, new Locale(StringUtils.isBlank(lang) ? "en" : lang));
    }
}