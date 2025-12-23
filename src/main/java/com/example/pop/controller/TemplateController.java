package com.example.pop.controller;

import com.example.pop.service.template.TemplateService;
import com.example.pop.vo.MartIpVO;
import com.example.pop.vo.PopTemplateVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * 공통 템플릿 등록 (a4 관리자만) - 파일 업로드만
     * POST /api/templates/common (multipart/form-data)
     */
    @PostMapping(value = "/templates/common", consumes = "multipart/form-data")
    public Map<String, Object> createCommonTemplate(
            @RequestParam String templateName,
            @RequestParam String layoutType,
            @RequestParam(required = false) String useYn,
            @RequestParam(required = false) String ctgyBig,
            @RequestParam(required = false) String tplJson,
            @RequestParam List<MultipartFile> templateImages,
            HttpSession session
    ) {
        MartIpVO user = (MartIpVO) session.getAttribute("user");
        
        return templateService.createCommonTemplates(
                templateName, layoutType, useYn,
                ctgyBig, tplJson, templateImages, user
        );
    }

    /**
     * 우리 매장 템플릿 저장 (캔버스 편집 후 저장)
     * POST /api/templates/my (application/json)
     */
    @PostMapping("/templates/my")
    public Map<String, Object> saveMyTemplate(
            @RequestBody PopTemplateVO vo,
            HttpSession session
    ) {
        MartIpVO user = (MartIpVO) session.getAttribute("user");
        return templateService.saveMyTemplate(vo, user);
    }

    /**
     * 공통 템플릿 조회 (is_common = 'Y')
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
        templateService.validateUser(user);
        
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

    /**
     * 공통 템플릿의 고유 카테고리 대분류 목록 조회
     * GET /api/templates/categories
     */
    @GetMapping("/templates/categories")
    public Map<String, Object> getCategories() {
        List<String> categories = templateService.getDistinctCategories();
        return Map.of(
                "success", true,
                "categories", categories
        );
    }

    /**
     * 우리 마트 템플릿의 고유 카테고리 대분류 목록 조회
     * GET /api/templates/my/categories
     */
    @GetMapping("/templates/my/categories")
    public Map<String, Object> getMyCategoriesByMartCd(HttpSession session) {
        MartIpVO user = (MartIpVO) session.getAttribute("user");
        templateService.validateUser(user);
        
        String martCd = user.getId();
        List<String> categories = templateService.getDistinctCategoriesByMartCd(martCd);
        
        return Map.of(
                "success", true,
                "martCd", martCd,
                "categories", categories
        );
    }
}
