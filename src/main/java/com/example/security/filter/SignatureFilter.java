package com.example.security.filter;

import com.example.security.utils.ApiResponse;
import com.example.security.utils.CryptoUtils;
import com.example.security.utils.HttpHelpers;
import com.example.security.utils.RequestWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class SignatureFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);

        HttpServletRequest request = new RequestWrapper(requestWrapper);

        String requestParams = HttpHelpers.getRequestBody(request);
        String signature = requestWrapper.getHeader("X-Signature");

        if(StringUtils.isNotBlank(requestParams) && !StringUtils.equals(CryptoUtils.decrypt(signature), requestParams)){
            log.error("Returning Invalid signature.");
            ObjectMapper objectMapper = new ObjectMapper();
            ApiResponse apiResponse = new ApiResponse(false, "", "Invalid Signature.");
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return;
        }

        log.info("Signature validated !");

        filterChain.doFilter(request, responseWrapper);

        responseWrapper.copyBodyToResponse();
    }
}