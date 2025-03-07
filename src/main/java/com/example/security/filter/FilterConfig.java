package com.example.security.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<SignatureFilter> signatureFilter() {
        FilterRegistrationBean<SignatureFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SignatureFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(3);
        return registrationBean;
    }
}