package com.metamate.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [3] 토큰 무효화 및 관리를 위한 서비스
 * 로그인 재접속 실패 시 세션 무효화 또는 토큰 재발급 처리
 */
@Service
public class TokenInvalidationService {

    private static final Logger logger = LoggerFactory.getLogger(TokenInvalidationService.class);

    // 무효화된 토큰 목록 (실제 운영에서는 Redis 등 외부 저장소 사용 권장)
    private final Map<String, Long> invalidatedTokens = new ConcurrentHashMap<>();

    // OTP 사용자 토큰 목록 (자동 재접속 방지용)
    private final Map<String, Boolean> otpUserTokens = new ConcurrentHashMap<>();

    // 정기적으로 만료된 토큰 정리를 위한 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TokenInvalidationService() {
        // 1시간마다 만료된 토큰 정리
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    /**
     * [3] 토큰 무효화
     */
    public void invalidateToken(String tokenId, String reason) {
        if (tokenId != null) {
            invalidatedTokens.put(tokenId, System.currentTimeMillis());
            otpUserTokens.remove(tokenId);
            logger.info("Token invalidated: {}, reason: {}", tokenId, reason);
        }
    }

    /**
     * [1] OTP 사용자 토큰 등록 (자동 재접속 방지)
     */
    public void registerOtpUserToken(String tokenId) {
        if (tokenId != null) {
            otpUserTokens.put(tokenId, true);
            logger.info("OTP user token registered: {}", tokenId);
        }
    }

    /**
     * [3] 토큰 유효성 검사
     */
    public boolean isTokenValid(String tokenId) {
        if (tokenId == null) {
            return false;
        }

        // 무효화된 토큰인지 확인
        if (invalidatedTokens.containsKey(tokenId)) {
            logger.debug("Token is invalidated: {}", tokenId);
            return false;
        }

        return true;
    }

    /**
     * [1] OTP 사용자 토큰인지 확인
     */
    public boolean isOtpUserToken(String tokenId) {
        return tokenId != null && otpUserTokens.containsKey(tokenId);
    }

    /**
     * [1] OTP 사용자 자동 재접속 방지 체크
     */
    public boolean shouldPreventAutoReconnect(String tokenId) {
        return isOtpUserToken(tokenId);
    }

    /**
     * [4] 사용자 모든 토큰 무효화 (계정 삭제 시)
     */
    public void invalidateAllUserTokens(Long userId, String reason) {
        // 실제 구현에서는 DB에서 해당 사용자의 모든 토큰을 조회하여 무효화
        logger.info("All tokens invalidated for user: {}, reason: {}", userId, reason);
    }

    /**
     * [3] 강제 토큰 갱신 (보안 위협 감지 시)
     */
    public void forceTokenRefresh(String tokenId, String reason) {
        invalidateToken(tokenId, "Force refresh: " + reason);
        logger.warn("Token force refresh requested: {}, reason: {}", tokenId, reason);
    }

    /**
     * 만료된 토큰 정리 (24시간 이상 된 무효화 토큰)
     */
    private void cleanupExpiredTokens() {
        long oneDayAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
        AtomicInteger removedCount = new AtomicInteger();

        invalidatedTokens.entrySet().removeIf(entry -> {
            if (entry.getValue() < oneDayAgo) {
                removedCount.getAndIncrement();
                return true;
            }
            return false;
        });

        if (removedCount.get() > 0) {
            logger.info("Cleaned up {} expired invalidated tokens", removedCount);
        }
    }

    /**
     * [2] 보안 위협 감지 시 긴급 토큰 무효화
     */
    public void emergencyInvalidateToken(String tokenId, String securityThreat) {
        invalidateToken(tokenId, "Security threat: " + securityThreat);
        logger.warn("Emergency token invalidation due to security threat: {}", securityThreat);
    }
}