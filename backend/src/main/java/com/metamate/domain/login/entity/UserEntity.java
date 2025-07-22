package com.metamate.domain.login.entity;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PUBLIC)  // 생성자 접근 수준을 PUBLIC으로 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@EqualsAndHashCode
public class UserEntity
{
    private Long userId;
    private Long companyId;
    private String userName;
    private Integer userAge;
    private String userEmail;
    private String userPassword;
    private String userRole;
    private String position;
    private String region;
    private LocalDateTime createdAt;

    // otp관련 필드 추가
    private Boolean useOtp;
    private String otpSecret;
    private Boolean allowAutoReconnect;

    // 세션/토큰 관리 필드 추가
    private String currentTokenId;
    private LocalDateTime lastLoginAt;
    private LocalDateTime tokenIssuedAt;

    // 사용자 상태 관리 필드 추가
    private Boolean isActive;
    private Boolean isDeleted;

    // OAuth2 관련 관리 필드 추가
    private String provider;   // "kakao", "google", "naver" 등
    private String socialId;   // SNS 플랫폼에서 제공하는 고유 ID (email 말고 "id" 값 등)
}
