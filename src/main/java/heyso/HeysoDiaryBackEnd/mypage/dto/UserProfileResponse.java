package heyso.HeysoDiaryBackEnd.mypage.dto;

import heyso.HeysoDiaryBackEnd.mypage.model.UserProfile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private Long userId;
    private String nickname;
    private String mbti;
    private Boolean hasThumbnail; // TODO : 이 또한 쓸지말지 고민중.
    private String thumbnailUrl; // TODO : 쓸지말지 고민해보자.

    public static UserProfileResponse from(UserProfile profile, boolean hasThumbnail) {
        return UserProfileResponse.builder()
                .userId(profile.getUserId())
                .nickname(profile.getNickname())
                .mbti(profile.getMbti())
                .hasThumbnail(hasThumbnail)
                .thumbnailUrl(hasThumbnail ? "/api/mypage/thumbnail" : null)
                .build();
    }
}
