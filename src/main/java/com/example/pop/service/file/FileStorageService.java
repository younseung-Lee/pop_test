package com.example.pop.service.file;

import com.example.pop.config.FileUploadConfig;
import com.example.pop.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileUploadConfig fileUploadConfig;

    /**
     * 파일 저장 및 URL 반환
     * 
     * @param file 업로드 파일
     * @return 저장된 파일의 URL 경로
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("파일이 비어있습니다.");
        }

        // 원본 파일명
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new FileUploadException("파일명이 유효하지 않습니다.");
        }

        // 파일 확장자 추출
        String extension = getFileExtension(originalFilename);
        
        // 고유한 파일명 생성: 날짜_UUID_원본파일명
        String uniqueFilename = generateUniqueFilename(originalFilename);

        try {
            // 저장 경로
            Path uploadPath = Paths.get(fileUploadConfig.getUploadDir());
            
            // 디렉토리가 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일 저장
            Path targetPath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 완료: {} -> {}", originalFilename, targetPath.toString());


            return fileUploadConfig.getUrlPath() + "/" + uniqueFilename;

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", originalFilename, e);
            throw new FileUploadException("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 삭제
     * 
     * @param fileUrl 삭제할 파일의 URL 경로
     * @return 삭제 성공 여부
     */
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            // URL에서 파일명 추출 (예: /uploads/templates/filename.jpg -> filename.jpg)
            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            
            Path filePath = Paths.get(fileUploadConfig.getUploadDir(), filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("파일 삭제 완료: {}", filePath);
                return true;
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", filePath);
                return false;
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * 고유한 파일명 생성
     * 형식: 날짜(yyyyMMdd)_UUID_원본파일명
     */
    private String generateUniqueFilename(String originalFilename) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        // 파일명에서 특수문자 제거 (공백을 언더스코어로)
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9가-힣._-]", "_");
        
        return datePrefix + "_" + uuid + "_" + sanitizedFilename;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 이미지 파일 여부 확인
     */
    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        return contentType.startsWith("image/");
    }

    /**
     * 파일 크기 검증 (10MB)
     */
    public void validateFileSize(MultipartFile file, long maxSizeInMB) {
        long maxSizeInBytes = maxSizeInMB * 1024 * 1024;
        
        if (file.getSize() > maxSizeInBytes) {
            throw new FileUploadException(
                String.format("파일 크기가 너무 큽니다. (최대: %dMB, 현재: %.2fMB)", 
                    maxSizeInMB, 
                    file.getSize() / (1024.0 * 1024.0))
            );
        }
    }
}
