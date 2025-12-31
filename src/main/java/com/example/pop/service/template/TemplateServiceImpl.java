package com.example.pop.service.template;

import com.example.pop.exception.ForbiddenException;
import com.example.pop.exception.InvalidRequestException;
import com.example.pop.exception.UnauthorizedException;
import com.example.pop.mapper.PopTemplateMapper;
import com.example.pop.vo.MartIpVO;
import com.example.pop.vo.PopTemplateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final PopTemplateMapper popTemplateMapper;
    private final com.example.pop.service.file.FileStorageService fileStorageService;

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
    @Transactional
    public int createTemplate(PopTemplateVO vo) {
        //  커스텀 예외로 변경
        if (vo.getTplNm() == null || vo.getTplNm().isBlank()) {
            throw new InvalidRequestException("템플릿 이름은 필수입니다.");
        }
        
        if (vo.getLayoutType() == null || vo.getLayoutType().isBlank()) {
            throw new InvalidRequestException("레이아웃 타입은 필수입니다.");
        }

        // tpl_common dummy 기본값 보정
        if (vo.getTplCommon() == null || vo.getTplCommon().isBlank()) {
            vo.setTplCommon("001");
        }

        return popTemplateMapper.insertTemplate(vo);
    }

    @Override
    public MartIpVO validateUser(MartIpVO user) {
        if (user == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return user;
    }

    @Override
    public void validateAdminUser(MartIpVO user) {
        validateUser(user);
        if (!"a4".equalsIgnoreCase(user.getId())) {
            throw new ForbiddenException("공통 템플릿 등록 권한이 없습니다. (관리자 전용)");
        }
    }

    @Override
    @Transactional
    public Map<String, Object> createCommonTemplates(
            String templateName,
            String layoutType,
            String useYn,
            String ctgyBig,
            String tplJson,
            List<MultipartFile> templateImages,
            MartIpVO user
    ) {
        // 권한 검증
        validateAdminUser(user);

        // 파일 검증
        if (templateImages == null || templateImages.isEmpty()) {
            throw new InvalidRequestException("템플릿 이미지 파일이 필요합니다.");
        }

        int successCount = 0;
        int totalFiles = templateImages.size();

        // 다중 파일인 경우에만 인덱스 추가
        boolean isMultipleFiles = totalFiles > 1;

        for (int i = 0; i < totalFiles; i++) {
            MultipartFile file = templateImages.get(i);
            
            // 고유한 템플릿 이름 생성
            String uniqueTemplateName = generateUniqueTemplateName(
                    templateName, 
                    file, 
                    i + 1,  // 1부터 시작
                    isMultipleFiles
            );

            PopTemplateVO vo = buildCommonTemplateVO(
                    uniqueTemplateName, 
                    layoutType, 
                    useYn,
                    ctgyBig,
                    tplJson, 
                    file
            );

            int inserted = createTemplate(vo);
            if (inserted == 1) {
                successCount++;
                log.info("공통 템플릿 등록 성공: {}", uniqueTemplateName);
            }
        }

        return Map.of(
                "success", successCount > 0,
                "message", successCount + "개의 공통 템플릿이 등록되었습니다.",
                "totalFiles", totalFiles,
                "successCount", successCount
        );
    }

    @Override
    @Transactional
    public Map<String, Object> saveMyTemplate(
            String tplNm,
            String layoutType,
            String tplCtgyBig,
            String bgImgUrl,
            String tplCtgyMid,
            String tplCtgySml,
            String tplCtgySub,
            String tplJson,
            MultipartFile thumbnailImage,
            MartIpVO user
    ) {
        // 인증 검증
        validateUser(user);
        String martCd = user.getId();

        // 필수 필드 검증
        if (tplNm == null || tplNm.isBlank()) {
            throw new InvalidRequestException("템플릿 이름(tplNm)은 필수입니다.");
        }

        if (layoutType == null || layoutType.isBlank()) {
            throw new InvalidRequestException("layoutType은 필수입니다.");
        }

        if (tplCtgyBig == null || tplCtgyBig.isBlank()) {
            throw new InvalidRequestException("카테고리(대)는 필수입니다.");
        }

        // 썸네일 이미지 저장
        String thumbnailUrl = null;
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            // 이미지 파일 검증
            if (!fileStorageService.isImageFile(thumbnailImage)) {
                throw new InvalidRequestException("썸네일은 이미지 파일만 업로드 가능합니다.");
            }
            fileStorageService.validateFileSize(thumbnailImage, 10); // 10MB 제한
            
            // 파일 저장
            thumbnailUrl = fileStorageService.storeFile(thumbnailImage);
            log.info("썸네일 이미지 저장 완료: {}", thumbnailUrl);
        }

        // VO 생성
        PopTemplateVO vo = new PopTemplateVO();
        vo.setTplNm(tplNm);
        vo.setLayoutType(layoutType);
        vo.setTplCtgyBig(tplCtgyBig);
        vo.setTplCtgyMid(tplCtgyMid);
        vo.setTplCtgySml(tplCtgySml);
        vo.setTplCtgySub(tplCtgySub);
        vo.setBgImgUrl(bgImgUrl);
        vo.setThumbnailUrl(thumbnailUrl); // 썸네일 URL 설정
        vo.setTplJson(tplJson);
        vo.setIsCommon("N");
        vo.setMartCd(martCd);
        vo.setTplCommon("001");
        vo.setUseYn("Y");
        vo.setRegId(martCd);
        vo.setModId(martCd);

        int inserted = createTemplate(vo);

        Map<String, Object> result = new HashMap<>();
        result.put("success", inserted == 1);
        result.put("martCd", martCd);
        result.put("tplSeq", vo.getTplSeq());
        result.put("thumbnailUrl", thumbnailUrl);
        return result;
    }

    /**
     * 고유한 템플릿 이름 생성
     * 전략:
     * 1. 단일 파일: 원본 이름 그대로 사용
     * 2. 다중 파일: "원본이름_파일명" 또는 "원본이름_순번" 형식
     */
    private String generateUniqueTemplateName(
            String baseName, 
            MultipartFile file, 
            int index,
            boolean isMultiple
    ) {
        if (!isMultiple) {
            // 단일 파일은 원본 이름 그대로
            return baseName;
        }

        // 다중 파일의 경우
        String originalFilename = file.getOriginalFilename();
        
        if (originalFilename != null && !originalFilename.isEmpty()) {
            // 확장자 제거한 파일명 추출
            String fileNameWithoutExt = getFileNameWithoutExtension(originalFilename);
            
            // 파일명이 의미있는 경우 (숫자나 무의미한 이름이 아닌 경우)
            if (fileNameWithoutExt.length() > 2 && !isNumericOnly(fileNameWithoutExt)) {
                return baseName + "_" + fileNameWithoutExt;
            }
        }
        
        // 파일명이 없거나 무의미한 경우 순번 사용
        return baseName + "_" + index;
    }

    /**
     * 파일명에서 확장자 제거
     */
    private String getFileNameWithoutExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        return filename;
    }

    /**
     * 숫자로만 구성된 문자열인지 확인
     */
    private boolean isNumericOnly(String str) {
        return str.matches("\\d+");
    }

    /**
     * 공통 템플릿 VO 생성 헬퍼 메서드
     * 
     * 카테고리 대분류만 입력받고, 중/소/세분류는 null로 설정
     * 파일을 실제로 저장하고 URL을 설정
     */
    private PopTemplateVO buildCommonTemplateVO(
            String templateName,
            String layoutType,
            String useYn,
            String ctgyBig,
            String tplJson,
            MultipartFile file
    ) {
        // 파일 검증
        if (!fileStorageService.isImageFile(file)) {
            throw new InvalidRequestException("이미지 파일만 업로드 가능합니다.");
        }
        
        fileStorageService.validateFileSize(file, 10); // 10MB 제한

        // 파일 저장 및 URL 반환
        String fileUrl = fileStorageService.storeFile(file);
        
        PopTemplateVO vo = new PopTemplateVO();
        vo.setTplNm(templateName);
        vo.setLayoutType(layoutType);
        vo.setUseYn((useYn == null || useYn.isBlank()) ? "Y" : useYn);

        // 카테고리 대분류만 설정 (나머지는 null)
        vo.setTplCtgyBig(ctgyBig);
        vo.setTplCtgyMid(null);
        vo.setTplCtgySml(null);
        vo.setTplCtgySub(null);

        vo.setTplJson(tplJson);

        // 공통 템플릿 강제
        vo.setIsCommon("Y");
        vo.setMartCd("a4");
        vo.setTplCommon("001");

        vo.setRegId("a4");
        vo.setModId("a4");

        // 저장된 파일의 URL 경로 설정
        vo.setBgImgUrl(fileUrl);

        log.info("파일 저장 완료 - 원본: {}, URL: {}", file.getOriginalFilename(), fileUrl);

        return vo;
    }

    @Override
    public List<String> getDistinctCategories() {
        return popTemplateMapper.selectDistinctCtgyBig();
    }

    @Override
    public List<String> getDistinctCategoriesByMartCd(String martCd) {
        return popTemplateMapper.selectDistinctCtgyBigByMartCd(martCd);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteTemplate(Long tplSeq, MartIpVO user) {
        // 관리자 권한 검증
        validateAdminUser(user);

        // 템플릿 조회
        PopTemplateVO template = popTemplateMapper.selectByTplSeq(tplSeq);
        
        if (template == null) {
            throw new com.example.pop.exception.ResourceNotFoundException(
                "템플릿을 찾을 수 없습니다. (tplSeq: " + tplSeq + ")"
            );
        }

        // 파일 삭제 (실패해도 진행)
        if (template.getBgImgUrl() != null && !template.getBgImgUrl().isEmpty()) {
            boolean fileDeleted = fileStorageService.deleteFile(template.getBgImgUrl());
            if (!fileDeleted) {
                log.warn("파일 삭제 실패 (계속 진행): {}", template.getBgImgUrl());
            }
        }

        // DB에서 삭제
        int deleted = popTemplateMapper.deleteTemplate(tplSeq);

        Map<String, Object> result = new HashMap<>();
        result.put("success", deleted > 0);
        result.put("tplSeq", tplSeq);
        result.put("tplNm", template.getTplNm());
        
        if (deleted > 0) {
            log.info("템플릿 삭제 성공: tplSeq={}, tplNm={}", tplSeq, template.getTplNm());
        }
        
        return result;
    }
}
