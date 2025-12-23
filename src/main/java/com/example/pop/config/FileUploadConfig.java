package com.example.pop.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
@Getter
public class FileUploadConfig {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.url-path}")
    private String urlPath;

    /**
     * 애플리케이션 시작 시 업로드 디렉토리 생성
     */
    @PostConstruct
    public void init() {
        File uploadDirectory = new File(uploadDir);
        
        if (!uploadDirectory.exists()) {
            boolean created = uploadDirectory.mkdirs();
            if (created) {
                System.out.println("✅ 파일 업로드 디렉토리 생성 완료: " + uploadDirectory.getAbsolutePath());
            } else {
                System.err.println("❌ 파일 업로드 디렉토리 생성 실패: " + uploadDirectory.getAbsolutePath());
            }
        } else {
            System.out.println("✅ 파일 업로드 디렉토리 존재: " + uploadDirectory.getAbsolutePath());
        }
    }

    /**
     * 파일의 절대 경로 반환
     */
    public String getAbsolutePath() {
        return new File(uploadDir).getAbsolutePath();
    }
}
