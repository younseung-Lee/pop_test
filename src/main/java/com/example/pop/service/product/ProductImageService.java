package com.example.pop.service.product;

import com.example.pop.vo.ProductImageVO;

import java.util.List;

/**
 * 상품 이미지 Service 인터페이스
 */
public interface ProductImageService {

    /**
     * 상품 이미지 검색
     * @param searchType 검색 타입 (NAME: 상품명, CODE: 바코드)
     * @param keyword 검색 키워드
     * @return 상품 이미지 목록
     */
    List<ProductImageVO> searchProductImages(String searchType, String keyword);

    /**
     * 상품 이미지 총 개수
     * @param searchType 검색 타입
     * @param keyword 검색 키워드
     * @return 총 개수
     */
    int countProductImages(String searchType, String keyword);
}
