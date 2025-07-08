package KDT_Hackathon.backend.Login.Service;

import KDT_Hackathon.backend.Config.CommonType.UserRole;
import KDT_Hackathon.backend.Login.DTO.UserDTO;
import KDT_Hackathon.backend.Login.Entity.UserEntity;
import KDT_Hackathon.backend.Login.Mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService
{
    @Autowired
    UserMapper userMapper;

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
