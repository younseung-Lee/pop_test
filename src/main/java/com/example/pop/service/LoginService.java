package com.example.pop.service;

import com.example.pop.mapper.LoginMapper;
import com.example.pop.vo.MartIpVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    
    private final LoginMapper loginMapper;
    private static final String SESSION_USER_KEY = "user";
    
    /**
     * 로그인 처리
     * @param id 사용자 아이디
     * @param pw 사용자 비밀번호
     * @param session HTTP 세션
     * @return 로그인 성공 여부
     */
    public boolean login(String id, String pw, HttpSession session) {
        log.info("로그인 시도 - ID: {}", id);
        
        // 입력값 검증
        if (!validateLoginInput(id, pw)) {
            log.warn("로그인 실패 - 입력값 검증 실패: ID={}", id);
            return false;
        }
        
        try {
            // DB에서 사용자 정보 조회
            MartIpVO user = loginMapper.findByIdAndPw(id, pw);
            
            if (user != null) {
                // 로그인 성공 - 세션에 사용자 정보 저장
                session.setAttribute(SESSION_USER_KEY, user);
                log.info("로그인 성공 - ID: {}", id);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생 - ID: {}", id, e);
            return false;
        }
    }
    
    /**
     * 로그인 입력값 검증
     * @param id 사용자 아이디
     * @param pw 사용자 비밀번호
     * @return 검증 통과 여부
     */
    private boolean validateLoginInput(String id, String pw) {
        // null 또는 빈 문자열 체크
        if (!StringUtils.hasText(id)) {
            log.warn("아이디가 비어있습니다.");
            return false;
        }
        
        if (!StringUtils.hasText(pw)) {
            log.warn("비밀번호가 비어있습니다.");
            return false;
        }
        
        return true;
    }

    
    /**
     * 로그아웃 처리
     * @param session HTTP 세션
     */
    public void logout(HttpSession session) {
        MartIpVO user = getLoginUser(session);
        if (user != null) {
            log.info("로그아웃 - ID: {}", user.getId());
        }
        session.invalidate();
    }
    
    /**
     * 로그인 여부 확인
     * @param session HTTP 세션
     * @return 로그인된 사용자 정보 (로그인되지 않았으면 null)
     */
    public MartIpVO getLoginUser(HttpSession session) {
        try {
            return (MartIpVO) session.getAttribute(SESSION_USER_KEY);
        } catch (Exception e) {
            log.error("세션에서 사용자 정보 조회 중 오류 발생", e);
            return null;
        }
    }
    
    /**
     * 로그인 여부 체크
     * @param session HTTP 세션
     * @return 로그인 여부
     */
    public boolean isLoggedIn(HttpSession session) {
        return getLoginUser(session) != null;
    }
    

}
