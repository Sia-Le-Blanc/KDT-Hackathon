package KDT_Hackathon.backend.Login.DTO;

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
}