package heyso.HeysoDiaryBackEnd.diary.controller;

import heyso.HeysoDiaryBackEnd.diary.dto.DiarySummaryApiResponse;
import heyso.HeysoDiaryBackEnd.diary.service.DiarySummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diary")
public class DiarySummaryController {
    private final DiarySummaryService diarySummaryService;

    public DiarySummaryController(DiarySummaryService diarySummaryService) {
        this.diarySummaryService = diarySummaryService;
    }

    @GetMapping("/summary")
    public DiarySummaryApiResponse getDiarySummary() {
        return diarySummaryService.getMySummary();
    }
}
