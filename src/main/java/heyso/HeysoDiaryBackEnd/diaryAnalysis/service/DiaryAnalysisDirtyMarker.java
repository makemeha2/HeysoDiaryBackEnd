package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import heyso.HeysoDiaryBackEnd.diaryAnalysis.mapper.DiaryAnalysisMapper;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import heyso.HeysoDiaryBackEnd.utils.JsonHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryAnalysisDirtyMarker {
    private final DiaryAnalysisMapper diaryAnalysisMapper;

    @Transactional
    public void markDirty(Long diaryId, Long userId, String title, String contentMd,
            LocalDate diaryDate, String moodId, List<String> tags) {
        String contentHash = buildContentHash(title, contentMd, diaryDate, moodId, tags);
        diaryAnalysisMapper.markDiaryAnalysisDirty(diaryId, userId, contentHash);
    }

    @Transactional
    public void markStale(Long diaryId, Long userId) {
        diaryAnalysisMapper.markDiaryAnalysisStale(diaryId, userId);
    }

    String buildContentHash(String title, String contentMd, LocalDate diaryDate, String moodId, List<String> tags) {
        String canonicalText = canonical(title)
                + "\n" + canonical(contentMd)
                + "\n" + (diaryDate == null ? "" : diaryDate)
                + "\n" + canonical(moodId)
                + "\n" + canonicalTags(tags);
        return JsonHashUtil.sha256Hex(canonicalText);
    }

    private String canonical(String value) {
        return value == null ? "" : value.trim();
    }

    private String canonicalTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining("|"));
    }

}
