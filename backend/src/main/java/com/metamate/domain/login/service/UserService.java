package com.metamate.domain.login.service;

import com.metamate.config.common.UserRole;
import com.metamate.config.expection.clud.FindFailedException;
import com.metamate.config.expection.clud.InsertFailedException;
import com.metamate.config.expection.clud.UpdateFailedException;
import com.metamate.domain.login.dto.LoginDTO;
import com.metamate.domain.login.dto.LoginSelectDTO;
import com.metamate.domain.login.dto.UserDTO;
import com.metamate.domain.login.entity.UserEntity;
import com.metamate.domain.login.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
public class UserService
{
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    UserMapper userMapper;

    //데이터 저장 - 처리함
    public UserDTO UserCreate(UserDTO userDTO, PasswordEncoder passwordEncoder) {

        UserEntity userEntity2 = ConvertToEntity(userDTO, passwordEncoder);
        userMapper.insertUser(userEntity2);
        UserEntity userEntity = userMapper.selectUserByUserEmail(userEntity2.getUserEmail());
        if (userEntity != null) {
            return ConvertToDTO(userEntity);
        }
        else
        {
            throw new InsertFailedException("데이터 삽입 도중 에러가 발생하였습니다, 저장된 User 정보를 찾을 수 없습니다.");
        }
    }

    public LoginSelectDTO tokenSelect(UserDTO userDTO, String Token)
    {
        return LoginSelectDTO.builder()
                .userEmail(userDTO.getUserEmail())
                .Token(Token)
                .build();
    }


    //데이터 업데이트 - 처리함
    public UserDTO UserUpdate(UserDTO userDTO, PasswordEncoder passwordEncoder)
    {
        try
        {
            UserEntity OlduserEntity = userMapper.selectUserByUserEmail(userDTO.getUserEmail());
            UserDTO NewUserDTO = ConvertToChangeEntity(userDTO, OlduserEntity);
            UserEntity UpdatingEntity = ConvertToEntity(NewUserDTO, passwordEncoder);
            userMapper.updateUserInfo(UpdatingEntity);
            UserEntity UpdatedEntity = userMapper.selectUserByUserEmail(UpdatingEntity.getUserEmail());
            if(UpdatingEntity.equals(UpdatedEntity))
            {
                return ConvertToDTO(UpdatedEntity);
            }
            else
            {
                throw new UpdateFailedException("데이터 업데이트 실패");
            }
        }
        catch (Exception e)
        {
            throw new UpdateFailedException(e);
        }
    }
    //로그인 성공 여부 확인 - 처리함
    public UserDTO UserLogin(LoginDTO loginDTO, PasswordEncoder passwordEncoder)
    {
        try
        {
            validateSecuritySettings(loginDTO);

            UserEntity userEntity = userMapper.selectUserByUserEmail(loginDTO.getUserEmail());
            if(userEntity != null && passwordEncoder.matches(loginDTO.getUserPassword(), userEntity.getUserPassword()))
            {
                // OTP 검증 (사용하는 경우)
                if (userEntity.getUseOtp() != null && userEntity.getUseOtp()) {
                    validateOtpCode(loginDTO, userEntity);
                }

                if (userEntity.getIsDeleted()) {
                    throw new RuntimeException("탈퇴했거나 존재하지 않는 사용자입니다.");
                }

                if (!userEntity.getIsActive()) {
                    throw new RuntimeException("비활성화된 사용자입니다.");
                }

                // 기존 토큰 무효화
                if (userEntity.getUserId() != null) {
                    invalidateExistingToken(userEntity.getUserId());
                }

                return ConvertToDTO(userEntity);
            }
            else
            {
                throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
            }
        }
        catch (Exception e)
        {
            throw new FindFailedException(e);
        }
    }

