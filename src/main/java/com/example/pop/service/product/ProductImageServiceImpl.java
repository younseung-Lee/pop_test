package com.example.pop.service.product;

import com.example.pop.mapper.ProductImageMapper;
import com.example.pop.vo.ProductImageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 상품 이미지 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageMapper productImageMapper;

    @Override
    public List<ProductImageVO> searchProductImages(String searchType, String keyword) {
        log.info("상품 이미지 검색 - searchType: {}, keyword: {}", searchType, keyword);
        return productImageMapper.searchProductImages(searchType, keyword);
    }

    @Override
    public int countProductImages(String searchType, String keyword) {
        log.info("상품 이미지 개수 조회 - searchType: {}, keyword: {}", searchType, keyword);
        return productImageMapper.countProductImages(searchType, keyword);
    }
}
