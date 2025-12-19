package com.example.pop.service.template;

import com.example.pop.vo.MartIpVO;
import com.example.pop.vo.PopTemplateVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface TemplateService {

    /**
     * 공통 템플릿 조회 (is_common = 'Y') + 페이징
     */
    List<PopTemplateVO> getCommonTemplates(
            String layoutType,
            String ctgyBig,
            String ctgyMid,
            String ctgySml,
            String ctgySub,
            int page,
            int size
    );


    /**
     * 공통 템플릿 전체 개수
     */
    int getCommonTemplateCount(
            String layoutType,
            String ctgyBig,
            String ctgyMid,
            String ctgySml,
            String ctgySub
    );

    /**
     * 우리 마트 템플릿 조회 (is_common = 'N') + 페이징
     */
    List<PopTemplateVO> getMyTemplates(
            String martCd,
            String layoutType,
            String ctgyBig,
            String ctgyMid,
            String ctgySml,
            String ctgySub,
            int page,
            int size
    );

    /**
     * 우리 마트 템플릿 전체 개수
     */
    int getMyTemplateCount(
            String martCd,
            String layoutType,
            String ctgyBig,
            String ctgyMid,
            String ctgySml,
            String ctgySub
    );

    int createTemplate(PopTemplateVO vo);

    /**
     * 공통 템플릿 등록 (관리자 전용)
     */
    Map<String, Object> createCommonTemplates(
            String templateName,
            String layoutType,
            String useYn,
            String ctgyBig,
            String tplJson,
            List<MultipartFile> templateImages,
            MartIpVO user
    );

    /**
     * 우리 매장 템플릿 저장
     */
    Map<String, Object> saveMyTemplate(PopTemplateVO vo, MartIpVO user);

    /**
     * 세션에서 사용자 정보 확인 및 검증
     */
    MartIpVO validateUser(MartIpVO user);

    /**
     * 관리자 권한 검증
     */
    void validateAdminUser(MartIpVO user);
}
