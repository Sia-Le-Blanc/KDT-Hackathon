package com.metamate.domain.login.dto;

import lombok.*;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class LoginDTO {
    private String userEmail;
    private String userPassword;

    // OTP 관련 필드 추가
    private Boolean useOtp;
    private String otpCode;

    // 암호화, 인증방식 필드 추가
    private String encryptionType;
    private String authenticationMethod;
}