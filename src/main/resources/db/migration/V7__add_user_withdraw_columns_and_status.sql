-- =============================================
-- V7__add_user_withdraw_columns_and_status.sql
-- 회원 탈퇴 기능 지원을 위한 컬럼 및 상태 추가
-- 정책:
-- 1. 서비스 데이터는 유지
-- 2. 개인정보는 익명화
-- 3. 동일 이메일 재가입 허용 (탈퇴 시 email 익명화 예정)
-- 4. OAuth 인증 정보는 탈퇴 시 삭제
-- =============================================

-- 1. status ENUM 확장 (WITHDRAWN 추가)
ALTER TABLE tb_user
    MODIFY status ENUM('ACTIVE', 'INACTIVE', 'BLOCKED', 'WITHDRAWN')
    NOT NULL DEFAULT 'ACTIVE'
    COMMENT '계정 상태 (ACTIVE, INACTIVE, BLOCKED, WITHDRAWN)';


-- 2. 탈퇴 관련 컬럼 추가
ALTER TABLE tb_user
    ADD COLUMN withdrawn_at DATETIME NULL COMMENT '탈퇴 일시',
    ADD COLUMN withdraw_reason_cd VARCHAR(50) NULL COMMENT '탈퇴 사유 코드',
    ADD COLUMN withdraw_reason_text VARCHAR(500) NULL COMMENT '탈퇴 사유 상세';


-- 3. 인덱스 추가 (탈퇴 사용자 조회 최적화)
CREATE INDEX idx_tb_user_withdrawn_at
    ON tb_user (withdrawn_at);