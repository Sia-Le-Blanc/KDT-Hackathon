package KDT_Hackathon.backend.Login.Controller;

import KDT_Hackathon.backend.Config.CommonType.DTO.ResponseDTO;
import KDT_Hackathon.backend.Config.Expection.CLUD.FindFailedException;
import KDT_Hackathon.backend.Config.Expection.CLUD.UpdateFailedException;
import KDT_Hackathon.backend.Config.Security.TokenProvider;
import KDT_Hackathon.backend.Login.DTO.DeleteUserDTO;
import KDT_Hackathon.backend.Login.DTO.LoginDTO;
import KDT_Hackathon.backend.Login.DTO.LoginSelectDTO;
import KDT_Hackathon.backend.Login.DTO.UserDTO;
import KDT_Hackathon.backend.Login.Service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "유저 정보를 제공하는 Controller")
public class UserController
{

    private final ResponseDTO<UserDTO> responseDTO = new ResponseDTO<>();

    @Autowired
    UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;


    @PostMapping
    public ResponseEntity<?> UserInsert(@RequestBody @Valid UserDTO userDTO)
    {
        try
        {
            Boolean userBool = userService.getUserID(userDTO.getUserEmail());
            if(userBool != true)
            {
                UserDTO userDTO1 = userService.UserCreate(userDTO, passwordEncoder);
                return ResponseEntity.ok().body(responseDTO.Response("sucess", "회원가입 완료", Collections.singletonList(userDTO1)));
            }
            else
            {
                throw new FindFailedException("이미 가입된 회원이 존재하는 이메일입니다.");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> UserUpdate(@AuthenticationPrincipal String email, @RequestBody @Valid UserDTO userDTO)
    {
        try
        {
            if (email == null)
            {
                throw new FindFailedException("현재 인증된 회원이 없는 것 같습니다. 로그인해주세여");
            }
            if(email.equals(userDTO.getUserEmail()))
            {
                UserDTO userDTO1 = userService.UserUpdate(userDTO, passwordEncoder);
                return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 업데이트 완료", Collections.singletonList(userDTO1)));
            }
            else
            {
                throw new FindFailedException("수정하려는 이메일 정보가 일치하지 않습니다.");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }


    @GetMapping("/search")
    public ResponseEntity<?> UserSelect(@AuthenticationPrincipal String Email)
    {
        try
        {
           if(Email != null)
           {
               UserDTO userDTO = userService.UserSelect(Email);
               return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 조회 완료", Collections.singletonList(userDTO)));
           }
           else
           {
               throw new FindFailedException("로그인 정보가 일치하지 않습니다.");
           }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> UserLogin(@RequestBody @Valid LoginDTO loginDTO)
    {
        try
        {
            UserDTO userDTO = userService.UserLogin(loginDTO, passwordEncoder); //로그인 가능 여부 확인
            String token = tokenProvider.createToken(userDTO);
            LoginSelectDTO loginSelectDTO = userService.tokenSelect(userDTO, token);
            ResponseDTO<LoginSelectDTO> responseDTO2 = new ResponseDTO<>();
            return ResponseEntity.ok().body(responseDTO2.Response("success", "로그인에 성공하였습니다.", Collections.singletonList(loginSelectDTO)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> UserDelete(@AuthenticationPrincipal String Email, @RequestBody @Valid DeleteUserDTO deleteUserDTO)
    {
        try
        {
            if(Email == deleteUserDTO.getUserEmail() && deleteUserDTO.getUserPassword().equals(deleteUserDTO.getUserPasswordVal()))
            {
                Boolean aBoolean = userService.UserDelete(deleteUserDTO.getUserEmail(), deleteUserDTO.getUserPassword(), passwordEncoder);
                if(aBoolean == true)
                {
                    return ResponseEntity.ok().body(responseDTO.Response("success", "회원정보 탈퇴에 성공하였습니다."));
                }
                else
                {
                    throw new UpdateFailedException("회원정보 삭제 실패");
                }
            }
            else
            {
                throw new FindFailedException("삭제를 위한 이메일과 비밀번호 입력 값이 다릅니다.");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }
}
