package heyso.HeysoDiaryBackEnd.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.user.model.User;
import heyso.HeysoDiaryBackEnd.user.model.UserAuth;

@Mapper
public interface UserMapper {

    User selectUserById(@Param("userId") Long userId);

    User selectUserByEmail(@Param("email") String email);

    void insertUser(User user); // useGeneratedKeys

    UserAuth selectUserAuthByProviderAndProviderUserId(
            @Param("authProvider") String authProvider,
            @Param("providerUserId") String providerUserId);

    void insertUserAuth(UserAuth userAuth);

    void updateUserAuthLastLoginAt(@Param("userAuthId") Long userAuthId);
}
