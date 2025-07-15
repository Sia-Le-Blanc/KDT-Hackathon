package com.metamate.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.rmi.server.ExportException;


@Slf4j
@Service
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // req 에서 token 가져오기
            String token = parseBearerToken(request);
            log.info("JwtAuthenticationFilter is running...");

            // token 검사
            if (token != null && !token.equalsIgnoreCase("null"))
            {
                String userId = tokenProvider.validateAndGetUserId(token);
                log.info("Authenticated userId: {}", userId);

                if (!tokenProvider.validateToken(token))
                {
                    //토큰의 날짜를 확인하여, 현재 날짜 기준 만료 날짜가 지난 토큰일 경우 에러 코드를 생성함.
                    throw new ExportException("토큰 만료로 인해 DB에 접속할 수 없습니다.");
                }

                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, AuthorityUtils.NO_AUTHORITIES);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);
            }
        }
        catch (ExportException e)
        {
            log.error(e.getMessage());
        }
        catch (Exception e)
        {
            log.error("Could not set user authentication");
        }

        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        return null;
    }
}
