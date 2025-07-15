package com.metamate.domain.login.controller;

import com.metamate.config.common.dto.ResponseDTO;
import com.metamate.config.expection.clud.FindFailedException;
import com.metamate.config.expection.clud.UpdateFailedException;
import com.metamate.config.security.TokenProvider;
import com.metamate.domain.login.dto.DeleteUserDTO;
import com.metamate.domain.login.dto.LoginDTO;
import com.metamate.domain.login.dto.LoginSelectDTO;
import com.metamate.domain.login.dto.UserDTO;
import com.metamate.domain.login.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "유저 정보를 제공하는 Controller")
public class UserController
{

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
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
            if(!userBool)
            {
                UserDTO userDTO1 = userService.UserCreate(userDTO, passwordEncoder);
                return ResponseEntity.ok().body(responseDTO.Response("sucess", "회원가입 완료", Collections.singletonList(userDTO1)));
            }
            else
            {
                logger.error("Conditions for inserting data into the database are not met.");
                throw new FindFailedException("이미 가입된 회원이 존재하는 이메일입니다.");
            }

        } catch (Exception e) {
            logger.error("Error occurred during DB processing", e);
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
                logger.warn("User is not currently logged in.");
                throw new FindFailedException("현재 인증된 회원이 없는 것 같습니다. 로그인해주세여");
            }
            if(email.equals(userDTO.getUserEmail()))
            {
                UserDTO userDTO1 = userService.UserUpdate(userDTO, passwordEncoder);
                logger.info("데이터베이스에 데이터 삽입 완료!! UserDTO = {}", userDTO);
                return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 업데이트 완료", Collections.singletonList(userDTO1)));
            }
            else
            {
                logger.error("수정 대상인 이메일 정보가 일치하지 않아요.");
                throw new FindFailedException("수정하려는 이메일 정보가 일치하지 않습니다.");
            }

        } catch (Exception e) {
            logger.error("데이터 조작 도중 예기치 못한 에러 발생: userId={}, action={}", email, userDTO, e);
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
               logger.info("로그인 정보에 따라 정상적으로 데이터를 찾았습니다 UserDTO = {}",userDTO);
               return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 조회 완료", Collections.singletonList(userDTO)));
           }
           else
           {
               logger.warn("Failed to find data due to login info.", Email);
               throw new FindFailedException("JWT 토큰이 만료된 것 같아요. 다시 로그인해주세요.");

           }
        }
        catch (Exception e)
        {
            logger.error("Failed to retrieve data due to an unexpected error.", e);
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
            logger.info("User has successfully completed login. loginDTO: {}", loginDTO);
            return ResponseEntity.ok().body(responseDTO2.Response("success", "로그인에 성공하였습니다.", Collections.singletonList(loginSelectDTO)));
        } catch (Exception e) {
            logger.error("User has NOT completed login. loginDTO: {}", loginDTO, e);
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
                    logger.info("User with email {} has been successfully deleted from the database.", Email);
                    return ResponseEntity.ok().body(responseDTO.Response("success", "회원정보 탈퇴에 성공하였습니다."));
                }
                else
                {
                    logger.warn("Failed to delete user information due to an unexpected error.");
                    throw new UpdateFailedException("회원정보 삭제 실패");
                }
            }
            else
            {
                logger.warn("Email or password provided for deletion does not match. Email: {}", Email);
                throw new FindFailedException("삭제를 위한 이메일과 비밀번호 입력 값이 다릅니다.");
            }
        }
        catch (Exception e)
        {
            logger.error("Exception occurred: {}", e.getMessage(), e);  // 예외 로그 기록
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }
}
