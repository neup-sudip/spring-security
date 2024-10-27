package com.example.security.filter;


import com.example.security.models.ActivityProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long beginTime = System.currentTimeMillis();

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(requestWrapper, responseWrapper);

        String requestParam = convertString(requestWrapper.getContentAsByteArray());
        ActivityProperty prop = getActivityProperty(request);

        updateResponse(prop.getUri(), responseWrapper);

        long timeCost = System.currentTimeMillis() - beginTime;

        trace(responseWrapper, request, requestParam, prop, timeCost);
    }

    private void trace(ContentCachingResponseWrapper responseWrapper, HttpServletRequest request, String requestParam,
                       ActivityProperty prop, long timeCost) {

        String username = request.getUserPrincipal() == null ? "NO AUTH" : request.getUserPrincipal().getName();
        final String prettyRequestParam = beautifyJson(requestParam);

        if (isNotJsonContentType(responseWrapper.getContentType())) {
            log.info(
                    "[REQUEST({}) > username: {}, ip: {}, Agent: {}, URI: {}, Parameters: {}, TotalTimeCost: [{}]ms ",
                    prop.getMethod(), username, prop.getIp(), prop.getAgent(), prop.getUri(), prettyRequestParam, timeCost);
        } else {
            final String result = convertString(responseWrapper.getContentAsByteArray());
            final String prettyResult = beautifyJson(result);
            final String httpsStatus = HttpStatus.valueOf(responseWrapper.getStatus()).name();

            log.info(
                    "[REQUEST({}) > username: {}, ip: {}, Agent: {}, URI: {}, Parameters: {}, RESPONSE({}), TotalTimeCost: [{}]ms",
                    prop.getMethod(), username, prop.getIp(), prop.getAgent(), prop.getUri(), prettyRequestParam,
                    httpsStatus, timeCost);

            log.info("RESPONSE DATA: {}", prettyResult);
        }
    }


    private boolean isNotJsonContentType(String contentType) {
        return contentType == null || !contentType.toLowerCase().startsWith("application/json");
    }

    private String convertString(byte[] contentByte) {
        return new String(contentByte, StandardCharsets.UTF_8);
    }

    private void updateResponse(String requestURI, ContentCachingResponseWrapper responseWrapper) {
        try {
            HttpServletResponse rawResponse = (HttpServletResponse) responseWrapper.getResponse();
            byte[] body = responseWrapper.getContentAsByteArray();
            try (ServletOutputStream outputStream = rawResponse.getOutputStream()) {

                if (rawResponse.isCommitted() && body.length > 0) {
                    StreamUtils.copy(body, outputStream);

                } else if (body.length > 0) {
                    rawResponse.setContentLength(body.length);
                    StreamUtils.copy(body, rawResponse.getOutputStream());
                }

                finishResponse(outputStream, body);
            }
        } catch (Exception ex) {
            log.error("Error: {} on request address: {}", ex.getMessage(), requestURI);
        }
    }

    private void finishResponse(ServletOutputStream outputStream, byte[] body) throws IOException {
        if (body.length > 0) {
            outputStream.flush();
            outputStream.close();
        }
    }

    private String beautifyJson(String json) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            JsonElement je = JsonParser.parseString(json);
            return gson.toJson(je);
        } catch (Exception e) {
            return json;
        }
    }


    public static ActivityProperty getActivityProperty(HttpServletRequest request) {
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
}