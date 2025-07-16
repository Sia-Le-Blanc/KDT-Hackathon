package com.metamate.domain.login.mapper;

import com.metamate.domain.login.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper
{

    UserEntity selectUserByUserEmail(@Param("userEmail") String userEmail);//처리 완료
    List<UserEntity> selectAllUsers();
    void insertUser(UserEntity user);//처리 완료
    void  updateUserInfo(UserEntity userEntity);//처리완료
    void deleteUser(@Param("userId") Long userId);
    Long findByEmailAndPassword(@Param("userEmail") String userEmail, @Param("userPassword") String Password);
    int existsByEmail(@Param("userEmail") String userEmail);

    // 토큰 관리 메서드 추가
    void updateUserToken(@Param("userId") Long userId, @Param("tokenId") String tokenId, @Param("issuedAt")LocalDateTime issuedAt);
    void invalidateUserToken(@Param("userId") Long userId);
    String getCurrentTokenByUserId(@Param("userId") Long userId);

    // 사용자 상태 관리 메서드 추가
    void updateUserActiveStatus(@Param("userId") Long userId, @Param("isActive") Boolean isActive);
    void markUserAsDeleted(@Param("userId") Long userId);
    Boolean isUserActiveAndNotDeleted(@Param("userEmail") String userEmail);

    // OTP 관련 메서드 추가
    void updateOtpSettings(@Param("userId") Long userId, @Param("userOtp") Boolean userOtp, @Param("otpSecret") String otpSecret);
}
