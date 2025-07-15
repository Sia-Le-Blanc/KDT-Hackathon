// MetaworkMateApplication.java
package com.metamate;

import com.metamate.config.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class MetaworkMateApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetaworkMateApplication.class, args);
    }
}
