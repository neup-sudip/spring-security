package com.example.security.utils;

import com.example.security.models.ActivityProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class HttpHelpers {

    private HttpHelpers(){}

    public static ActivityProperty getActivity(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String ip = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();
        return ActivityProperty.builder().uri(requestURI).ip(ip).agent(userAgent).method(method).build();
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return new StringTokenizer(xForwardedForHeader, ",").nextToken().trim();
        }
    }

    public static String getRequestBody(HttpServletRequest httpServletRequest) throws IOException {
        HttpServletRequestWrapper request = new HttpServletRequestWrapper(httpServletRequest);

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line.trim());
        }
        String requestBody = stringBuilder.toString().trim();

        ObjectMapper objectMapper = new ObjectMapper();

        Object jsonNode = objectMapper.readTree(requestBody);

        return objectMapper.writeValueAsString(jsonNode);
    }
}