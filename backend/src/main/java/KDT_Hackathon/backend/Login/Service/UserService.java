package KDT_Hackathon.backend.Login.Service;

import KDT_Hackathon.backend.Config.CommonType.UserRole;
import KDT_Hackathon.backend.Config.Expection.CLUD.FindFailedException;
import KDT_Hackathon.backend.Config.Expection.CLUD.InsertFailedException;
import KDT_Hackathon.backend.Config.Expection.CLUD.UpdateFailedException;
import KDT_Hackathon.backend.Login.DTO.LoginDTO;
import KDT_Hackathon.backend.Login.DTO.LoginSelectDTO;
import KDT_Hackathon.backend.Login.DTO.UserDTO;
import KDT_Hackathon.backend.Login.Entity.UserEntity;
import KDT_Hackathon.backend.Login.Mapper.UserMapper;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService
{
    @Autowired
    UserMapper userMapper;

    //데이터 저장 - 처리함
    public UserDTO UserCreate(UserDTO userDTO, PasswordEncoder passwordEncoder)
    {
        try
        {
            UserEntity userEntity2 = ConvertToEntity(userDTO, passwordEncoder);
            userMapper.insertUser(userEntity2);
            UserEntity userEntity = userMapper.selectUserByUserEmail(userEntity2.getUserEmail());
            if(userEntity != null)
            {
                return ConvertToDTO(userEntity);
            }
            return  userDTO;

        } catch (Exception e) {
            throw new InsertFailedException(e);
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
            UserEntity userEntity = userMapper.selectUserByUserEmail(loginDTO.getUserEmail());
            if(userEntity != null && passwordEncoder.matches(loginDTO.getUserPassword(), userEntity.getUserPassword()))
            {
                return ConvertToDTO(userEntity);
            }
            else
            {
                throw new RuntimeException("없는 아이디이거나, 아이디 혹은 비밀번호가 틀린 것 같습니다.");
            }
        }
        catch (Exception e)
        {
            throw new FindFailedException(e);
        }
    }

    //회원정보 탈퇴
    public Boolean UserDelete(String UserEmail, String UserPassword, PasswordEncoder passwordEncoder)
    {
        try
        {
            Long UserId = userMapper.findByEmailAndPassword(UserEmail, passwordEncoder.encode(UserPassword));
            if(UserId != null)
            {
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
        Boolean bool = userMapper.existsByEmail(userId);
        return bool;
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

    private UserDTO ConvertToDTO(UserEntity userEntity)
    {
        return UserDTO.builder()
                .userId(null)
                .companyId(null)
                .userAge(userEntity.getUserAge())
                .userEmail(userEntity.getUserEmail())
                .userName(userEntity.getUserName())
                .userPassword(userEntity.getUserPassword())
                .region(userEntity.getRegion())
                .position(userEntity.getPosition())
                .userRole(UserRole.valueOf(userEntity.getUserRole()))
                .createdAt(LocalDate.now().atStartOfDay())
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
                .userRole(String.valueOf(userDTO.getUserRole()))
                .createdAt(LocalDate.now().atStartOfDay())
                .build();
    }
}
