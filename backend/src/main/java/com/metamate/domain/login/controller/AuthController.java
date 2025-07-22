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
            if (service == null) {
                return ResponseEntity.badRequest().body("지원하지 않는 OAuth 타입입니다.");
            }

            String accessToken = service.requestAccessToken(code);
            Map<String, Object> userInfo = service.requestUserInfo(accessToken);

            String userId = extractUserIdFrom(userInfo, oauthType);
            UserDTO userDTO = userService.UserSelect(userId);

            if (userDTO == null) {
                // 회원가입 처리
                userDTO = userService.UserCreate(userInfo, oauthType);
            }

            String jwt = tokenProvider.createToken(userDTO);

            return ResponseEntity.ok(Map.of("token", jwt, "user", userInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

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
