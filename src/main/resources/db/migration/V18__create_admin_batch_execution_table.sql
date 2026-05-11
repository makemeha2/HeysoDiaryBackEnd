SET NAMES utf8mb4;
SET time_zone = '+09:00';

CREATE TABLE tb_admin_batch_execution (
    execution_id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '관리자 배치 실행 이력 PK',
    batch_key          VARCHAR(100) NOT NULL COMMENT '코드 기반 배치 키',
    trigger_type       VARCHAR(20) NOT NULL COMMENT '실행 유형: AUTO, MANUAL',
    status             VARCHAR(20) NOT NULL COMMENT '실행 상태: RUNNING, SUCCESS, FAILED',
    requested_by       BIGINT UNSIGNED NULL COMMENT '수동 실행 요청 관리자 user_id',
    started_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '실행 시작 일시',
    finished_at        DATETIME NULL COMMENT '실행 종료 일시',
    duration_ms        BIGINT UNSIGNED NULL COMMENT '실행 소요 시간(ms)',
    success_count      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '성공 처리 건수',
    failure_count      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '실패 처리 건수',
    message            VARCHAR(500) NULL COMMENT '실행 결과 메시지',
    error_message      VARCHAR(1000) NULL COMMENT '관리자 표시용 오류 메시지',
    running_batch_key  VARCHAR(100) AS (CASE WHEN status = 'RUNNING' THEN batch_key ELSE NULL END) STORED COMMENT 'RUNNING 중복 방지 키',
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    PRIMARY KEY (execution_id),
    UNIQUE KEY uk_admin_batch_execution_running (running_batch_key),
    KEY idx_admin_batch_execution_batch_started (batch_key, started_at DESC, execution_id DESC),
    KEY idx_admin_batch_execution_status (status),
    KEY idx_admin_batch_execution_requested_by (requested_by),
    CONSTRAINT fk_admin_batch_execution_requested_by
        FOREIGN KEY (requested_by) REFERENCES tb_user (user_id),
    CONSTRAINT chk_admin_batch_execution_trigger_type
        CHECK (trigger_type IN ('AUTO', 'MANUAL')),
    CONSTRAINT chk_admin_batch_execution_status
        CHECK (status IN ('RUNNING', 'SUCCESS', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 배치 실행 이력';
