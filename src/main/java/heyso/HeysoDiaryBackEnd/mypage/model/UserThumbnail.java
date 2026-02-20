package heyso.HeysoDiaryBackEnd.mypage.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserThumbnail {
    private Long userId;
    private String fileName;
    private String contentType;
    private byte[] imageBlob;
    private Integer bytes;
    private LocalDateTime updatedAt;
}
