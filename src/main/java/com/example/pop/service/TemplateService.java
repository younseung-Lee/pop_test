package com.example.pop.service;

import com.example.pop.vo.PopTemplateVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TemplateService {

    List<PopTemplateVO> getTemplates(String layoutType,String category);

    /**
     * 템플릿 전체 개수 (필터 포함)
     */
    int getTemplateCount(String layoutType, String category);

    /**
     * 최근 사용 템플릿 조회
     *
     * @param martId 마트 아이디 (MartIpVO.id)
     * @param limit  최대 개수
     */
    List<PopTemplateVO> getRecentTemplates(String martId, int limit);
}
