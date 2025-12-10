package com.example.pop.controller;

import com.example.pop.service.template.TemplateService;
import com.example.pop.vo.MartIpVO;
import com.example.pop.vo.PopTemplateVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping("/templates")
    public Map<String, Object> getTemplates(
            @RequestParam(required = false) String layoutType,
            @RequestParam(required = false) String category
    ) {
        log.info("템플릿 조회 요청 - layoutType: {}, category: {}", layoutType, category);

        List<PopTemplateVO> list = templateService.getTemplates(layoutType, category);
        int totalCount = templateService.getTemplateCount(layoutType, category);

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("templates", list);

        return result;
    }

    /**
     * 최근 사용 템플릿 조회
     * - 로그인한 마트 기준
     * - /api/templates/recent?limit=6
     */
    @GetMapping("/templates/recent")
    public Map<String, Object> getRecentTemplates(
            @RequestParam(defaultValue = "6") int limit,
            HttpSession session
    ) {
        // 세션 키 이름은 실제 프로젝트에 맞게 변경 (예: "user", "loginUser" 등)
        MartIpVO user = (MartIpVO) session.getAttribute("loginUser");
        if (user == null) {
            throw new RuntimeException("로그인 정보가 없습니다.");
        }

        String martId = user.getId();
        log.info("최근 템플릿 조회 요청 - martId: {}, limit: {}", martId, limit);

        List<PopTemplateVO> list = templateService.getRecentTemplates(martId, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("martId", martId);
        result.put("templates", list);

        return result;
    }
}
