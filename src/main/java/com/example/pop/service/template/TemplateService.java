package com.example.pop.service.template;

import com.example.pop.vo.PopTemplateVO;

import java.util.List;

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

}
