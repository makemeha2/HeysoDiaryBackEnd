package heyso.HeysoDiaryBackEnd.aiQuota.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.aiQuota.dto.AiQuotaStatusResponse;
import heyso.HeysoDiaryBackEnd.aiQuota.service.AiQuotaService;
import heyso.HeysoDiaryBackEnd.auth.util.SecurityUtils;
import heyso.HeysoDiaryBackEnd.user.model.User;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-quota")
public class AiQuotaController {

    private final AiQuotaService aiQuotaService;

    @GetMapping("/today")
    public ResponseEntity<AiQuotaStatusResponse> getTodayStatus() {
        User user = SecurityUtils.getCurrentUserOrThrow();
        return ResponseEntity.ok(aiQuotaService.getStatus(user.getUserId()));
    }
}
