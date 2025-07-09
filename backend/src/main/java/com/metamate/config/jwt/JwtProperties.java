package com.metamate.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component  // 필수
@ConfigurationProperties(prefix = "jwt")  // application.yml 에서 "jwt:" 키 아래를 읽어옴
@Getter
@Setter
public class JwtProperties {
    private String secretKey;
    private String otherValue;
}

