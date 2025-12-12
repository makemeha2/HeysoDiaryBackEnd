package heyso.HeysoDiaryBackEnd.diary.controller;

import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateResponse;
import heyso.HeysoDiaryBackEnd.diary.model.DiaryMonthlyCount;
import heyso.HeysoDiaryBackEnd.diary.service.DiaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @GetMapping
    public DiaryListResponse getDiaryList(@Valid @ModelAttribute DiaryListRequest request) {
        return diaryService.getDiaryList(request);
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

    @PostMapping
    public ResponseEntity<DiaryCreateResponse> createDiary(
            @Valid @RequestBody DiaryCreateRequest request) {
        DiaryCreateResponse response = diaryService.createDiary(request);
        return ResponseEntity.status(201).body(response);
    }
}
