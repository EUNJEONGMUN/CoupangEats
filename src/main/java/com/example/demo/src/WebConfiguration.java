package com.example.demo.src;

import com.example.demo.utils.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CertificationInterceptor(jwtService, objectMapper))
                .order(1)
                .addPathPatterns("/*");
    }
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new CertificationInterceptor(jwtService))
//                .order(1)
//                .addPathPatterns("/**");
//    }
}
