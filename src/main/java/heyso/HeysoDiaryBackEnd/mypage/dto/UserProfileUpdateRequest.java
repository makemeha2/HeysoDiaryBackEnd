package heyso.HeysoDiaryBackEnd.mypage.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {
    @NotBlank
    @Size(max = 50)
    private String nickname;

    @Size(max = 4)
    @Pattern(regexp = "^$|^[A-Za-z]{4}$", message = "mbti must be 4 letters")
    private String mbti;

    private MultipartFile thumbnail;
}
