package com.example.pop.config;

import com.example.pop.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final LoginCheckInterceptor loginCheckInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .order(1)
                .addPathPatterns("/**")  // 모든 경로에 인터셉터 적용
                .excludePathPatterns(
                        "/",                // 홈
                        "/login",           // 로그인 페이지
                        "/logout",          // 로그아웃
                        "/css/**",          // CSS 파일
                        "/js/**",           // JS 파일
                        "/images/**",       // 이미지 파일
                        "/favicon.ico",     // 파비콘
                        "/error"            // 에러 페이지
                );
    }
}
