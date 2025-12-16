package com.example.pop.mapper;

import com.example.pop.vo.PopTemplateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PopTemplateMapper {

    /**
     * 공통 템플릿 리스트 조회 (is_common = 'Y')
     */
    List<PopTemplateVO> selectCommonTemplates(
            @Param("layoutType") String layoutType,
            @Param("ctgyBig") String ctgyBig,
            @Param("ctgyMid") String ctgyMid,
            @Param("ctgySml") String ctgySml,
            @Param("ctgySub") String ctgySub,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    /**
     * 공통 템플릿 전체 개수
     */
    int countCommonTemplates(
            @Param("layoutType") String layoutType,
            @Param("ctgyBig") String ctgyBig,
            @Param("ctgyMid") String ctgyMid,
            @Param("ctgySml") String ctgySml,
            @Param("ctgySub") String ctgySub
    );

    /**
     * 우리 마트 템플릿 리스트 조회 (is_common = 'N' AND mart_cd = ?)
     */
    List<PopTemplateVO> selectMyTemplates(
            @Param("martCd") String martCd,
            @Param("layoutType") String layoutType,
            @Param("ctgyBig") String ctgyBig,
            @Param("ctgyMid") String ctgyMid,
            @Param("ctgySml") String ctgySml,
            @Param("ctgySub") String ctgySub,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    /**
     * 우리 마트 템플릿 전체 개수
     */
    int countMyTemplates(
            @Param("martCd") String martCd,
            @Param("layoutType") String layoutType,
            @Param("ctgyBig") String ctgyBig,
            @Param("ctgyMid") String ctgyMid,
            @Param("ctgySml") String ctgySml,
            @Param("ctgySub") String ctgySub
    );

    int insertTemplate(PopTemplateVO vo);
}
