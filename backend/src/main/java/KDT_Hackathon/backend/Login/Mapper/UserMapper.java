package KDT_Hackathon.backend.Login.Mapper;

import KDT_Hackathon.backend.Login.Entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper
{

    UserEntity selectUserById(@Param("userId") Long userId);
    UserEntity selectUserByUserEmail(@Param("userEmail") String userEmail);
    List<UserEntity> selectAllUsers();
    void insertUser(UserEntity user);
    void updateUserEmail(@Param("userId") Long userId, @Param("email") String email);
    void deleteUser(@Param("userId") Long userId);

}
