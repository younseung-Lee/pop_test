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

    /**
     * 공통 템플릿 조회 (is_common = 'Y')
     * ex) GET /api/templates/common?layoutType=VERTICAL&ctgyBig=EVT&page=1&size=20
     */
    @GetMapping("/templates/common")
    public Map<String, Object> getCommonTemplates(
            @RequestParam(required = false) String layoutType,
            @RequestParam(required = false, name = "ctgyBig") String ctgyBig,
            @RequestParam(required = false, name = "ctgyMid") String ctgyMid,
            @RequestParam(required = false, name = "ctgySml") String ctgySml,
            @RequestParam(required = false, name = "ctgySub") String ctgySub,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("공통 템플릿 조회 요청 - layoutType: {}, ctgyBig: {}, ctgyMid: {}, ctgySml: {}, ctgySub: {}, page: {}, size: {}",
                layoutType, ctgyBig, ctgyMid, ctgySml, ctgySub, page, size);

        List<PopTemplateVO> list = templateService.getCommonTemplates(
                layoutType, ctgyBig, ctgyMid, ctgySml, ctgySub, page, size);
        int totalCount = templateService.getCommonTemplateCount(
                layoutType, ctgyBig, ctgyMid, ctgySml, ctgySub);

        Map<String, Object> result = new HashMap<>();
        result.put("page", page);
        result.put("size", size);
        result.put("totalCount", totalCount);
        result.put("templates", list);

        return result;
    }

    /**
     * 우리 마트 템플릿 조회 (is_common = 'N' AND mart_cd = 세션 mart)
     */
    @GetMapping("/templates/my")
    public Map<String, Object> getMyTemplates(
            HttpSession session,
            @RequestParam(required = false) String layoutType,
            @RequestParam(required = false, name = "ctgyBig") String ctgyBig,
            @RequestParam(required = false, name = "ctgyMid") String ctgyMid,
            @RequestParam(required = false, name = "ctgySml") String ctgySml,
            @RequestParam(required = false, name = "ctgySub") String ctgySub,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        MartIpVO user = (MartIpVO) session.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("로그인 정보가 없습니다.");
        }

        // MartIpVO 에서 마트 코드(ID) 추출
        String martCd = user.getId();

        log.info("우리 템플릿 조회 요청 - martCd: {}, layoutType: {}, ctgyBig: {}, ctgyMid: {}, ctgySml: {}, ctgySub: {}, page: {}, size: {}",
                martCd, layoutType, ctgyBig, ctgyMid, ctgySml, ctgySub, page, size);

        List<PopTemplateVO> list = templateService.getMyTemplates(
                martCd, layoutType, ctgyBig, ctgyMid, ctgySml, ctgySub, page, size);
        int totalCount = templateService.getMyTemplateCount(
                martCd, layoutType, ctgyBig, ctgyMid, ctgySml, ctgySub);

        Map<String, Object> result = new HashMap<>();
        result.put("martCd", martCd);
        result.put("page", page);
        result.put("size", size);
        result.put("totalCount", totalCount);
        result.put("templates", list);

        return result;
    }
}
