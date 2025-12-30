package com.example.pop.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 이미지 프록시 Controller
 * 외부 이미지 URL의 CORS 문제를 해결하기 위한 프록시
 */
@Slf4j
@RestController
@RequestMapping("/api/image-proxy")
public class ImageProxyController {

    /**
     * 외부 이미지 프록시
     * @param url 이미지 URL
     * @return 이미지 바이트 데이터
     */
    @GetMapping
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        log.info("이미지 프록시 요청: {}", url);

        try {
            // URL 연결
            URL imageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            // User-Agent 설정 (서버가 봇 요청을 차단하지 않도록, 프록시 요청을 ‘일반 브라우저처럼 보이게 만드는 설정)
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("이미지 로드 실패: HTTP {}", responseCode);
                return ResponseEntity.status(responseCode).build();
            }

            // Content-Type 가져오기
            String contentType = connection.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("이미지가 아닌 타입: {}", contentType);
                contentType = "image/jpeg"; // 기본값
            }

            // 이미지 데이터 읽기
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] imageBytes = inputStream.readAllBytes();

                // 헤더 설정
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setCacheControl("public, max-age=86400"); // 1일 캐싱
                headers.set("Access-Control-Allow-Origin", "*"); // CORS 허용

                log.info("이미지 프록시 성공: {} bytes, {}", imageBytes.length, contentType);
                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            }

        } catch (IOException e) {
            log.error("이미지 프록시 실패: {}", url, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
