package com.example.pop.mapper;

import com.example.pop.vo.PopTemplateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PopTemplateMapper {

    // 템플릿 리스트 조회 (전체 / 레이아웃 / 카테고리 필터)
    List<PopTemplateVO> selectTemplates(
            @Param("layoutType") String layoutType,
            @Param("category") String category
    );

    // 전체 개수 (paging이나 화면에 총 개수 보여줄 때)
    int countTemplates(
            @Param("layoutType") String layoutType,
            @Param("category") String category
    );

    // 최근 사용 템플릿 조회
    List<PopTemplateVO> selectRecentTemplates(
            @Param("martId") String martId,
            @Param("limit") int limit
    );
}
