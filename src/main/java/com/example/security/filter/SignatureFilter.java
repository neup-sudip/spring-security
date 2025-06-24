package com.example.security.filter;

import com.example.security.utils.CryptoUtils;
import com.example.security.utils.HttpHelpers;
import com.example.security.utils.RequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

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

        log.info("Received Signature: {}", signature);

        try {
            PrivateKey privateKey = CryptoUtils.privateKeyFromPem("files/PrivateKey.pem");
            PublicKey publicKey = CryptoUtils.publicKeyFromPem("files/PublicKey.pem");
            String signatureG = CryptoUtils.generateSignature(requestParams, privateKey);
            log.info("FROM GENERATION: {}", CryptoUtils.verifySignature(requestParams, signatureG, publicKey));
            log.info("FROM POSTMAN: {}", CryptoUtils.verifySignature(requestParams, signature, publicKey));

        } catch (Exception e) {
            log.error("Exception: ", e);
        }


//        if(StringUtils.isNotBlank(requestParams) && !StringUtils.equals(CryptoUtils.decrypt(signature), requestParams)){
//            log.error("Returning Invalid signature.");
//            ObjectMapper objectMapper = new ObjectMapper();
//            ApiResponse apiResponse = new ApiResponse(false, "", "Invalid Signature.");
//            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
//            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
//            httpServletResponse.getWriter().write(objectMapper.writeValueAsString(apiResponse));
//            return;
//        }

        log.info("Signature validated !");

        filterChain.doFilter(request, responseWrapper);

        responseWrapper.copyBodyToResponse();
    }
}