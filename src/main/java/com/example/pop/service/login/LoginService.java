package com.example.pop.service.login;

import com.example.pop.vo.MartIpVO;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public interface LoginService {

    /**
     * 로그인 처리
     *  - id, password로 사용자 조회
     *  - 성공 시 세션에 사용자 정보 저장
     *  - 실패 시 null 반환
     */
    MartIpVO login(Map<String, Object> params, HttpSession session);

    /** 로그아웃 (세션 종료) */
    void logout(HttpSession session);

    /** 세션에서 로그인 사용자 정보 조회 */
    MartIpVO getLoginUser(HttpSession session);

    /** 로그인 여부 확인 */
    boolean isLoggedIn(HttpSession session);
}
