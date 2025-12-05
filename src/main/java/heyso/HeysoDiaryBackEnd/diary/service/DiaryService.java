package heyso.HeysoDiaryBackEnd.diary.service;

import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiarySummaryResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateResponse;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.diary.model.Diary;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryTag;
import heyso.HeysoDiaryBackEnd.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

        List<String> sanitizedTags = sanitizeTags(request.getTags());
        for (String tagName : sanitizedTags) {
            Long tagId = diaryMapper.selectTagIdByName(tagName);
            if (tagId == null) {
                diaryMapper.insertTag(tagName);
                tagId = diaryMapper.selectTagIdByName(tagName);
            }
            if (tagId != null) {
                diaryMapper.insertDiaryTag(diary.getDiaryId(), tagId);
            }
        }

        return DiaryCreateResponse.of(diary.getDiaryId());
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
}
