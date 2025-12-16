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
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TemplateController {

    private final TemplateService templateService;


    /**
     * ✅ 공통 템플릿 등록 (a4 관리자만) - 파일 업로드만
     * POST /api/templates/common (multipart/form-data)
     */
    @PostMapping(value = "/templates/common", consumes = "multipart/form-data")
    public Map<String, Object> createCommonTemplate(
            @RequestParam String templateName,
            @RequestParam String layoutType,
            @RequestParam(required = false) String useYn,
            @RequestParam(required = false) String ctgyBig,
            @RequestParam(required = false) String ctgyMid,
            @RequestParam(required = false) String ctgySml,
            @RequestParam(required = false) String ctgySub,
            @RequestParam(required = false) String tplJson,
            @RequestParam List<MultipartFile> templateImages,
            HttpSession session
    ) {
        MartIpVO user = (MartIpVO) session.getAttribute("user");
        if (user == null) throw new RuntimeException("로그인 정보가 없습니다.");
        if (!"a4".equalsIgnoreCase(user.getId())) throw new RuntimeException("권한 없음(a4만 가능)");
        if (templateImages == null || templateImages.isEmpty()) throw new RuntimeException("템플릿 이미지 파일이 없습니다.");

        int successCount = 0;

        for (MultipartFile file : templateImages) {

            PopTemplateVO vo = new PopTemplateVO();
            vo.setTplNm(templateName);
            vo.setLayoutType(layoutType);
            vo.setUseYn((useYn == null || useYn.isBlank()) ? "Y" : useYn);

            vo.setTplCtgyBig(ctgyBig);
            vo.setTplCtgyMid(ctgyMid);
            vo.setTplCtgySml(ctgySml);
            vo.setTplCtgySub(ctgySub);

            vo.setTplJson(tplJson);

            // ✅ 공통 템플릿 강제
            vo.setIsCommon("Y");
            vo.setMartCd("a4"); // NOT NULL이라 반드시 들어가야 함

            // tpl_common은 dummy → 기본값 '001' 쓰거나 명시적으로 '001'
            vo.setTplCommon("001");

            vo.setRegId("a4");
            vo.setModId("a4");

            // TODO: 실제 업로드 후 URL 저장
            // String uploadedUrl = fileUploadService.upload(file);
            // vo.setBgImgUrl(uploadedUrl);
            vo.setBgImgUrl(file.getOriginalFilename());

            int inserted = templateService.createTemplate(vo);
            if (inserted == 1) successCount++;
        }

        return Map.of(
                "success", successCount > 0,
                "message", successCount + "개의 공통 템플릿이 등록되었습니다."
        );
    }

    /**
     * ✅ 우리 매장 템플릿 저장 (캔버스 편집 후 저장)
     * - 공통 템플릿을 선택해서 편집 후 "새로 저장"하는 개념
     * - 카테고리는 이미 정해져 있으므로(=선택한 공통 템플릿에서 복사)
     *
     * POST /api/templates/my (application/json)
     */
    @PostMapping("/templates/my")
    public Map<String, Object> saveMyTemplate(
            @RequestBody PopTemplateVO vo,
            HttpSession session
    ) {
        MartIpVO user = (MartIpVO) session.getAttribute("user");
        if (user == null) throw new RuntimeException("로그인 정보가 없습니다.");

        String martCd = user.getId();

        // ✅ 사용자 입력 이름 필수
        if (vo.getTplNm() == null || vo.getTplNm().isBlank()) {
            throw new RuntimeException("템플릿 이름(tplNm)은 필수입니다.");
        }

        // ✅ 공통 템플릿에서 편집해서 저장하는 구조면 layoutType도 공통에서 따라옴. 그래도 필수 체크.
        if (vo.getLayoutType() == null || vo.getLayoutType().isBlank()) {
            throw new RuntimeException("layoutType은 필수입니다.");
        }

        // ✅ 우리매장 저장 강제
        vo.setIsCommon("N");
        vo.setMartCd(martCd);

        // tpl_common dummy
        vo.setTplCommon("001");

        if (vo.getUseYn() == null || vo.getUseYn().isBlank()) vo.setUseYn("Y");

        vo.setRegId(martCd);
        vo.setModId(martCd);

        // ✅ tpl_id는 bigint → Java에서 문자열 생성 금지!
        // tpl_id는 MyBatis INSERT에서 UUID_SHORT()로 생성하게 만들 것 (아래 Mapper 수정 필요)

        int inserted = templateService.createTemplate(vo);

        Map<String, Object> result = new HashMap<>();
        result.put("success", inserted == 1);
        result.put("martCd", martCd);
        result.put("tplSeq", vo.getTplSeq());
        return result;
    }

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
