package com.example.pop.service;

import com.example.pop.mapper.LoginMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final LoginMapper loginMapper;

    @Override
    public Map<String, Object> login(Map<String, Object> params, HttpSession session) {
        String id = (String) params.get("id");
        String pw = (String) params.get("pw");

        if (id == null || pw == null) {
            log.warn("로그인 파라미터 누락 - id: {}, pw: {}", id, pw);
            return null;
        }

        // Mapper에서 단일 사용자 Map 조회
        Map<String, Object> user = loginMapper.findByIdAndPw(params);

        if (user == null) {
            log.warn("로그인 실패 - 아이디/비밀번호 불일치, id={}", id);
            return null;
        }

        // 로그인 성공 → 세션에 저장
        session.setAttribute("loginUser", user);
        session.setAttribute("userId", user.get("user_id"));
        session.setAttribute("martCd", user.get("mart_cd"));

        log.info("로그인 성공 - id={}", id);
        return user;
    }

    @Override
    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
            log.info("로그아웃 처리 완료");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getLoginUser(HttpSession session) {
        if (session == null) return null;
        Object obj = session.getAttribute("loginUser");
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return null;
    }

    @Override
    public boolean isLoggedIn(HttpSession session) {
        return getLoginUser(session) != null;
    }
}
