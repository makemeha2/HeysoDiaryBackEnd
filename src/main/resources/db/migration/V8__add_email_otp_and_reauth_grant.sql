/* ============================================================
   Email OTP + Re-auth Grant tables
   For sensitive operations (ex: ACCOUNT_DELETE)
   ============================================================ */

CREATE TABLE tb_email_otp (
    otp_id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'OTP PK',

    user_id           BIGINT UNSIGNED NOT NULL COMMENT '사용자 ID',
    purpose           VARCHAR(50) NOT NULL COMMENT 'OTP 목적 (ACCOUNT_DELETE 등)',

    email             VARCHAR(255) NOT NULL COMMENT 'OTP 발송 이메일',

    otp_hash          VARCHAR(255) NOT NULL COMMENT 'OTP 해시값',

    expires_at        DATETIME NOT NULL COMMENT 'OTP 만료시간',

    verified_at       DATETIME NULL COMMENT 'OTP 검증 성공 시각',
    consumed_at       DATETIME NULL COMMENT '실제 작업에 사용된 시각',

    send_status       VARCHAR(20) NOT NULL DEFAULT 'SENT' COMMENT '메일 발송 상태 (PENDING/SENT/FAILED)',

    fail_count        INT NOT NULL DEFAULT 0 COMMENT 'OTP 검증 실패 횟수',
    resend_count      INT NOT NULL DEFAULT 0 COMMENT 'OTP 재발송 횟수',

    last_sent_at      DATETIME NOT NULL COMMENT '마지막 발송 시각',

    request_ip        VARCHAR(45) NULL COMMENT '요청 IP',
    request_ua        VARCHAR(500) NULL COMMENT '요청 User-Agent',

    verify_ip         VARCHAR(45) NULL COMMENT '검증 IP',
    verify_ua         VARCHAR(500) NULL COMMENT '검증 User-Agent',

    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성시간',
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정시간',

    CONSTRAINT fk_email_otp_user
        FOREIGN KEY (user_id)
        REFERENCES tb_user(user_id)
) ENGINE=InnoDB COMMENT='이메일 OTP 인증 관리 테이블';


/* ============================================================
   Index
   ============================================================ */

CREATE INDEX idx_email_otp_user_purpose_created
    ON tb_email_otp (user_id, purpose, created_at DESC);

CREATE INDEX idx_email_otp_user_purpose_verified
    ON tb_email_otp (user_id, purpose, verified_at);

CREATE INDEX idx_email_otp_expires
    ON tb_email_otp (expires_at);



/* ============================================================
   Re-auth Grant
   ============================================================ */

CREATE TABLE tb_reauth_grant (
    grant_id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '재인증 권한 PK',

    user_id           BIGINT UNSIGNED NOT NULL COMMENT '사용자 ID',

    purpose           VARCHAR(50) NOT NULL COMMENT '재인증 목적 (ACCOUNT_DELETE 등)',

    granted_by_type   VARCHAR(30) NOT NULL COMMENT '인증 수단 (EMAIL_OTP 등)',

    source_otp_id     BIGINT NULL COMMENT '사용된 OTP ID',

    expires_at        DATETIME NOT NULL COMMENT '재인증 권한 만료 시각',

    consumed_at       DATETIME NULL COMMENT '민감 작업 수행 시각',

    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성시간',

    CONSTRAINT fk_reauth_grant_user
        FOREIGN KEY (user_id)
        REFERENCES tb_user(user_id),

    CONSTRAINT fk_reauth_grant_otp
        FOREIGN KEY (source_otp_id)
        REFERENCES tb_email_otp(otp_id)
) ENGINE=InnoDB COMMENT='민감 작업 수행을 위한 재인증 권한 관리';


/* ============================================================
   Index
   ============================================================ */

CREATE INDEX idx_reauth_grant_user_purpose_expires
    ON tb_reauth_grant (user_id, purpose, expires_at);

CREATE INDEX idx_reauth_grant_user_purpose_consumed
    ON tb_reauth_grant (user_id, purpose, consumed_at);
