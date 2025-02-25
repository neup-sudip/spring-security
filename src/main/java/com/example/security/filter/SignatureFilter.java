package com.example.security.filter;

import com.example.security.utils.RequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(3)
public class SignatureFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);

        HttpServletRequest request = new RequestWrapper(requestWrapper);

//        String requestParams = HttpHelpers.getRequestBody(request);
//        String signature = requestWrapper.getHeader("Signature");

//        if(!StringUtils.equals(CryptoUtils.decrypt(signature), requestParams)){
//            log.error("Returning Invalid signature.");
//            ObjectMapper objectMapper = new ObjectMapper();
//            ApiResponse apiResponse = new ApiResponse(false, "", "Invalid Signature.");
//            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
//            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
//            httpServletResponse.getWriter().write(objectMapper.writeValueAsString(apiResponse));
//            return;
//        }

        log.info("Signature validated !");

        chain.doFilter(request, responseWrapper);

        responseWrapper.copyBodyToResponse();
    }
}