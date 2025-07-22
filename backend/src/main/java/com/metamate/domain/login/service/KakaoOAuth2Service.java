package com.metamate.domain.login.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamate.domain.login.dto.OAuth2Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Service("kakao")
public class KakaoOAuth2Service implements OAuth2Service {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Override
    public String requestAccessToken(String code) {
        try {
            URL url = new URL("https://kauth.kakao.com/oauth/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String params = "grant_type=authorization_code"
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&redirect_uri=" + redirectUri
                    + "&code=" + code;

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes());
            }

            InputStream inputStream = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = mapper.readValue(inputStream, Map.class);

            return (String) response.get("access_token");
        } catch (Exception e) {
            throw new RuntimeException("카카오 액세스 토큰 요청 실패", e);
        }
    }


    @Override
    public Map<String, Object> requestUserInfo(String accessToken)
    {
        try {
            URL url = new URL("https://kapi.kakao.com/v2/user/me");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            InputStream inputStream = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(inputStream, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 요청 실패", e);
        }
    }
}
