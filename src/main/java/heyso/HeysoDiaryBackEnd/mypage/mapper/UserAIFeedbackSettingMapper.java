package heyso.HeysoDiaryBackEnd.mypage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.mypage.model.UserAIFeedbackSetting;

@Mapper
public interface UserAIFeedbackSettingMapper {
    int insertUserAIFeedbackSettingIfMissing(@Param("userId") Long userId);

    UserAIFeedbackSetting selectUserAIFeedbackSettingByUserId(@Param("userId") Long userId);

    int updateUserAIFeedbackSetting(UserAIFeedbackSetting setting);
}
