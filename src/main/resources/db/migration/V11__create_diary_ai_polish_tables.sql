-- V11__create_diary_ai_polish_tables.sql

-- =========================================
-- 1. 일일 사용량 테이블
-- =========================================
CREATE TABLE tb_diary_ai_polish_daily_usage (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    usage_date DATE NOT NULL COMMENT '사용일자 (서버 기준)',
    
    quota_limit INT NOT NULL DEFAULT 3 COMMENT '일일 제한 횟수',
    used_count INT NOT NULL DEFAULT 0 COMMENT '사용 횟수',
    
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_polish_daily_usage_user_date (user_id, usage_date),
    KEY idx_ai_polish_daily_usage_date (usage_date)
) COMMENT='사용자별 일일 AI 글다듬기 사용량';



-- =========================================
-- 2. 글다듬기 요청 로그 테이블
-- =========================================
CREATE TABLE tb_diary_ai_polish_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    diary_id BIGINT NULL COMMENT '일기 ID (저장 전이면 NULL)',
    
    request_text_length INT NOT NULL COMMENT '요청 시 본문 길이',
    
    request_status VARCHAR(20) NOT NULL COMMENT 'REQUESTED / SUCCESS / FAILED',
    fail_reason_code VARCHAR(50) NULL COMMENT '실패 코드',
    
    used_quota_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '차감 여부 (Y/N)',
    
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '요청 시각',
    completed_at DATETIME NULL COMMENT '완료 시각',
    
    PRIMARY KEY (id),
    KEY idx_ai_polish_log_user_created (user_id, created_at),
    KEY idx_ai_polish_log_diary (diary_id),
    KEY idx_ai_polish_log_status (request_status)
) COMMENT='AI 글다듬기 요청 로그';



-- =========================================
-- 3. 글다듬기 결과 이력 테이블
-- =========================================
CREATE TABLE tb_diary_ai_polish_result (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
    polish_log_id BIGINT NOT NULL COMMENT '로그 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    diary_id BIGINT NULL COMMENT '일기 ID (저장 전이면 NULL)',

    original_content MEDIUMTEXT NOT NULL COMMENT '원본 내용',
    polished_content MEDIUMTEXT NOT NULL COMMENT 'AI 결과 내용',

    applied_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '적용 여부 (Y/N)',
    saved_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '저장 여부 (Y/N)',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    applied_at DATETIME NULL COMMENT '적용 시각',
    saved_at DATETIME NULL COMMENT '저장 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_polish_result_log (polish_log_id),
    KEY idx_ai_polish_result_user_created (user_id, created_at),
    KEY idx_ai_polish_result_diary (diary_id)
) COMMENT='AI 글다듬기 결과 이력';