package com.example.pop.service;

import com.example.pop.mapper.PopTemplateMapper;
import com.example.pop.vo.PopTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final PopTemplateMapper popTemplateMapper;

    @Override
    public List<PopTemplateVO> getTemplates(String layoutType, String category) {
        return popTemplateMapper.selectTemplates(layoutType, category);
    }

    @Override
    public int getTemplateCount(String layoutType, String category) {
        return popTemplateMapper.countTemplates(layoutType, category);
    }

    @Override
    public List<PopTemplateVO> getRecentTemplates(String martId, int limit) {
        return popTemplateMapper.selectRecentTemplates(martId, limit);
    }
}
