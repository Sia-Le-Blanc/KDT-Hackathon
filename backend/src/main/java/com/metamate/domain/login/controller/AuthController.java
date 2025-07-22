package com.metamate.domain.login.controller;

import com.metamate.config.common.dto.ResponseDTO;
import com.metamate.config.security.JwtAuthenticationFilter;
import com.metamate.config.security.TokenProvider;
import com.metamate.domain.login.dto.OAuth2Service;
import com.metamate.domain.login.dto.UserDTO;
import com.metamate.domain.login.service.OAuth2ServiceFactory;
import com.metamate.domain.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private TokenProvider tokenProvider;


    private ResponseDTO responseDTO = new ResponseDTO();

    @Autowired
    private UserService userService;

    @Autowired
    private OAuth2ServiceFactory oAuth2ServiceFactory;

    @GetMapping("/api/auth/sign/{oauthType}")
    public ResponseEntity<?> oauthSignUP(@RequestParam("code") String code, @PathVariable String oauthType) {
        try {
            OAuth2Service service = oAuth2ServiceFactory.getService(oauthType);
            //Path 값에 따라 어떤 Service로 넘길 것인지 선택.

            if (service == null) {
                return ResponseEntity.badRequest().body("지원하지 않는 OAuth 타입입니다.");
            }

            String accessToken = service.requestAccessToken(code);
            Map<String, Object> userInfo = service.requestUserInfo(accessToken);
            //인터페이스를 이용해 구현된 Service를 실행함.

            String userId = extractUserIdFrom(userInfo, oauthType);
            //Service 객체로 부터 User의 이메일 값을 상속 받음
            UserDTO userDTO = userService.UserSelect(userId);
            // 이메일을 이용해서 User 정보의 존재 여부를 확인

            if (userDTO == null) {
                // 회원가입 처리
                userDTO = userService.UserCreate(userInfo, oauthType);
                // User 정보가 없다면 OAuth2 타입 정보를 받은 데이터를 생성하고 반환 받음
            }

            String jwt = tokenProvider.createToken(userDTO);
            //반환 받은 UserDTO를 토대로 jwt 토큰 생성

            return ResponseEntity.ok(responseDTO.Response("success", "OAuth2 인증 완료", Collections.singletonList(Map.of("token", jwt, "user", userInfo))));
            //생성한 JWT 토큰을 프론트 엔드로 보냄

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    // OAuth2를 제공하는 서버에서 반환한 토큰에서 서버에 저장할 값을 반환 받는 코드
    public String extractUserIdFrom(Map<String, Object> userInfo, String provider) {
        switch (provider.toLowerCase()) {
            case "kakao":
                // kakao는 "id" 필드가 고유 ID
                Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
                return (String) kakaoAccount.get("email");
            case "naver":
                // naver는 "response" 내부에 있음
                Map<String, Object> naverResponse = (Map<String, Object>) userInfo.get("response");
                return (String) naverResponse.get("email");
            case "google":
                // google은 "sub" 필드가 고유 ID
                return (String) userInfo.get("email");
            default:
                throw new IllegalArgumentException("지원하지 않는 OAuth 타입입니다.");
        }
    }



}
