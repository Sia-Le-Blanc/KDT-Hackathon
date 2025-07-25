package com.metamate.domain.login.service;

import com.metamate.domain.login.dto.OAuth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2ServiceFactory {

    private final Map<String, OAuth2Service> serviceMap = new HashMap<>();

    @Autowired
    public OAuth2ServiceFactory(ApplicationContext context) {
        Map<String, OAuth2Service> beans = context.getBeansOfType(OAuth2Service.class);
        for (Map.Entry<String, OAuth2Service> entry : beans.entrySet()) {
            serviceMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }
    }
    // OAuth2Service Interface를 상속하는 객체를 Map에 저장
    // 전략 패턴 + 팩토리 패턴

    public OAuth2Service getService(String provider) {
        return serviceMap.get(provider.toLowerCase());
    }//어떤 Service로 넘길 것인지 선택
}
