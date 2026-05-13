package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import lombok.Getter;

@Getter
public class DiaryAnalysisException extends RuntimeException {
    private final String errorCode;

    public DiaryAnalysisException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DiaryAnalysisException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
