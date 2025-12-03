package heyso.HeysoDiaryBackEnd.diary.controller;

import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryListResponse;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateRequest;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryCreateResponse;
import heyso.HeysoDiaryBackEnd.diary.service.DiaryService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping
    public ResponseEntity<DiaryCreateResponse> createDiary(
            @Valid @RequestBody DiaryCreateRequest request) {
        DiaryCreateResponse response = diaryService.createDiary(request);
        return ResponseEntity.status(201).body(response);
    }
}
