package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.auth.service.AdminAuthorizationService;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.DiarySummary;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.service.DiaryAnalysisDirtyMarker;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisDetailResponse;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisDiaryRow;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisPageResponse;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisRow;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisSearchRequest;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryContentResponse;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryReanalysisResponse;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.mapper.AdminDiaryAnalysisMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDiaryAnalysisService {
    private final AdminAuthorizationService adminAuthorizationService;
    private final AdminDiaryAnalysisMapper adminDiaryAnalysisMapper;
    private final DiaryMapper diaryMapper;
    private final DiaryAnalysisDirtyMarker diaryAnalysisDirtyMarker;

    @Transactional(readOnly = true)
    public AdminDiaryAnalysisPageResponse getDiaryPage(AdminDiaryAnalysisSearchRequest request) {
        adminAuthorizationService.requireAdminUser();
        List<AdminDiaryAnalysisDiaryRow> items = adminDiaryAnalysisMapper.selectDiaryAnalysisPage(request);
        long totalCount = adminDiaryAnalysisMapper.countDiaryAnalysisPage(request);
        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / request.getSize());

        return AdminDiaryAnalysisPageResponse.builder()
                .items(items)
                .page(request.getPage())
                .size(request.getSize())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDiaryAnalysisDetailResponse getDiaryDetail(Long diaryId) {
        adminAuthorizationService.requireAdminUser();
        var diary = requireDiaryAnalysisRow(diaryId);
        return AdminDiaryAnalysisDetailResponse.builder()
                .diary(diary)
                .events(adminDiaryAnalysisMapper.selectActiveEvents(diaryId))
                .traitEvidence(adminDiaryAnalysisMapper.selectActiveTraitEvidence(diaryId))
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDiaryContentResponse getDiaryContent(Long diaryId) {
        adminAuthorizationService.requireAdminUser();
        DiarySummary diary = requireDiary(diaryId);
        List<String> tags = diaryMapper.selectTagNamesByDiaryId(diaryId);
        return AdminDiaryContentResponse.builder()
                .diaryId(diary.getDiaryId())
                .userId(diary.getAuthorId())
                .authorNickname(diary.getAuthorNickname())
                .title(diary.getTitle())
                .contentMd(diary.getContentMd())
                .diaryDate(diary.getDiaryDate())
                .moodId(diary.getMoodId())
                .tags(tags)
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminDiaryAnalysisRow> getAnalyses(Long diaryId) {
        adminAuthorizationService.requireAdminUser();
        requireDiary(diaryId);
        return adminDiaryAnalysisMapper.selectAnalyses(diaryId);
    }

    @Transactional
    public AdminDiaryReanalysisResponse requestReanalysis(Long diaryId) {
        adminAuthorizationService.requireAdminUser();
        DiarySummary diary = requireDiary(diaryId);
        List<String> tags = diaryMapper.selectTagNamesByDiaryId(diaryId);

        diaryAnalysisDirtyMarker.markDirty(
                diary.getDiaryId(),
                diary.getAuthorId(),
                diary.getTitle(),
                diary.getContentMd(),
                diary.getDiaryDate(),
                diary.getMoodId(),
                tags);
        adminDiaryAnalysisMapper.markReanalysisEligible(diaryId);

        return AdminDiaryReanalysisResponse.builder()
                .diaryId(diaryId)
                .analysisStatus("DIRTY")
                .dirty(true)
                .message("재분석 요청이 등록되었습니다.")
                .build();
    }

    private DiarySummary requireDiary(Long diaryId) {
        DiarySummary diary = diaryMapper.selectDiaryById(diaryId);
        if (diary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary not found");
        }
        return diary;
    }

    private AdminDiaryAnalysisDiaryRow requireDiaryAnalysisRow(Long diaryId) {
        var diary = adminDiaryAnalysisMapper.selectDiaryAnalysisDetail(diaryId);
        if (diary == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diary not found");
        }
        return diary;
    }
}
