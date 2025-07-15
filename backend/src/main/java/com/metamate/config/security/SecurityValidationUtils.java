package com.metamate.config.security;
import com.metamate.config.expection.clud.FindFailedException;
import com.metamate.domain.login.dto.LoginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * [2][5] 보안 검증 유틸리티
 * 암호화/인증 방식 검증 및 기타 보안 체크
 */
@Component
public class SecurityValidationUtils {

    private static final Logger logger = LoggerFactory.getLogger(SecurityValidationUtils.class);

    // [2] 허용되는 암호화 방식
    private static final List<String> ALLOWED_ENCRYPTION_TYPES = Arrays.asList(
            "AES256", "AES128", "RSA2048", "RSA4096", "TLS1.2", "TLS1.3"
    );

    // [2] 허용되는 인증 방식
    private static final List<String> ALLOWED_AUTH_METHODS = Arrays.asList(
            "JWT", "OAUTH2", "SAML", "LDAP", "CERTIFICATE", "BIOMETRIC"
    );

    // [2] 금지된 설정 (보안상 위험)
    private static final List<String> FORBIDDEN_SETTINGS = Arrays.asList(
            "NONE", "NULL", "DISABLED", "OFF", "CLEAR", "PLAIN"
    );

    // 의심스러운 IP 패턴
    private static final Pattern SUSPICIOUS_IP_PATTERN = Pattern.compile(
            "^(10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.|192\\.168\\.|127\\.)"
    );

    /**
     * [2] 로그인 보안 설정 전체 검증
     */
    public void validateLoginSecurity(LoginDTO loginDTO, String clientIp) {
        validateEncryptionType(loginDTO.getEncryptionType());
        validateAuthenticationMethod(loginDTO.getAuthenticationMethod());
        validateSecurityCombination(loginDTO.getEncryptionType(), loginDTO.getAuthenticationMethod());

        // 추가 보안 체크
        if (clientIp != null) {
            checkSuspiciousActivity(loginDTO.getUserEmail(), clientIp);
        }
    }

    /**
     * [2] 암호화 방식 검증
     */
    private void validateEncryptionType(String encryptionType) {
        if (encryptionType == null || encryptionType.trim().isEmpty()) {
            logger.warn("Login attempt with null/empty encryption type");
            throw new FindFailedException("암호화 방식이 설정되지 않았습니다.");
        }

        String normalizedType = encryptionType.trim().toUpperCase();

        // [2] NONE 또는 기타 금지된 설정 체크
        if (FORBIDDEN_SETTINGS.contains(normalizedType)) {
            logger.warn("Login attempt with forbidden encryption type: {}", encryptionType);
            throw new FindFailedException("안전하지 않은 암호화 설정입니다. 로그인할 수 없습니다.");
        }

        // 허용된 암호화 방식인지 확인
        if (!ALLOWED_ENCRYPTION_TYPES.contains(normalizedType)) {
            logger.warn("Login attempt with unsupported encryption type: {}", encryptionType);
            throw new FindFailedException("지원하지 않는 암호화 방식입니다.");
        }
    }

    /**
     * [2] 인증 방식 검증
     */
    private void validateAuthenticationMethod(String authMethod) {
        if (authMethod == null || authMethod.trim().isEmpty()) {
            logger.warn("Login attempt with null/empty authentication method");
            throw new FindFailedException("인증 방식이 설정되지 않았습니다.");
        }

        String normalizedMethod = authMethod.trim().toUpperCase();

        // [2] NONE 또는 기타 금지된 설정 체크
        if (FORBIDDEN_SETTINGS.contains(normalizedMethod)) {
            logger.warn("Login attempt with forbidden authentication method: {}", authMethod);
            throw new FindFailedException("안전하지 않은 인증 설정입니다. 로그인할 수 없습니다.");
        }

        // 허용된 인증 방식인지 확인
        if (!ALLOWED_AUTH_METHODS.contains(normalizedMethod)) {
            logger.warn("Login attempt with unsupported authentication method: {}", authMethod);
            throw new FindFailedException("지원하지 않는 인증 방식입니다.");
        }
    }

