package com.example.pop.mapper;

import com.example.pop.vo.ProductImageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 상품 이미지 Mapper
 */
@Mapper
public interface ProductImageMapper {

    /**
     * 상품 이미지 검색
     * @param searchType 검색 타입 (NAME: 상품명, CODE: 바코드)
     * @param keyword 검색 키워드
     * @return 상품 이미지 목록
     */
    List<ProductImageVO> searchProductImages(@Param("searchType") String searchType,
                                              @Param("keyword") String keyword);

    /**
     * 상품 이미지 총 개수
     * @param searchType 검색 타입
     * @param keyword 검색 키워드
     * @return 총 개수
     */
    int countProductImages(@Param("searchType") String searchType,
                           @Param("keyword") String keyword);
}
