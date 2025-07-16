package com.metamate.config.security;


import com.metamate.config.jwt.JwtProperties;
import com.metamate.domain.login.dto.UserDTO;
import com.metamate.domain.login.entity.UserEntity;
import com.metamate.domain.login.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
@Component 
public class TokenProvider {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserService userService;

    // JWT 토큰 생성
    public String createToken(UserDTO userDTO)
    {
        String secretKey = jwtProperties.getSecretKey();
        log.info("JwtProperties.secretKey = {}", jwtProperties.getSecretKey());
        Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
        // 기한 : 지금으로부터 1시간
        // 생성
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder().signWith(SignatureAlgorithm.HS512, key)
                .setSubject(userDTO.getUserEmail())
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .compact();
    }

    // 토큰 디코딩 + 위조 여부 확인 -> 아이디 리턴
    public String validateAndGetUserId(String token) {

        Claims claims = Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    
    // 토큰 디코딩 + 위조 여부 확인 -> 토큰 전체 정보 리턴
    public Claims GetClaims(String token) {

        Claims claims = Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }


    //해당 토큰이 가용 가능한 토큰인지 확인
    public Boolean validateToken(String token) {

        Boolean res = false;

        //token에서 id추출
        final String id = getUsernameFromToken(token);

        //①token에서 추출한 id가 db에 존재하는지 검증
        res = userService.getUserID(id);
        if( res == true )
        {
            //②token만료시간 지났는지 검증
            if( !isTokenExpired(token) )
            {
                res = true;
            }
            else
            {
                res = false;
                log.error("validateToken token검증 : 만료시간이 지남 ");
            }
        }
        else
        {
            res = false;
            log.error("validateToken token검증 : id가 존재하지 않음.") ;
        }

        return res;
    }

    // token으로부터 id를 조회
    public String getUsernameFromToken(String token) {
        String tokenUserId = null;
        try {
            final Claims claims = GetClaims(token);
            if (claims != null) {
                if( claims.getId() != null ) tokenUserId = claims.getId();
                if( claims.getSubject() != null ) tokenUserId = claims.getSubject();

            }else {
                log.error("claims is null");
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while invoke getUsernameFromToken(). Reason : [{}]",
                    e.getMessage());
        }
        return tokenUserId;
    }

    //token 만료 여부 검증
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }


    //token 만료시간 조회
    public Date getExpirationDateFromToken(String token) {
        Date expiration = null;
        try {
            final Claims claims = GetClaims(token);
            log.info("############claims###########" + claims);
            if (claims != null) {
                //expiration=만료시간
                expiration = claims.getExpiration();
            }
        }catch (Exception e) {
            log.error("Unhandled exception occurred while invoke getExpirationDateFromToken(). Reason : [{}]",
                    e.getMessage());

        }
        return expiration;
    }



}
