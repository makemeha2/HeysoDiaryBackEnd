SET NAMES utf8mb4;
SET time_zone = '+09:00';

-- ======================================================
-- AI Chat tables
--  - 프로젝트 기존 규칙에 맞춰 tb_ 접두어 사용
--  - FK는 논리적 FK만 사용 (제약 미사용)
-- ======================================================

-- ------------------------------------------------------
-- tb_chat_conversation : 대화방(세션)
-- ------------------------------------------------------
CREATE TABLE tb_chat_conversation (
    conversation_id  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '대화방 PK',
    user_id          BIGINT UNSIGNED NOT NULL COMMENT 'tb_user.user_id (논리적 FK)',

    title            VARCHAR(200) NULL COMMENT '대화 제목',
    model            VARCHAR(50)  NOT NULL DEFAULT 'gpt-4o-mini' COMMENT '사용 모델',

    system_prompt    MEDIUMTEXT NULL COMMENT '시스템 프롬프트',

    is_deleted       TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    deleted_at       DATETIME NULL COMMENT '삭제 일시',

    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    PRIMARY KEY (conversation_id),
    KEY idx_tb_chat_conv_user_updated (user_id, updated_at),
    KEY idx_tb_chat_conv_user_created (user_id, created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='AI Chat 대화방';

-- ------------------------------------------------------
-- tb_chat_message : 대화 메시지
-- ------------------------------------------------------
CREATE TABLE tb_chat_message (
    message_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '메시지 PK',
    conversation_id   BIGINT UNSIGNED NOT NULL COMMENT 'tb_chat_conversation.conversation_id',

    role              ENUM('SYSTEM','USER','ASSISTANT','TOOL') NOT NULL COMMENT '메시지 역할',

    content           LONGTEXT NOT NULL COMMENT '메시지 본문',
    content_format    ENUM('text','markdown','json')
                      NOT NULL DEFAULT 'markdown' COMMENT '본문 포맷',

    token_count       INT NULL COMMENT '토큰 수',
    parent_message_id BIGINT UNSIGNED NULL COMMENT '부모 메시지 ID',
    client_message_id VARCHAR(64) NULL COMMENT '클라이언트 메시지 ID',

    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    PRIMARY KEY (message_id),
    KEY idx_tb_chat_msg_conv_created (conversation_id, created_at),
    KEY idx_tb_chat_msg_conv_role_created (conversation_id, role, created_at),
    KEY idx_tb_chat_msg_parent (parent_message_id),
    UNIQUE KEY uk_tb_chat_msg_client (conversation_id, client_message_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='AI Chat 메시지';

-- ------------------------------------------------------
-- tb_chat_conversation_summary : 대화 요약 메모리
-- ------------------------------------------------------
CREATE TABLE tb_chat_conversation_summary (
    conversation_id  BIGINT UNSIGNED NOT NULL COMMENT 'tb_chat_conversation.conversation_id',

    summary           MEDIUMTEXT NOT NULL COMMENT '대화 요약',
    summary_version   INT NOT NULL DEFAULT 1 COMMENT '요약 버전',
    last_message_id   BIGINT UNSIGNED NULL COMMENT '마지막 반영 메시지',

    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                      ON UPDATE CURRENT_TIMESTAMP COMMENT '갱신일시',

    PRIMARY KEY (conversation_id),
    KEY idx_tb_chat_summary_last_msg (last_message_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='AI Chat 대화 요약';

-- ------------------------------------------------------
-- tb_chat_usage_log : 모델 사용량 로그
-- ------------------------------------------------------
CREATE TABLE tb_chat_usage_log (
    usage_id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '사용량 로그 PK',

    user_id           BIGINT UNSIGNED NOT NULL COMMENT 'tb_user.user_id',
    conversation_id   BIGINT UNSIGNED NULL COMMENT 'tb_chat_conversation.conversation_id',

    request_id        VARCHAR(64) NULL COMMENT '요청 ID',
    model             VARCHAR(50) NOT NULL COMMENT '사용 모델',

    prompt_tokens     INT NOT NULL DEFAULT 0 COMMENT '입력 토큰',
    completion_tokens INT NOT NULL DEFAULT 0 COMMENT '출력 토큰',
    total_tokens      INT NOT NULL DEFAULT 0 COMMENT '총 토큰',

    cost_usd          DECIMAL(10,6) NULL COMMENT '비용(USD)',

    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    PRIMARY KEY (usage_id),
    KEY idx_tb_chat_usage_user_created (user_id, created_at),
    KEY idx_tb_chat_usage_conv_created (conversation_id, created_at),
    UNIQUE KEY uk_tb_chat_usage_request (request_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='AI Chat 사용량 로그';
