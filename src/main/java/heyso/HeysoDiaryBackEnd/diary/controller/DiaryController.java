package heyso.HeysoDiaryBackEnd.diary.controller;

import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryEditRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryDetailResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryNudgeResponse;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryMonthlyCount;
import heyso.HeysoDiaryBackEnd.diary.service.DiaryNudgeService;
import heyso.HeysoDiaryBackEnd.diary.service.DiaryService;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryNudgeService diaryNudgeService;

    public DiaryController(DiaryService diaryService, DiaryNudgeService diaryNudgeService) {
        this.diaryService = diaryService;
        this.diaryNudgeService = diaryNudgeService;
    }

    @GetMapping
    public DiaryListResponse getDiaryList(@Valid @ModelAttribute DiaryListRequest request) {
        return diaryService.getDiaryList(request);
    }

    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryDetailResponse> getDiaryDetail(@PathVariable Long diaryId) {
        DiaryDetailResponse response = diaryService.getDiaryDetail(diaryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly")
    public List<DiaryMonthlyCount> getMonthlyDiaryCounts(
            @RequestParam("month") @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be in yyyy-MM format") String month) {
        return diaryService.getMonthlyDiaryCounts(month);
    }

    @GetMapping("/daily")
    public DiaryListResponse getDailyDiaryList(
            @RequestParam("day") @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "day must be in yyyy-MM-dd format") String day) {
        return diaryService.getDailyDiaryList(day);
    }

    @GetMapping("/mytags")
    public List<String> getTagNamesByUserId() {
        return diaryService.getTagNamesByUserId();
    }

    @PostMapping
    public ResponseEntity<DiaryCreateResponse> createDiary(
            @Valid @RequestBody DiaryCreateRequest request) {
        DiaryCreateResponse response = diaryService.createDiary(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/{diaryId}/edit")
    public ResponseEntity<Void> editDiary(
            @PathVariable Long diaryId,
            @Valid @RequestBody DiaryEditRequest request) {
        diaryService.editDiary(diaryId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{diaryId}/delete")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/diary-nudge/today")
    public CompletableFuture<ResponseEntity<DiaryNudgeResponse>> getTodayDiaryNudge() {
        Long userId = SecurityUtils.getCurrentUserOrThrow().getUserId();
        return diaryNudgeService.createTodayNudgeAsync(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    // 반드시 로그 남기기
                    log.error("createTodayNudgeAsync failed", ex);

                    // 원하는 상태코드로 내려주기 (예: 500)
                    return ResponseEntity
                            .status(500)
                            .body(new DiaryNudgeResponse("", "잠시 후 다시 시도해 주세요.")); // DTO에 맞게
                });

    }
}
