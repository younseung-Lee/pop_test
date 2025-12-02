package com.example.pop.interceptor;

import com.example.pop.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {
    
    private final LoginService loginService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
//        log.info("인증 체크 인터셉터 실행: {}", requestURI);
        
        // 로그인 체크
        if (!loginService.isLoggedIn(request.getSession())) {
            log.info("미인증 사용자 요청: {}", requestURI);
            // 로그인으로 리다이렉트
            response.sendRedirect("/login?redirectURL=" + requestURI);
            return false;
        }
        
        return true;
    }
}
