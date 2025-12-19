package com.example.pop.controller;

import com.example.pop.service.template.TemplateService;
import com.example.pop.vo.MartIpVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/templates")
public class TemplatePageController {

    private final TemplateService templateService;

    /**
     * 공통 템플릿 등록 페이지 (a4 관리자만)
     */
    @GetMapping("/common/new")
    public String commonNew(HttpSession session) {
        MartIpVO user = (MartIpVO) session.getAttribute("user");
        
        // 로그인 체크는 인터셉터에서 처리되므로, 관리자 권한만 체크
        templateService.validateAdminUser(user);
        
        return "template/template_new";
    }
}
