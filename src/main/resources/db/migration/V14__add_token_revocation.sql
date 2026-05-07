-- Server-side JWT revocation support.
-- - tb_user.token_revoked_after invalidates all tokens issued at or before that time.
-- - tb_auth_token_denylist invalidates a single access token by JWT jti.

ALTER TABLE tb_user
    ADD COLUMN token_revoked_after DATETIME NULL COMMENT '이 시각 이전 발급 JWT 무효화 기준';

CREATE INDEX idx_tb_user_token_revoked_after
    ON tb_user (token_revoked_after);

CREATE TABLE tb_auth_token_denylist (
    jti             VARCHAR(64) NOT NULL COMMENT 'JWT ID',
    user_id         BIGINT UNSIGNED NOT NULL COMMENT '사용자 PK',
    expires_at      DATETIME NOT NULL COMMENT 'JWT 만료 시각',
    revoked_reason  VARCHAR(50) NOT NULL COMMENT '폐기 사유',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    PRIMARY KEY (jti),
    KEY idx_auth_token_denylist_user_id (user_id),
    KEY idx_auth_token_denylist_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='JWT access token 폐기 목록';
