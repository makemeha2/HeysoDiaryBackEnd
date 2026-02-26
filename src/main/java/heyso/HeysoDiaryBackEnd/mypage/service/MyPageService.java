package heyso.HeysoDiaryBackEnd.mypage.service;

import java.io.IOException;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.mypage.dto.UserProfileResponse;
import heyso.HeysoDiaryBackEnd.mypage.dto.UserProfileUpdateRequest;
import heyso.HeysoDiaryBackEnd.mypage.mapper.UserProfileMapper;
import heyso.HeysoDiaryBackEnd.mypage.model.UserProfile;
import heyso.HeysoDiaryBackEnd.mypage.model.UserThumbnail;
import heyso.HeysoDiaryBackEnd.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private static final long THUMBNAIL_MAX_BYTES = 500L * 1024L; // 500KB

    private final UserProfileMapper userProfileMapper;

    // TODO : 사용여부에 따라 삭제 처리
    @Transactional(readOnly = true)
    public UserProfileResponse getMyPage() {
        User user = SecurityUtils.getCurrentUserOrThrow();
        ensureUserProfile(user.getUserId());

        UserProfile userProfile = userProfileMapper.selectUserProfileByUserId(user.getUserId());
        if (userProfile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
        }

        boolean hasThumbnail = userProfileMapper.existsUserThumbnail(user.getUserId()) > 0;
        return UserProfileResponse.from(userProfile, hasThumbnail);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile() {
        User user = SecurityUtils.getCurrentUserOrThrow();
        ensureUserProfile(user.getUserId());

        UserProfile userProfile = userProfileMapper.selectUserProfileByUserId(user.getUserId());
        if (userProfile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
        }

        boolean hasThumbnail = userProfileMapper.existsUserThumbnail(user.getUserId()) > 0;
        return UserProfileResponse.from(userProfile, hasThumbnail);
    }

    @Transactional
    public void updateProfile(UserProfileUpdateRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        ensureUserProfile(user.getUserId());

        UserProfile userProfile = UserProfile.builder()
                .userId(user.getUserId())
                .nickname(request.getNickname() != null ? request.getNickname().trim() : null)
                .mbti(normalizeMbti(request.getMbti()))
                .build();

        int updatedRows = userProfileMapper.updateUserProfile(userProfile);
        if (updatedRows <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to update user profile");
        }

        MultipartFile thumbnail = request.getThumbnail();
        if (thumbnail != null && !thumbnail.isEmpty()) {
            if (thumbnail.getSize() > THUMBNAIL_MAX_BYTES) {
                throw new ResponseStatusException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "Thumbnail must be <= 500KB");
            }

            upsertThumbnail(user.getUserId(), thumbnail);
        }
    }

    @Transactional(readOnly = true)
    public UserThumbnail getMyThumbnail() {
        User user = SecurityUtils.getCurrentUserOrThrow();
        UserThumbnail thumbnail = userProfileMapper.selectUserThumbnailByUserId(user.getUserId());
        if (thumbnail == null || thumbnail.getImageBlob() == null || thumbnail.getImageBlob().length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thumbnail not found");
        }
        return thumbnail;
    }

    private void ensureUserProfile(Long userId) {
        userProfileMapper.insertUserProfileIfMissing(userId);
    }

    private void upsertThumbnail(Long userId, MultipartFile thumbnail) {
        String contentType = thumbnail.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thumbnail must be an image file");
        }

        byte[] imageBlob;
        try {
            imageBlob = thumbnail.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read thumbnail file");
        }

        UserThumbnail userThumbnail = UserThumbnail.builder()
                .userId(userId)
                .fileName(thumbnail.getOriginalFilename())
                .contentType(contentType)
                .imageBlob(imageBlob)
                .bytes(imageBlob.length)
                .build();

        userProfileMapper.upsertUserThumbnail(userThumbnail);
    }

    private String normalizeMbti(String mbti) {
        if (mbti == null || mbti.trim().isEmpty()) {
            return null;
        }
        return mbti.trim().toUpperCase(Locale.ROOT);
    }
}
