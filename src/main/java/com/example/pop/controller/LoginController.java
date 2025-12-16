package com.example.pop.controller;

import com.example.pop.service.login.LoginService;
import com.example.pop.vo.MartIpVO;
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

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam Map<String, Object> params,
                        HttpSession session,
                        Model model) {

        MartIpVO user = loginService.login(params, session);

        if (user != null) {
            return "redirect:/main";
        }

        model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
        model.addAttribute("id", params.get("id"));
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        loginService.logout(session);
        return "redirect:/login";
    }

    @GetMapping("/main")
    public String main(HttpSession session, Model model) {
        MartIpVO user = (MartIpVO) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "main";
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        return (session.getAttribute("user") != null)
                ? "redirect:/main"
                : "redirect:/login";
    }
}
