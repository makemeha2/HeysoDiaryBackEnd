package heyso.HeysoDiaryBackEnd.mypage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.mypage.model.UserProfile;
import heyso.HeysoDiaryBackEnd.mypage.model.UserThumbnail;

@Mapper
public interface MyPageMapper {
    int insertUserProfileIfMissing(@Param("userId") Long userId);

    UserProfile selectUserProfileByUserId(@Param("userId") Long userId);

    int updateUserProfile(UserProfile userProfile);

    int existsUserThumbnail(@Param("userId") Long userId);

    UserThumbnail selectUserThumbnailByUserId(@Param("userId") Long userId);

    int upsertUserThumbnail(UserThumbnail userThumbnail);
}
