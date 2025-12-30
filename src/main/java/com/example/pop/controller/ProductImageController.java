package com.example.pop.controller;

import com.example.pop.service.product.ProductImageService;
import com.example.pop.vo.ProductImageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 상품 이미지 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * 상품 이미지 검색
     * @param searchType 검색 타입 (NAME: 상품명, CODE: 바코드)
     * @param keyword 검색 키워드
     * @return 상품 이미지 목록 및 총 개수
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProductImages(
            @RequestParam(required = false, defaultValue = "NAME") String searchType,
            @RequestParam(required = false, defaultValue = "") String keyword) {

        log.info("상품 이미지 검색 API 호출 - searchType: {}, keyword: {}", searchType, keyword);

        Map<String, Object> result = new HashMap<>();

        try {
            // 검색 타입 검증
            if (!searchType.equals("NAME") && !searchType.equals("CODE")) {
                searchType = "NAME";
            }

            // 상품 이미지 검색
            List<ProductImageVO> productImages = productImageService.searchProductImages(searchType, keyword);
            int totalCount = productImageService.countProductImages(searchType, keyword);

            result.put("success", true);
            result.put("productImages", productImages);
            result.put("totalCount", totalCount);
            result.put("message", "검색 완료");

            log.info("검색 결과 - 총 {}건", totalCount);

        } catch (Exception e) {
            log.error("상품 이미지 검색 실패", e);
            result.put("success", false);
            result.put("message", "검색 중 오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
