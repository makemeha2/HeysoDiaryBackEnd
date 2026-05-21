package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisDetailResponse;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisPageResponse;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisRow;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisSearchRequest;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryContentResponse;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryReanalysisResponse;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.service.AdminDiaryAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/admin/diary-analysis/diaries")
@RequiredArgsConstructor
public class AdminDiaryAnalysisController {
    private final AdminDiaryAnalysisService adminDiaryAnalysisService;

    @GetMapping
    public ResponseEntity<AdminDiaryAnalysisPageResponse> getDiaryPage(
            @Valid @ModelAttribute AdminDiaryAnalysisSearchRequest request) {
        return ResponseEntity.ok(adminDiaryAnalysisService.getDiaryPage(request));
    }

    @GetMapping("/{diaryId}")
    public ResponseEntity<AdminDiaryAnalysisDetailResponse> getDiaryDetail(@PathVariable Long diaryId) {
        return ResponseEntity.ok(adminDiaryAnalysisService.getDiaryDetail(diaryId));
    }

    @GetMapping("/{diaryId}/content")
    public ResponseEntity<AdminDiaryContentResponse> getDiaryContent(@PathVariable Long diaryId) {
        return ResponseEntity.ok(adminDiaryAnalysisService.getDiaryContent(diaryId));
    }

    @GetMapping("/{diaryId}/analyses")
    public ResponseEntity<List<AdminDiaryAnalysisRow>> getAnalyses(@PathVariable Long diaryId) {
        return ResponseEntity.ok(adminDiaryAnalysisService.getAnalyses(diaryId));
    }

    @PostMapping("/{diaryId}/reanalyze")
    public ResponseEntity<AdminDiaryReanalysisResponse> requestReanalysis(@PathVariable Long diaryId) {
        return ResponseEntity.ok(adminDiaryAnalysisService.requestReanalysis(diaryId));
    }
}
