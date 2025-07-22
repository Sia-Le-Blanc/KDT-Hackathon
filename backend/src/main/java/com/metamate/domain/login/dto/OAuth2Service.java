package com.metamate.domain.login.dto;

import java.util.Map;

public interface OAuth2Service {
    String requestAccessToken(String code);
    Map<String, Object> requestUserInfo(String accessToken);
}