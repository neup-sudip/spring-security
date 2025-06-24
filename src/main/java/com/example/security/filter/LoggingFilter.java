package com.example.security.filter;


import com.example.security.entity.UserActivity;
import com.example.security.events.UserActivityEvent;
import com.example.security.models.ActivityProperty;
import com.example.security.models.ApiResponse;
import com.example.security.utils.HttpHelpers;
import com.example.security.utils.RequestWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        long beginTime = System.currentTimeMillis();

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);

        HttpServletRequest request = new RequestWrapper(requestWrapper);

        filterChain.doFilter(request, responseWrapper);

        String requestParam = HttpHelpers.getRequestBody(request);
        ActivityProperty prop = HttpHelpers.getActivity(request);

        long timeCost = System.currentTimeMillis() - beginTime;

        trace(responseWrapper, requestWrapper, requestParam, prop, timeCost);

        publishActivity(request, prop, responseWrapper);

        responseWrapper.copyBodyToResponse();
    }

    private void trace(ContentCachingResponseWrapper responseWrapper, ContentCachingRequestWrapper requestWrapper, String requestParam,
                       ActivityProperty prop, long timeCost) {

        String username = requestWrapper.getUserPrincipal() == null ? "____" : requestWrapper.getUserPrincipal().getName();

        ApiResponse response = getResponse(responseWrapper);
        final String httpsStatus = HttpStatus.valueOf(responseWrapper.getStatus()).name();

        log.info(
                "[REQUEST({}) > username: {}, ip: {}, Agent: {}, URI: {}, Parameters: {}, RESPONSE({}), TotalTimeCost: [{}]ms",
                prop.getMethod(), username, prop.getIp(), prop.getAgent(), prop.getUri(), requestParam, httpsStatus, timeCost);

        log.info("RESPONSE: {}", response);
    }

    private void publishActivity(HttpServletRequest request, ActivityProperty prop, ContentCachingResponseWrapper responseWrapper){

        if(prop.getUri().equals("/public/auth/login")){
            return;
        }

        ApiResponse response = getResponse(responseWrapper);

        UserActivity activity = UserActivity.builder()
                .user(request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous")
                .recordedAt(LocalDateTime.now())
                .ip(prop.getIp())
                .agent(prop.getAgent())
                .uri(prop.getUri())
                .responseCode(response.isResult() ? "000" : "001")
                .responseMessage(response.getMessage())
                .build();

        eventPublisher.publishEvent(new UserActivityEvent(this, activity));
    }

    private ApiResponse getResponse(ContentCachingResponseWrapper responseWrapper){
        try{
            final String result = new String(responseWrapper.getContentAsByteArray());

            return new ObjectMapper().readValue(result, ApiResponse.class);
        }catch (Exception e){
            return ApiResponse.builder().build();
        }
    }
}