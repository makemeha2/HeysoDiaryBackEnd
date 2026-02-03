package heyso.HeysoDiaryBackEnd.diaryAi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.diaryAi.service.DiaryAiService;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiCommentCreateResponse;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiCommentListItemResponse;
import heyso.HeysoDiaryBackEnd.diaryAi.dto.DiaryAiFeedbackCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/diary")
public class DiaryAiController {

    private final DiaryAiService diaryAiService;

    public DiaryAiController(DiaryAiService diaryAiService) {
        this.diaryAiService = diaryAiService;
    }

    // @PostMapping("/{diaryId}/ai-comment")
    // public ResponseEntity<DiaryAiCommentCreateResponse>
    // createAiComment(@PathVariable Long diaryId,
    // @Valid @RequestBody DiaryAiCommentCreateRequest request) {
    // DiaryAiCommentCreateResponse response =
    // diaryAiService.createAiComment(diaryId, request);
    // return ResponseEntity.status(201).body(response);
    // }

    @PostMapping("/{diaryId}/ai-comment")
    public ResponseEntity<DiaryAiCommentCreateResponse> createAiComment(@PathVariable Long diaryId) {
        DiaryAiCommentCreateResponse response = diaryAiService.createAiComment(diaryId);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{diaryId}/ai-comments")
    public ResponseEntity<List<DiaryAiCommentListItemResponse>> getAiComments(@PathVariable Long diaryId,
            @RequestParam(name = "limit", defaultValue = "10") @Min(1) Integer limit) {
        List<DiaryAiCommentListItemResponse> responses = diaryAiService.getAiComments(diaryId, limit);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/ai-comments/{aiCommentId}/feedback")
    public ResponseEntity<Void> createFeedback(@PathVariable Long aiCommentId,
            @Valid @RequestBody DiaryAiFeedbackCreateRequest request) {
        diaryAiService.createFeedback(aiCommentId, request);
        return ResponseEntity.ok().build();
    }
}
