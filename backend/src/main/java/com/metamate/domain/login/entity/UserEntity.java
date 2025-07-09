package KDT_Hackathon.backend.Login.Entity;

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
}
