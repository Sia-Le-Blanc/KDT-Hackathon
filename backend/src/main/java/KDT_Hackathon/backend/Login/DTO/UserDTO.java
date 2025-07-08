package KDT_Hackathon.backend.Login.DTO;

import KDT_Hackathon.backend.Config.CommonType.UserRole;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDTO {

    @NotNull(message = "UserId cannot be null")
    @Schema(description = "사용자 ID (PK)", example = "123")
    private Long userId;

    @NotNull(message = "CompanyId cannot be null")
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

    @NotBlank(message = "UserPassword cannot be blank")
    @Size(min = 8, max = 255, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    @Schema(description = "사용자 비밀번호 (해시 처리됨)", example = "Abc12345!")
    private String userPassword;

    @NotBlank(message = "UserRole cannot be blank")
    @Schema(description = "사용자 역할", example = "admin")
    private UserRole userRole;

    @Pattern(regexp = "^[가-힣a-zA-Z\\s]{0,50}$", message = "직책은 한글, 영문, 공백만 허용되며 50자 이하여야 합니다.")
    @Schema(description = "직책", example = "팀장")
    private String position;

    @Pattern(regexp = "^[가-힣a-zA-Z\\s]{0,50}$", message = "지역은 한글, 영문, 공백만 허용되며 50자 이하여야 합니다.")
    @Schema(description = "지역", example = "서울")
    private String region;

    @NotNull(message = "CreatedAt cannot be null")
    @Schema(description = "생성일시", example = "2025-07-08T11:00:00")
    private LocalDateTime createdAt;
}
