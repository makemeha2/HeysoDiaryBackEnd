-- =========================================================
-- V2__1_create_diary_ai_tables.sql
-- 다이어리 AI 멘토 기능 관련 테이블 생성
-- =========================================================

/* =========================================================
 * 1. tb_diary_ai_run
 *  - AI 실행 단위(버튼 클릭 1회 = 1 run)
 * ========================================================= */
CREATE TABLE tb_diary_ai_run (
    run_id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'AI 실행 PK',

    diary_id            BIGINT UNSIGNED NOT NULL COMMENT '대상 일기 ID (tb_diary.diary_id)',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '사용자 ID (tb_user.user_id)',

    trigger_type        ENUM('BUTTON','AUTO') NOT NULL DEFAULT 'BUTTON' COMMENT '실행 트리거',
    status              ENUM('RUNNING','SUCCESS','ERROR') NOT NULL DEFAULT 'RUNNING' COMMENT '실행 상태',

    model               VARCHAR(50) NOT NULL COMMENT '사용 모델',
    temperature         DECIMAL(4,3) NULL COMMENT 'temperature',
    top_p               DECIMAL(4,3) NULL COMMENT 'top_p',
    max_output_tokens   INT NULL COMMENT '최대 출력 토큰',

    request_id          VARCHAR(64) NULL COMMENT 'LLM provider request id',

    prompt_system       MEDIUMTEXT NULL COMMENT '시스템 프롬프트 (요약/규칙/컨텍스트)',
    prompt_user         MEDIUMTEXT NULL COMMENT '유저 프롬프트 (오늘 일기 + 요청)',

    prompt_hash         CHAR(64) NULL COMMENT '프롬프트 해시 (중복/재현 방지용)',
    diary_updated_at_snapshot DATETIME NULL COMMENT '실행 당시 diary.updated_at',

    prompt_tokens       INT NOT NULL DEFAULT 0 COMMENT '입력 토큰 수',
    completion_tokens  INT NOT NULL DEFAULT 0 COMMENT '출력 토큰 수',
    total_tokens        INT NOT NULL DEFAULT 0 COMMENT '총 토큰 수',
    cost_usd            DECIMAL(10,6) NULL COMMENT '비용 (USD)',

    error_code          VARCHAR(50) NULL COMMENT '에러 코드',
    error_message       TEXT NULL COMMENT '에러 메시지',

    started_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '실행 시작 시각',
    finished_at         DATETIME NULL COMMENT '실행 종료 시각',

    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (run_id),
    KEY idx_diary_ai_run_diary (diary_id, created_at),
    KEY idx_diary_ai_run_user (user_id, created_at),
    KEY idx_diary_ai_run_request (request_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='다이어리 AI 실행 로그';


/* =========================================================
 * 2. tb_diary_ai_run_context
 *  - AI 실행 시 사용된 컨텍스트(연관/최근 일기)
 * ========================================================= */
CREATE TABLE tb_diary_ai_run_context (
    run_context_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '컨텍스트 PK',
    run_id              BIGINT UNSIGNED NOT NULL COMMENT 'AI 실행 ID (tb_diary_ai_run.run_id)',

    source_diary_id     BIGINT UNSIGNED NOT NULL COMMENT '참고한 일기 ID (tb_diary.diary_id)',
    source_type         ENUM('RECENT','TAG','SIMILAR','MANUAL') NOT NULL COMMENT '컨텍스트 타입',

    sort_order          INT NOT NULL DEFAULT 0 COMMENT '프롬프트에 포함된 순서',
    score               DECIMAL(10,6) NULL COMMENT '유사도/가중치 점수 (선택)',

    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (run_context_id),
    KEY idx_run_context_run (run_id, sort_order),
    KEY idx_run_context_source_diary (source_diary_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='다이어리 AI 실행 컨텍스트';


/* =========================================================
 * 3. tb_diary_ai_comment
 *  - AI가 생성한 실제 댓글
 * ========================================================= */
CREATE TABLE tb_diary_ai_comment (
    ai_comment_id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'AI 댓글 PK',

    diary_id            BIGINT UNSIGNED NOT NULL COMMENT '대상 일기 ID (tb_diary.diary_id)',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '사용자 ID (tb_user.user_id)',
    run_id              BIGINT UNSIGNED NULL COMMENT 'AI 실행 ID (tb_diary_ai_run.run_id)',

    content_md          MEDIUMTEXT NOT NULL COMMENT 'AI 댓글 내용 (Markdown)',
    is_pinned           TINYINT(1) NOT NULL DEFAULT 0 COMMENT '대표 댓글 여부',

    is_deleted          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    deleted_at          DATETIME NULL COMMENT '삭제 시각',

    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (ai_comment_id),
    KEY idx_ai_comment_diary (diary_id, created_at),
    KEY idx_ai_comment_run (run_id),
    KEY idx_ai_comment_user (user_id, created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='다이어리 AI 댓글';


/* =========================================================
 * 4. tb_diary_ai_feedback
 *  - AI 댓글에 대한 사용자 피드백
 * ========================================================= */
CREATE TABLE tb_diary_ai_feedback (
    feedback_id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '피드백 PK',

    ai_comment_id       BIGINT UNSIGNED NOT NULL COMMENT 'AI 댓글 ID (tb_diary_ai_comment.ai_comment_id)',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '사용자 ID (tb_user.user_id)',

    feedback_type       ENUM('LIKE','DISLIKE','NEUTRAL') NOT NULL COMMENT '피드백 유형',
    feedback_reason     VARCHAR(255) NULL COMMENT '피드백 사유(선택)',

    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (feedback_id),
    UNIQUE KEY uk_ai_feedback_unique (ai_comment_id, user_id),
    KEY idx_ai_feedback_comment (ai_comment_id),
    KEY idx_ai_feedback_user (user_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='다이어리 AI 댓글 피드백';
