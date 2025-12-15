package com.example.pop.service.login;

import com.example.pop.mapper.LoginMapper;
import com.example.pop.vo.MartIpVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final LoginMapper loginMapper;

    @Override
    public MartIpVO login(Map<String, Object> params, HttpSession session) {

        String id = (String) params.get("id");
        String pw = (String) params.get("pw");

        if (id == null || pw == null) {
            log.warn("로그인 파라미터 누락");
            return null;
        }

        MartIpVO user = loginMapper.findByIdAndPw(params);

        if (user == null) {
            log.warn("로그인 실패 - id={}", id);
            return null;
        }

        // ✅ 세션에는 무조건 "user" 키로 MartIpVO 저장
        session.setAttribute("user", user);

        log.info("로그인 성공 - martCd={}, userNm={}", user.getId(), user.getNm());
        return user;
    }

    @Override
    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public MartIpVO getLoginUser(HttpSession session) {
        Object obj = session.getAttribute("user");
        return (obj instanceof MartIpVO) ? (MartIpVO) obj : null;
    }

    @Override
    public boolean isLoggedIn(HttpSession session) {
        return getLoginUser(session) != null;
    }
}
