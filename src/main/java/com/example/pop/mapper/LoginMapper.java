package com.example.pop.mapper;

import com.example.pop.vo.MartIpVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginMapper {
    
    /**
     * 아이디와 비밀번호로 사용자 조회
     * @param id 사용자 아이디
     * @param pw 사용자 비밀번호
     * @return 사용자 정보 (없으면 null)
     */
    MartIpVO findByIdAndPw(@Param("id") String id, @Param("pw") String pw);
    
    /**
     * 아이디로 사용자 조회
     * @param id 사용자 아이디
     * @return 사용자 정보 (없으면 null)
     */
    MartIpVO findById(@Param("id") String id);
    
    /**
     * 사용자 존재 여부 확인
     * @param id 사용자 아이디
     * @return 존재 여부 (1: 존재, 0: 없음)
     */
    int existsById(@Param("id") String id);
}
