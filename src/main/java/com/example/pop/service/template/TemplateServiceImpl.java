package com.example.pop.service.template;

import com.example.pop.mapper.PopTemplateMapper;
import com.example.pop.vo.PopTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final PopTemplateMapper popTemplateMapper;

    private int calcOffset(int page, int size) {
        int safePage = (page <= 0) ? 1 : page;
        int safeSize = (size <= 0) ? 20 : size;
        return (safePage - 1) * safeSize;
    }

    private int calcPageSize(int size) {
        return (size <= 0) ? 20 : size;
    }

    @Override
    public List<PopTemplateVO> getCommonTemplates(String layoutType,
                                                  String ctgyBig,
                                                  String ctgyMid,
                                                  String ctgySml,
                                                  String ctgySub,
                                                  int page,
                                                  int size) {

        int offset = calcOffset(page, size);
        int pageSize = calcPageSize(size);

        return popTemplateMapper.selectCommonTemplates(
                layoutType,
                ctgyBig,
                ctgyMid,
                ctgySml,
                ctgySub,
                offset,
                pageSize
        );
    }

    @Override
    public int getCommonTemplateCount(String layoutType,
                                      String ctgyBig,
                                      String ctgyMid,
                                      String ctgySml,
                                      String ctgySub) {

        return popTemplateMapper.countCommonTemplates(
                layoutType,
                ctgyBig,
                ctgyMid,
                ctgySml,
                ctgySub
        );
    }

    @Override
    public List<PopTemplateVO> getMyTemplates(String martCd,
                                              String layoutType,
                                              String ctgyBig,
                                              String ctgyMid,
                                              String ctgySml,
                                              String ctgySub,
                                              int page,
                                              int size) {

        int offset = calcOffset(page, size);
        int pageSize = calcPageSize(size);

        return popTemplateMapper.selectMyTemplates(
                martCd,
                layoutType,
                ctgyBig,
                ctgyMid,
                ctgySml,
                ctgySub,
                offset,
                pageSize
        );
    }

    @Override
    public int getMyTemplateCount(String martCd,
                                  String layoutType,
                                  String ctgyBig,
                                  String ctgyMid,
                                  String ctgySml,
                                  String ctgySub) {

        return popTemplateMapper.countMyTemplates(
                martCd,
                layoutType,
                ctgyBig,
                ctgyMid,
                ctgySml,
                ctgySub
        );
    }

    @Override
    public int createTemplate(PopTemplateVO vo) {
        if (vo.getTplNm() == null || vo.getTplNm().isBlank()) throw new RuntimeException("템플릿 이름 필수");
        if (vo.getLayoutType() == null || vo.getLayoutType().isBlank()) throw new RuntimeException("layoutType 필수");

        return popTemplateMapper.insertTemplate(vo);
    }
}

