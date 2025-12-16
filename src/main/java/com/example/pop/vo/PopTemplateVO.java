package com.example.pop.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PopTemplateVO {
    private Long tplSeq;
    private String tplCommon;
    private String martCd;
    private String tplId;
    private String tplNm;
    private String tplCtgyBig;
    private String tplCtgyMid;
    private String tplCtgySml;
    private String tplCtgySub;
    private String layoutType;
    private String bgImgUrl;
    private String tplJson;
    private String isCommon;
    private String useYn;
    private String regId;
    private LocalDateTime regDt;
    private String modId;
    private LocalDateTime modDt;
}
