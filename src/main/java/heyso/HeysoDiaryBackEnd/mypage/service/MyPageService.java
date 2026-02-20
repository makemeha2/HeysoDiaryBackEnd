package heyso.HeysoDiaryBackEnd.mypage.service;

import java.io.IOException;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.mypage.dto.MyPageResponse;
import heyso.HeysoDiaryBackEnd.mypage.dto.MyPageUpdateRequest;
import heyso.HeysoDiaryBackEnd.mypage.mapper.MyPageMapper;
import heyso.HeysoDiaryBackEnd.mypage.model.UserProfile;
import heyso.HeysoDiaryBackEnd.mypage.model.UserThumbnail;
import heyso.HeysoDiaryBackEnd.user.model.User;

@Service
public class MyPageService {
    private final MyPageMapper myPageMapper;

    public MyPageService(MyPageMapper myPageMapper) {
        this.myPageMapper = myPageMapper;
    }

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage() {
        User user = SecurityUtils.getCurrentUserOrThrow();
        ensureUserProfile(user.getUserId());

        UserProfile userProfile = myPageMapper.selectUserProfileByUserId(user.getUserId());
        if (userProfile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
        }

        boolean hasThumbnail = myPageMapper.existsUserThumbnail(user.getUserId()) > 0;
        return MyPageResponse.from(userProfile, hasThumbnail);
    }

    @Transactional
    public void updateMyPage(MyPageUpdateRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        ensureUserProfile(user.getUserId());

        UserProfile userProfile = UserProfile.builder()
                .userId(user.getUserId())
                .nickname(request.getNickname() != null ? request.getNickname().trim() : null)
                .mbti(normalizeMbti(request.getMbti()))
                .build();

        int updatedRows = myPageMapper.updateUserProfile(userProfile);
        if (updatedRows <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to update user profile");
        }

        MultipartFile thumbnail = request.getThumbnail();
        if (thumbnail != null && !thumbnail.isEmpty()) {
            upsertThumbnail(user.getUserId(), thumbnail);
        }
    }

    @Transactional(readOnly = true)
    public UserThumbnail getMyThumbnail() {
        User user = SecurityUtils.getCurrentUserOrThrow();
        UserThumbnail thumbnail = myPageMapper.selectUserThumbnailByUserId(user.getUserId());
        if (thumbnail == null || thumbnail.getImageBlob() == null || thumbnail.getImageBlob().length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thumbnail not found");
        }
        return thumbnail;
    }

    private void ensureUserProfile(Long userId) {
        myPageMapper.insertUserProfileIfMissing(userId);
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

        myPageMapper.upsertUserThumbnail(userThumbnail);
    }

    private String normalizeMbti(String mbti) {
        if (mbti == null || mbti.trim().isEmpty()) {
            return null;
        }
        return mbti.trim().toUpperCase(Locale.ROOT);
    }
}
