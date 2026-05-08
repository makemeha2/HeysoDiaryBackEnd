CREATE TABLE tb_ai_quota_daily_usage (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    usage_date    DATE         NOT NULL COMMENT 'Asia/Seoul 기준',
    used_count    INT          NOT NULL DEFAULT 0,
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_quota_usage_user_date (user_id, usage_date),
    KEY idx_ai_quota_usage_date (usage_date)
) COMMENT='사용자별 일일 AI 통합 사용량';

CREATE TABLE tb_ai_quota_grant (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    grant_date     DATE         NOT NULL COMMENT '부여 대상 일자',
    amount         INT          NOT NULL,
    source_type    VARCHAR(30)  NOT NULL COMMENT 'ADMIN | AD_VIEW | STREAK | NEW_USER',
    source_ref     VARCHAR(100) NULL     COMMENT '관리자 ID, 광고 ID 등',
    reason         VARCHAR(500) NULL,
    is_active      TINYINT(1)   NOT NULL DEFAULT 1,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ai_quota_grant_user_date (user_id, grant_date),
    KEY idx_ai_quota_grant_source (source_type)
) COMMENT='AI 사용량 추가 부여';

CREATE TABLE tb_ai_quota_usage_log (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    usage_date     DATE         NOT NULL,
    feature_type   VARCHAR(20)  NOT NULL COMMENT 'POLISH | AI_COMMENT',
    feature_ref_id BIGINT       NULL     COMMENT 'polish_log_id 또는 run_id',
    status         VARCHAR(20)  NOT NULL COMMENT 'SUCCESS | FAILED | RELEASED',
    is_active      TINYINT(1)   NOT NULL DEFAULT 1,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ai_quota_log_user_date (user_id, usage_date),
    KEY idx_ai_quota_log_feature (feature_type)
) COMMENT='AI 사용 이력 로그';

DROP TABLE IF EXISTS tb_diary_ai_polish_daily_usage;