    // 보안 설정 검증
    private void validateSecuritySettings(LoginDTO loginDTO) {
        if ("NONE".equalsIgnoreCase(loginDTO.getEncryptionType()) ||
                "NONE".equalsIgnoreCase(loginDTO.getAuthenticationMethod())) {
            logger.warn("Login attempt with NONE security setting from: " + loginDTO.getUserEmail());
            throw new FindFailedException("보안 설정이 올바르지 않습니다. 로그인할 수 없습니다.");
        }
    }

    // OTP 검증
    private void validateOtpCode(LoginDTO loginDTO, UserEntity userEntity) {
        if (loginDTO.getOtpCode() == null || loginDTO.getOtpCode().trim().isEmpty()) {
            logger.warn("OTP required but not provided for user: " + loginDTO.getUserEmail());
            throw new FindFailedException("OTP 코드가 필요합니다.");
        }

        // 실제 OTP 검증 로직 (TOTP 라이브러리 사용 등)
        boolean isValidOtp = validateTOTP(loginDTO.getOtpCode(), userEntity.getOtpSecret());
        if (!isValidOtp) {
            logger.warn("Invalid OTP code for user: " + loginDTO.getUserEmail());
            throw new FindFailedException("OTP 코드가 올바르지 않습니다.");
        }
    }

    // 기존 토큰 무효화
    private void invalidateExistingToken(Long userId) {
        try {
            String existingToken = userMapper.getCurrentTokenByUserId(userId);
            if (existingToken != null) {
                userMapper.invalidateUserToken(userId);
                logger.info("Invalidated existing token for user ID: " + userId);
            }
        } catch (Exception e) {
            logger.warn("Error invalidating existing token for user ID: " + userId + ", error: " + e.getMessage());
        }
    }

    // 새 토큰 등록
    public void registerNewToken(Long userId, String tokenId) {
        try {
            userMapper.updateUserToken(userId, tokenId, LocalDateTime.now());
            logger.info("Registered new token for user ID: " + userId);
        } catch (Exception e) {
            logger.error("Error registering new token for user ID: " + userId + ", error: " + e.getMessage());
        }
    }

    // OTP 검증 로직
    private boolean validateTOTP(String otpCode, String otpSecret) {
        // 실제 구현에서는 Google Authenticator 호환 TOTP 검증
        // 예시: return TOTPGenerator.validate(otpCode, otpSecret, 30);
        return otpCode != null && otpCode.length() == 6 && otpCode.matches("\\d{6}");
    }

