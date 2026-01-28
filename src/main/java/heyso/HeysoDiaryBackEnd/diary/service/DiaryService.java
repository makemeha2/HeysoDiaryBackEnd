package heyso.HeysoDiaryBackEnd.diary.service;

import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiarySummaryResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryDetailResponse;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryTag;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryEditRequest;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.diary.model.Diary;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryMonthlyCount;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DiaryService {

    private final DiaryMapper diaryMapper;

    public DiaryService(DiaryMapper diaryMapper) {
        this.diaryMapper = diaryMapper;
    }

    public DiaryListResponse getDiaryList(DiaryListRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        request.setUserId(user.getUserId());

        List<DiarySummary> diaries = diaryMapper.selectDiaryList(request);
        List<DiarySummaryResponse> responses = diaries.stream()
                .map(DiarySummaryResponse::from)
                .collect(Collectors.toList());

        return DiaryListResponse.of(responses);
    }

    public DiaryDetailResponse getDiaryDetail(Long diaryId) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        DiarySummary diary = diaryMapper.selectDiaryById(diaryId);
        if (diary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary not found");
        }
        if (!diary.getAuthorId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this diary");
        }

        List<String> tags = toTagNameList(diaryMapper.selectDiaryTagsByDiaryId(diaryId));
        return DiaryDetailResponse.from(diary, tags);
    }

    public List<DiaryMonthlyCount> getMonthlyDiaryCounts(String month) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        return diaryMapper.selectDiaryMonthlyCounts(
                user.getUserId(),
                month);
    }

    public DiaryListResponse getDailyDiaryList(String day) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        List<DiarySummary> diaries = diaryMapper.selectDailyDiaryList(user.getUserId(), day);
        List<DiarySummaryResponse> responses = diaries.stream()
                .map(DiarySummaryResponse::from)
                .collect(Collectors.toList());

        return DiaryListResponse.of(responses);
    }

    @Transactional
    public DiaryCreateResponse createDiary(DiaryCreateRequest request) {
        // TODO : 사용자의 status가 BLOCKED 이거나 INACTIVE일 경우 return;

        User user = SecurityUtils.getCurrentUserOrThrow();

        Diary diary = new Diary();
        diary.setUserId(user.getUserId());
        diary.setTitle(request.getTitle());
        diary.setContentMd(request.getContentMd());
        diary.setDiaryDate(request.getDiaryDate());

        diaryMapper.insertDiary(diary);

        upsertDiaryTags(diary.getDiaryId(), request.getTags());

        return DiaryCreateResponse.of(diary.getDiaryId());
    }

    @Transactional
    public void editDiary(Long diaryId, DiaryEditRequest request) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        if (request.getDiaryId() != null && !diaryId.equals(request.getDiaryId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Diary id mismatch");
        }

        DiarySummary diary = diaryMapper.selectDiaryById(diaryId);
        if (diary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary not found");
        }
        if (!diary.getAuthorId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot edit this diary");
        }

        diaryMapper.updateDiary(diaryId, request.getTitle(), request.getContentMd(), request.getDiaryDate());

        diaryMapper.deleteDiaryTags(diaryId);
        upsertDiaryTags(diaryId, request.getTags());
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        DiarySummary diary = diaryMapper.selectDiaryById(diaryId);
        if (diary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary not found");
        }
        if (!diary.getAuthorId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete this diary");
        }

        diaryMapper.deleteDiaryTags(diaryId);
        diaryMapper.deleteDiary(diaryId);
    }

    private List<String> sanitizeTags(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return List.of();
        }

        Set<String> sanitized = new LinkedHashSet<>();
        for (String tag : rawTags) {
            String cleaned = sanitizeTag(tag);
            if (!cleaned.isEmpty()) {
                sanitized.add(cleaned);
            }
        }
        return List.copyOf(sanitized);
    }

    private String sanitizeTag(String tag) {
        if (tag == null) {
            return "";
        }

        String cleaned = tag.trim();
        cleaned = cleaned.replaceAll("<[^>]*>", "");
        cleaned = cleaned.replaceAll("(?i)script", "");
        cleaned = cleaned.replaceAll("[<>\"'`]", "");
        cleaned = cleaned.replaceAll("[^\\p{L}\\p{N}\\s_-]", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    private void upsertDiaryTags(Long diaryId, List<String> rawTags) {
        List<String> sanitizedTags = sanitizeTags(rawTags);
        for (String tagName : sanitizedTags) {
            Long tagId = diaryMapper.selectTagIdByName(tagName);
            if (tagId == null) {
                diaryMapper.insertTag(tagName);
                tagId = diaryMapper.selectTagIdByName(tagName);
            }
            if (tagId != null) {
                diaryMapper.insertDiaryTag(diaryId, tagId);
            }
        }
    }

    private List<String> toTagNameList(List<DiaryTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(DiaryTag::getTagName)
                .collect(Collectors.toList());
    }
}