    /**
     * [2] 암호화와 인증 방식 조합 검증
     */
    private void validateSecurityCombination(String encryptionType, String authMethod) {
        if (encryptionType == null || authMethod == null) {
            return;
        }

        String normalizedEncryption = encryptionType.trim().toUpperCase();
        String normalizedAuth = authMethod.trim().toUpperCase();

        // 보안상 위험한 조합 체크
        if ("AES128".equals(normalizedEncryption) && "JWT".equals(normalizedAuth)) {
            logger.warn("Potentially weak security combination: {} + {}", encryptionType, authMethod);
            // 경고는 하되 차단하지는 않음 (비즈니스 요구사항에 따라 조정)
        }

        // 권장하지 않는 조합에 대한 추가 검증 로직
        validateAdvancedSecurityCombination(normalizedEncryption, normalizedAuth);
    }

    /**
     * 고급 보안 조합 검증
     */
    private void validateAdvancedSecurityCombination(String encryption, String auth) {
        // 예: 특정 조합에서만 허용되는 경우
        if ("RSA4096".equals(encryption) && !"CERTIFICATE".equals(auth)) {
            logger.info("High-security encryption with standard auth: {} + {}", encryption, auth);
        }
    }

    /**
     * [5] 의심스러운 활동 감지
     */
    private void checkSuspiciousActivity(String userEmail, String clientIp) {
        // 내부 IP에서의 로그인 시도 체크
        if (SUSPICIOUS_IP_PATTERN.matcher(clientIp).find()) {
            logger.info("Login attempt from internal IP: {} for user: {}", clientIp, userEmail);
        }

        // 추가 의심스러운 패턴 검사
        checkBruteForcePattern(userEmail, clientIp);
        checkUnusualLoginTime(userEmail);
    }

    /**
     * 무차별 대입 공격 패턴 검사
     */
    private void checkBruteForcePattern(String userEmail, String clientIp) {
        // 실제 구현에서는 Redis 등을 사용하여 로그인 시도 횟수 추적
        logger.debug("Checking brute force pattern for user: {} from IP: {}", userEmail, clientIp);
    }

    /**
     * 비정상적인 로그인 시간 체크
     */
    private void checkUnusualLoginTime(String userEmail) {
        // 업무 시간 외 로그인, 휴일 로그인 등 체크
        logger.debug("Checking unusual login time for user: {}", userEmail);
    }

    /**
     * [2] 보안 설정 권장사항 검증
     */
    public void validateRecommendedSecurityLevel(LoginDTO loginDTO) {
        String encryption = loginDTO.getEncryptionType();
        String auth = loginDTO.getAuthenticationMethod();

        if (encryption != null && auth != null) {
            String normalizedEncryption = encryption.trim().toUpperCase();
            String normalizedAuth = auth.trim().toUpperCase();

            // 최소 보안 수준 체크
            if ("AES128".equals(normalizedEncryption)) {
                logger.info("User using minimum encryption level: {}", encryption);
            }

            // 권장 보안 수준 체크
            if ("AES256".equals(normalizedEncryption) || "RSA2048".equals(normalizedEncryption)) {
                logger.info("User using recommended encryption level: {}", encryption);
            }
        }
    }

    /**
     * [2] 긴급 보안 차단 체크
     */
    public boolean shouldBlockForSecurity(LoginDTO loginDTO, String clientIp) {
        // 실제 운영에서는 외부 보안 시스템과 연동
        // 예: WAF, IDS/IPS, 위협 인텔리전스 등

        if (loginDTO.getEncryptionType() != null &&
                FORBIDDEN_SETTINGS.contains(loginDTO.getEncryptionType().toUpperCase())) {
            return true;
        }

        if (loginDTO.getAuthenticationMethod() != null &&
                FORBIDDEN_SETTINGS.contains(loginDTO.getAuthenticationMethod().toUpperCase())) {
            return true;
        }

        return false;
    }
}
