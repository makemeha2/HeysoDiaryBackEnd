package heyso.HeysoDiaryBackEnd.diaryAiPolish.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.diaryAiPolish.dto.DiaryAiPolishRequest;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.dto.DiaryAiPolishResponse;
import heyso.HeysoDiaryBackEnd.diaryAiPolish.service.DiaryAiPolishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diary-ai-polish")
public class DiaryAiPolishController {

    private final DiaryAiPolishService diaryAiPolishService;

    @PostMapping("/request")
    public ResponseEntity<DiaryAiPolishResponse> requestPolish(@Valid @RequestBody DiaryAiPolishRequest request) {
        DiaryAiPolishResponse response = diaryAiPolishService.requestPolish(request);
        return ResponseEntity.status(201).body(response);
    }
}
