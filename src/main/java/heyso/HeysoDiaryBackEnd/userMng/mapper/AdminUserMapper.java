package heyso.HeysoDiaryBackEnd.userMng.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserDetailRow;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserListRow;
import heyso.HeysoDiaryBackEnd.userMng.dto.AdminUserSearchRequest;

@Mapper
public interface AdminUserMapper {

    List<AdminUserListRow> selectAdminUserPage(@Param("request") AdminUserSearchRequest request);

    long countAdminUsers(@Param("request") AdminUserSearchRequest request);

    AdminUserDetailRow selectAdminUserDetail(@Param("userId") Long userId);

    int countOtherActiveAdmins(@Param("userId") Long userId);

    int updateAdminUser(
            @Param("userId") Long userId,
            @Param("nickname") String nickname,
            @Param("role") String role);

    int updateAdminUserStatus(
            @Param("userId") Long userId,
            @Param("status") String status);

    int updateLocalUserPassword(
            @Param("userId") Long userId,
            @Param("passwordHash") String passwordHash);

    int deleteUserById(@Param("userId") Long userId);
}
