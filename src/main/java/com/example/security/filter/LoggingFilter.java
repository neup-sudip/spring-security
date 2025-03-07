package com.example.security.filter;


import com.example.security.models.ActivityProperty;
import com.example.security.utils.HttpHelpers;
import com.example.security.utils.RequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class LoggingFilter extends OncePerRequestFilter {

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

        responseWrapper.copyBodyToResponse();
    }

    private void trace(ContentCachingResponseWrapper responseWrapper, ContentCachingRequestWrapper requestWrapper, String requestParam,
                       ActivityProperty prop, long timeCost) {

        String username = requestWrapper.getUserPrincipal() == null ? "____" : requestWrapper.getUserPrincipal().getName();

        final String result = new String(responseWrapper.getContentAsByteArray());
        final String httpsStatus = HttpStatus.valueOf(responseWrapper.getStatus()).name();

        log.info(
                "[REQUEST({}) > username: {}, ip: {}, Agent: {}, URI: {}, Parameters: {}, RESPONSE({}), TotalTimeCost: [{}]ms",
                prop.getMethod(), username, prop.getIp(), prop.getAgent(), prop.getUri(), requestParam, httpsStatus, timeCost);

        log.info("RESPONSE DATA: {}", result);
    }
}