package com.example.pop.controller;

import com.example.pop.service.LoginService;
import com.example.pop.vo.MartIpVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {
    
    private final LoginService loginService;
    
    /**
     * 로그인 페이지 요청
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    /**
     * 로그인 처리 요청
     */
    @PostMapping("/login")
    public String login(@RequestParam String id, 
                       @RequestParam String pw,
                       HttpSession session,
                       Model model) {
        
        log.info("로그인 요청 - ID: {}", id);
        
        boolean loginSuccess = loginService.login(id, pw, session);
        
        if (loginSuccess) {
            log.info("로그인 성공 - ID: {}", id);
            return "redirect:/main";
        } else {
            log.warn("로그인 실패 - ID: {}", id);
            model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
            model.addAttribute("id", id);  // 입력했던 아이디 유지
            return "login";
        }
    }
    
    /**
     * 로그아웃 요청
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        loginService.logout(session);
        return "redirect:/login";
    }
    
    /**
     * 메인 페이지 요청
     */
    @GetMapping("/main")
    public String main(HttpSession session, Model model) {
        MartIpVO user = loginService.getLoginUser(session);
        
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        return "main";
    }
    
    /**
     * 홈 페이지 요청 (루트 경로)
     */
    @GetMapping("/")
    public String home(HttpSession session) {
        // 로그인되어 있으면 메인으로, 아니면 로그인 페이지로
        if (loginService.isLoggedIn(session)) {
            return "redirect:/main";
        }
        return "redirect:/login";
    }
}
