package com.example.pop.vo;

import lombok.Data;

@Data
public class PopTemplateVO {
    private Long templateId;
    private String templateName;
    private String templateImage;
    private String layoutType;   // VERTICAL(세로) / HORIZONTAL(가로) / SHOWCARD
    private String category;
    private String useYn;
}
