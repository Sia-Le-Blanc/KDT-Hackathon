package KDT_Hackathon.backend.Login.Mapper;

import KDT_Hackathon.backend.Login.Entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper
{

    UserEntity selectUserByUserEmail(@Param("userEmail") String userEmail);//처리 완료
    List<UserEntity> selectAllUsers();
    void insertUser(UserEntity user);//처리 완료
    void  updateUserInfo(UserEntity userEntity);//처리완료
    void deleteUser(@Param("userId") Long userId);
    Long findByEmailAndPassword(@Param("userEmail") String userEmail, @Param("userPassword") String Password);
    Boolean existsByEmail(@Param("userEmail") String userEmail);
}
