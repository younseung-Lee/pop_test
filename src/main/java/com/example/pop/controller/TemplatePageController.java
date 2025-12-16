package com.example.pop.controller;

import com.example.pop.vo.MartIpVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/templates")
public class TemplatePageController {

    @GetMapping("/common/new")
    public String commonNew(HttpSession session) {
        MartIpVO user = (MartIpVO) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        if (!"a4".equalsIgnoreCase(user.getId())) return "redirect:/main";
        return "template/common_new";
    }
}

