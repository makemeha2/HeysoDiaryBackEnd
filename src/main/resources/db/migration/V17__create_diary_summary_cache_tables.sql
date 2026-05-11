SET NAMES utf8mb4;
SET time_zone = '+09:00';

CREATE TABLE tb_diary_summary_cache (
    user_id              BIGINT UNSIGNED NOT NULL COMMENT '사용자 PK',
    total_diary_count    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '삭제되지 않은 전체 일기 수',
    current_streak_days  INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '현재 연속 작성일',
    last_diary_date      DATE NULL COMMENT '마지막 일기 작성일(diary_date 기준)',
    generated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '캐시 생성/갱신 일시',
    dirty                TINYINT(1) NOT NULL DEFAULT 1 COMMENT '재집계 필요 여부',

    PRIMARY KEY (user_id),
    KEY idx_diary_summary_cache_dirty (dirty),
    KEY idx_diary_summary_cache_generated_at (generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일기 요약 사용자별 캐시';

CREATE TABLE tb_diary_tag_summary_cache (
    user_id       BIGINT UNSIGNED NOT NULL COMMENT '사용자 PK',
    period_type   VARCHAR(10) NOT NULL COMMENT '기간 유형: ALL, YEAR',
    period_key    VARCHAR(10) NOT NULL COMMENT '기간 키: ALL 또는 yyyy',
    tag           VARCHAR(50) NOT NULL COMMENT '태그명',
    tag_count     BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '태그 빈도',
    rank_no       INT UNSIGNED NOT NULL COMMENT '기간 내 순위',
    generated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '캐시 생성/갱신 일시',

    PRIMARY KEY (user_id, period_type, period_key, rank_no),
    KEY idx_diary_tag_summary_cache_user_period (user_id, period_type, period_key),
    KEY idx_diary_tag_summary_cache_tag (tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일기 태그 순위 캐시';
