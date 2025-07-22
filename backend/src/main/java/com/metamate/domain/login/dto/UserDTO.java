package com.metamate.domain.login.dto;

import com.metamate.config.common.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDTO {

    @Schema(description = "사용자 ID (PK)", example = "123")
    private Long userId;

    @Schema(description = "회사 ID (FK)", example = "10")
    private Long companyId;

    @NotBlank(message = "UserName cannot be blank")
    @Size(min = 2, max = 10, message = "이름은 2~10자 사이여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글 또는 영문만 포함할 수 있습니다.")
    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;

    @NotNull(message = "UserAge cannot be null")
    @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
    @Max(value = 150, message = "나이는 150 이하이어야 합니다.")
    @Schema(description = "사용자 나이", example = "28")
    private Integer userAge;

    @NotBlank(message = "UserEmail cannot be blank")
    @Email(message = "Email 형식이 올바르지 않습니다.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "올바른 이메일 형식이어야 합니다.")
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String userEmail;

    // ↓ 일반 로그인 시에만 필수, 소셜은 비어있을 수 있음
    @Size(min = 8, max = 255, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    @Schema(description = "사용자 비밀번호 (해시 처리됨)", example = "Abc12345!")
    private String userPassword;

    @NotNull(message = "UserRole cannot be null")
    @Schema(description = "사용자 역할", example = "admin")
    private UserRole userRole;

    @Pattern(regexp = "^[가-힣a-zA-Z\\s]{0,50}$", message = "직책은 한글, 영문, 공백만 허용되며 50자 이하여야 합니다.")
    @Schema(description = "직책", example = "팀장")
    private String position;

    @Pattern(regexp = "^[가-힣a-zA-Z\\s]{0,50}$", message = "지역은 한글, 영문, 공백만 허용되며 50자 이하여야 합니다.")
    @Schema(description = "지역", example = "서울")
    private String region;

    @Schema(description = "생성일시", example = "2025-07-08T11:00:00")
    private LocalDateTime createdAt;

    // ✅ OTP 관련
    @Schema(description = "OTP 사용 여부", example = "false")
    private Boolean useOtp;

    @Schema(description = "OTP 비밀키", example = "JBSWY3DPEHPK3PXP")
    private String otpSecret;

    @Schema(description = "자동 재접속 허용 여부", example = "true")
    private Boolean allowAutoReconnect;

    // ✅ 상태 관련
    @Schema(description = "계정 활성 상태", example = "true")
    private Boolean isActive;

    @Schema(description = "삭제 상태", example = "false")
    private Boolean isDeleted;

    @Schema(description = "마지막 로그인 시간")
    private LocalDateTime lastLoginAt;

    // ✅ [OAuth2 추가 필드]
    @Schema(description = "OAuth2 제공자 (google, kakao, naver 등)", example = "kakao")
    private String provider;

    @Schema(description = "OAuth2 사용자 고유 식별자", example = "109283472012948")
    private String socialId;
}
