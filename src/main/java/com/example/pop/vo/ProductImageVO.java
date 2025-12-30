package com.example.pop.vo;

import lombok.Data;

/**
 * 상품 이미지 VO
 */
@Data
public class ProductImageVO {
    private Integer seqMstrPrdt;           // 이미지 상품 마스터 원장 시퀀스
    private String mstrPrdtJum;            // 더미 컬럼
    private String mstrPrdtCd;             // 상품 바코드
    private String mstrPrdtNm;             // 원장 상품명
    private String mstrPrdtSize;           // 원장 상품 사이즈
    private String mstrPrdtCtgyFrst;       // 원장 상품 대분류 카테고리 코드
    private String mstrPrdtCtgyFrstNm;     // 원장 상품 대분류 카테고리 명
    private String mstrPrdtCtgyScnd;       // 원장 상품 중분류 카테고리 코드
    private String mstrPrdtCtgyScndNm;     // 원장 상품 중분류 카테고리 명
    private String mstrPrdtCtgyThrd;       // 원장 상품 소분류 카테고리 코드
    private String mstrPrdtCtgyThrdNm;     // 원장 상품 소분류 카테고리 명
    private String mstrPrdtMUrl;           // 원장 상품 이미지 url 모바일용
    private String mstrPrdtPUrl;           // 원장 상품 이미지 url 포스용
    private String mstrPrdtWUrl;           // 원장 상품 이미지 url 웹용
    private String mstrPrdtUseyn;          // 원장 상품 이미지 사용여부
    private String mstrPrdtRgstDt;         // 원장 상품 이미지 등록일시
}
