package heyso.HeysoDiaryBackEnd.mypage.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heyso.HeysoDiaryBackEnd.mypage.dto.MyPageResponse;
import heyso.HeysoDiaryBackEnd.mypage.dto.MyPageUpdateRequest;
import heyso.HeysoDiaryBackEnd.mypage.model.UserThumbnail;
import heyso.HeysoDiaryBackEnd.mypage.service.MyPageService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/mypage")
public class MyPageController {
    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping
    public MyPageResponse getMyPage() {
        return myPageService.getMyPage();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateMyPage(@Valid @ModelAttribute MyPageUpdateRequest request) {
        myPageService.updateMyPage(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/thumbnail")
    public ResponseEntity<byte[]> getMyThumbnail() {
        UserThumbnail thumbnail = myPageService.getMyThumbnail();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(resolveMediaType(thumbnail.getContentType()));
        if (thumbnail.getFileName() != null && !thumbnail.getFileName().isBlank()) {
            headers.setContentDispositionFormData("inline", thumbnail.getFileName());
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(thumbnail.getImageBlob());
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMimeTypeException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