    //회원정보 탈퇴
    public Boolean UserDelete(String UserEmail, String UserPassword, PasswordEncoder passwordEncoder)
    {
        try
        {
            Long UserId = userMapper.findByEmailAndPassword(UserEmail, passwordEncoder.encode(UserPassword));
            if(UserId != null)
            {
                // 토큰 무효화
                invalidateExistingToken(UserId);

                userMapper.deleteUser(UserId);
                UserEntity userEntity = userMapper.selectUserByUserEmail(UserEmail);
                if(userEntity == null)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                throw new FindFailedException("해당 이메일에 해당하는 데이터를 찾을 수 없습니다.");
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    //userID가 DB에 있는지 여부 확인
    public Boolean getUserID(String userId)
    {
        int bool = userMapper.existsByEmail(userId);
        if(bool > 0)
        {
            return true;
        }
        else {
            return false;
        }
    }
    private UserDTO ConvertToChangeEntity(UserDTO userDTO, UserEntity olduserEntity)
    {
        return UserDTO.builder()
                .userId(userDTO.getUserId())
                .userAge(userDTO.getUserAge())
                .userEmail(olduserEntity.getUserEmail())
                .userName(userDTO.getUserName())
                .userPassword(userDTO.getUserPassword())
                .userRole(userDTO.getUserRole())
                .region(userDTO.getRegion())
                .position(userDTO.getPosition())
                .createdAt(userDTO.getCreatedAt())
                .companyId(userDTO.getCompanyId())
                .build();
    }

    //데이터 조회
    public  UserDTO UserSelect(String UserEmail)
    {
        try
        {
            UserEntity userEntity = userMapper.selectUserByUserEmail(UserEmail);
            if(userEntity != null)
            {
                return ConvertToDTO(userEntity);
            }
            else
            {
                throw new FindFailedException("조회할 데이터가 존재하지 않습니다.");
            }

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private UserDTO ConvertToDTO(UserEntity userEntity) {
        String roleStr = userEntity.getUserRole();
        UserRole role;
        try {
            role = roleStr != null ? UserRole.valueOf(roleStr.toUpperCase()) : UserRole.ADMIN;
        } catch (IllegalArgumentException e) {
            // 로그 남기고 기본값 사용
            log.warn("Unknown userRole received: {}, defaulting to ADMIN", roleStr);
            role = UserRole.ADMIN;
        }

        return UserDTO.builder()
                .userId(userEntity.getUserId())
                .companyId(userEntity.getCompanyId())
                .userAge(userEntity.getUserAge())
                .userEmail(userEntity.getUserEmail())
                .userName(userEntity.getUserName())
                .userPassword(userEntity.getUserPassword())
                .region(userEntity.getRegion())
                .position(userEntity.getPosition())
                .userRole(role)
                .createdAt(userEntity.getCreatedAt()) // 이게 맞습니다.
                .build();
    }

    private UserEntity ConvertToEntity(UserDTO userDTO, PasswordEncoder passwordEncoder)
    {
        return UserEntity.builder()
                .userId(null)
                .companyId(null)
                .userAge(userDTO.getUserAge())
                .userEmail(userDTO.getUserEmail())
                .userName(userDTO.getUserName())
                .userPassword(passwordEncoder.encode(userDTO.getUserPassword()))
                .region(userDTO.getRegion())
                .position(userDTO.getPosition())
                .userRole(
                        userDTO.getUserRole() != null
                                ? userDTO.getUserRole().name()
                                : String.valueOf(UserRole.ADMIN) // 기본 권한 지정
                )
                .createdAt(LocalDate.now().atStartOfDay())
                .build();
    }

    public UserDTO UserCreate(Map<String, Object> userInfo, String oauthType) {
        // 1. userInfo에서 이메일, 이름 등 추출
        String email;
        String name;
        String socialId;

        switch(oauthType.toLowerCase())
        {
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
                email = (String) kakaoAccount.get("email");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");  // 여기서 캐스팅
                name = (String) profile.get("nickname");
                socialId = String.valueOf(userInfo.get("id"));
            case "naver":
                Map<String, Object> naverResp = (Map<String, Object>) userInfo.get("response");
                email = (String) naverResp.get("email");
                name = (String) naverResp.get("name");
                socialId = (String) naverResp.get("id");
                break;
            case "google":
                email = (String) userInfo.get("email");
                name = (String) userInfo.get("name");
                socialId = (String) userInfo.get("sub");
                break;
            case "facebook":
                email = (String) userInfo.get("email");
                name = (String) userInfo.get("name");
                socialId = (String) userInfo.get("id");
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 OAuth 타입입니다.");
        }

        // 2. 기존 회원 여부 조회
        UserEntity existingUser = userMapper.selectUserByUserEmail(email);
        if (existingUser != null) {
            // 이미 가입된 사용자라면 그대로 반환
            return ConvertToDTO(existingUser);
        }

        // 3. 신규 회원 생성
        UserEntity newUser = UserEntity.builder()
                .userEmail(email)
                .userName(name)
                .provider(oauthType)
                .socialId(socialId)
                .userRole(String.valueOf(UserRole.ADMIN))  // 기본 권한
                .createdAt(LocalDateTime.now())
                .userPassword(generateRandomPassword())
                .build();

        userMapper.insertUser(newUser);

        return ConvertToDTO(newUser);
    }

    private String generateRandomPassword() {
        // 랜덤 문자열 생성 로직 (예: UUID 등)
        return UUID.randomUUID().toString();
    }
}