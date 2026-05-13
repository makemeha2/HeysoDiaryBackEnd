package heyso.HeysoDiaryBackEnd.diaryAnalysis.type;

public final class DiaryAnalysisErrorCode {
    public static final String CONTENT_CHANGED = "CONTENT_CHANGED";
    public static final String PROMPT_BINDING_NOT_FOUND = "PROMPT_BINDING_NOT_FOUND";
    public static final String AI_CALL_FAILED = "AI_CALL_FAILED";
    public static final String AI_EMPTY_RESPONSE = "AI_EMPTY_RESPONSE";
    public static final String JSON_PARSE_FAILED = "JSON_PARSE_FAILED";
    public static final String SCHEMA_INVALID = "SCHEMA_INVALID";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private DiaryAnalysisErrorCode() {
    }
}
