package com.example.pop.mapper;

import com.example.pop.vo.MartIpVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface LoginMapper {

    /**
     * 아이디와 비밀번호로 사용자 조회
     *  - 없으면 null
     */
    Map<String, Object> findByIdAndPw(Map<String, Object> params);

    /**
     * 아이디로 마트 IP 조회 (필요 시 사용)
     */
    MartIpVO findById(@Param("id") String id);

    /**
     * 사용자 존재 여부 확인 (필요 시 사용)
     */
    int existsById(@Param("id") String id);
}
