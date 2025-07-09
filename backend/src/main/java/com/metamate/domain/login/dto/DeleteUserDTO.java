package KDT_Hackathon.backend.Login.DTO;

import lombok.*;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class DeleteUserDTO
{
    private String userEmail;
    private String userPassword;
    private String userPasswordVal;
}
