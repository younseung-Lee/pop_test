package com.example.pop.controller;

import com.example.pop.service.login.LoginService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 로그인 처리
     */
    @PostMapping("/login")
    public String login(@RequestParam Map<String, Object> params,
                        HttpSession session,
                        Model model) {

        log.info("로그인 요청 - params: {}", params);

        Map<String, Object> user = loginService.login(params, session);

        if (user != null) {
            log.info("로그인 성공 - ID: {}", params.get("id"));
            return "redirect:/main";
        } else {
            log.warn("로그인 실패 - ID: {}", params.get("id"));
            model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
            model.addAttribute("id", params.get("id")); // 입력했던 아이디 유지
            return "login";
        }
    }

    /**
     * 로그아웃
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        loginService.logout(session);
        return "redirect:/login";
    }

    /**
     * 메인 페이지
     */
    @GetMapping("/main")
    public String main(HttpSession session, Model model) {
        Map<String, Object> user = loginService.getLoginUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "main";
    }

    /**
     * 홈(/) → 로그인 여부에 따라 분기
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
