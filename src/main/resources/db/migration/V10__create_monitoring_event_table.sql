/* =========================================================
   Monitoring Event Table
   - 시스템 오류
   - 보안 이상징후
   - 비즈니스 특이 이벤트
   ========================================================= */

CREATE TABLE tb_monitoring_event (
    event_id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '모니터링 이벤트 PK',

    event_type            VARCHAR(30) NOT NULL COMMENT '이벤트 유형 (ERROR/WARN/INFO/SECURITY/BUSINESS)',
    event_code            VARCHAR(100) NOT NULL COMMENT '이벤트 코드 (SYS_UNEXPECTED_ERROR 등)',
    severity              VARCHAR(20) NOT NULL COMMENT '심각도 (LOW/MEDIUM/HIGH/CRITICAL)',

    title                 VARCHAR(200) NOT NULL COMMENT '짧은 제목',
    message               TEXT NULL COMMENT '운영자용 설명',
    detail_json           JSON NULL COMMENT '추가 상세 정보(JSON)',

    http_method           VARCHAR(10) NULL COMMENT 'HTTP Method',
    request_uri           VARCHAR(500) NULL COMMENT '요청 URI',
    query_string          VARCHAR(1000) NULL COMMENT 'Query String',
    client_ip             VARCHAR(45) NULL COMMENT '클라이언트 IP',
    user_agent            VARCHAR(500) NULL COMMENT 'User-Agent',

    user_id               BIGINT UNSIGNED NULL COMMENT '사용자 ID',
    user_role             VARCHAR(50) NULL COMMENT '사용자 권한',
    trace_id              VARCHAR(100) NULL COMMENT '추적 ID',
    session_id            VARCHAR(100) NULL COMMENT '세션 ID',

    exception_class       VARCHAR(300) NULL COMMENT '예외 클래스명',
    exception_message     TEXT NULL COMMENT '예외 메시지',
    stack_trace           MEDIUMTEXT NULL COMMENT '스택 트레이스',

    source_class          VARCHAR(300) NULL COMMENT '발생 클래스',
    source_method         VARCHAR(200) NULL COMMENT '발생 메서드',

    resolved_yn           CHAR(1) NOT NULL DEFAULT 'N' COMMENT '조치 여부',
    resolved_at           DATETIME NULL COMMENT '조치 시각',
    resolved_by           BIGINT UNSIGNED NULL COMMENT '조치 사용자 ID',

    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각'
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='시스템 오류 및 특이 이벤트 모니터링 로그';


/* =========================================================
   Index
   ========================================================= */

CREATE INDEX idx_monitoring_event_created_at
    ON tb_monitoring_event (created_at DESC);

CREATE INDEX idx_monitoring_event_event_type
    ON tb_monitoring_event (event_type);

CREATE INDEX idx_monitoring_event_event_code
    ON tb_monitoring_event (event_code);

CREATE INDEX idx_monitoring_event_severity
    ON tb_monitoring_event (severity);

CREATE INDEX idx_monitoring_event_user_id
    ON tb_monitoring_event (user_id);

CREATE INDEX idx_monitoring_event_request_uri
    ON tb_monitoring_event (request_uri(255));

CREATE INDEX idx_monitoring_event_resolved_yn_created_at
    ON tb_monitoring_event (resolved_yn, created_at DESC);

CREATE INDEX idx_monitoring_event_trace_id
    ON tb_monitoring_event (trace_id);