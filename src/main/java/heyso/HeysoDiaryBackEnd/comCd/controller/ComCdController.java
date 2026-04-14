package heyso.HeysoDiaryBackEnd.comCd.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeGroupResponse;
import heyso.HeysoDiaryBackEnd.comCd.dto.CommonCodeResponse;
import heyso.HeysoDiaryBackEnd.comCd.service.ComCdService;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/comCd")
public class ComCdController {
    private final ComCdService comCdService;

    public ComCdController(ComCdService comCdService) {
        this.comCdService = comCdService;
    }

    @GetMapping("/groups")
    public List<CommonCodeGroupResponse> getActiveGroupList() {
        return comCdService.getActiveGroupList();
    }

    @GetMapping("/groups/{groupId}/codes")
    public List<CommonCodeResponse> getActiveCodeListByGroupId(
            @PathVariable @Size(max = 30) String groupId) {
        return comCdService.getActiveCodeListByGroupId(groupId);
    }
}
