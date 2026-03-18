package heyso.HeysoDiaryBackEnd.monitoring.support;

public enum MonitoringEventCode {
    // 처리되지 않은 서버 예외
    SYS_UNEXPECTED_ERROR,
    // DB 접근/쿼리 처리 실패
    SYS_DB_ACCESS_ERROR,
    // 외부 API 호출 실패
    SYS_EXTERNAL_API_ERROR,
    // 권한 없는 접근 시도
    SEC_FORBIDDEN_ACCESS_ATTEMPT,
    // 관리자 API 비정상 접근 패턴
    SEC_ADMIN_API_ANOMALY,
    // 유효하지 않은 토큰 사용
    SEC_INVALID_TOKEN,
    // 허용되지 않은 상태 전이 요청
    BIZ_INVALID_STATE_TRANSITION,
    // 삭제된 리소스 접근 시도
    BIZ_DELETED_RESOURCE_ACCESS,
    // 성공 처리됐지만 의심스러운 비즈니스 이벤트
    BIZ_SUSPICIOUS_SUCCESS,
    // 기본값
    UNKNOWN_EVENT
}
