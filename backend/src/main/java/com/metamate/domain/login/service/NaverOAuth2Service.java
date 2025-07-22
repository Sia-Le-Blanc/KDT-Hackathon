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

@Service("naver")
public class NaverOAuth2Service implements OAuth2Service
{
    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;

    @Override
    public String requestAccessToken(String code)
    {
        try {
            URL url = new URL("https://nid.naver.com/oauth2.0/token");
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

        }
        catch (Exception e)
        {
            throw new RuntimeException("네이버 액세스 토큰 요청 실패", e);
        }
    }

    @Override
    public Map<String, Object> requestUserInfo(String accessToken) {
        try {
            URL url = new URL("https://openapi.naver.com/v1/nid/me");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            InputStream inputStream = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = mapper.readValue(inputStream, Map.class);

            // 네이버 사용자 정보는 "response" 키 안에 실제 유저 정보가 있음
            return (Map<String, Object>) response.get("response");
        } catch (Exception e) {
            throw new RuntimeException("네이버 사용자 정보 요청 실패", e);
        }
    }
}
