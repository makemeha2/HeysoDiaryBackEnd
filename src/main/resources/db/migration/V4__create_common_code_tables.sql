-- V4__create_common_code_tables.sql
-- Common Code Tables (HeysoDiary)
-- - No FK constraints between tables
-- - Hard delete policy (control only via is_active)
-- - created_id/updated_id: BIGINT (userId)
-- - PK: (group_id, code_id)

CREATE TABLE IF NOT EXISTS tb_common_code_group (
  group_id    VARCHAR(30)  NOT NULL COMMENT '그룹 PK (직접입력)',
  group_name  VARCHAR(300) NOT NULL COMMENT '그룹명',

  is_active   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '사용여부(1=사용, 0=미사용)',

  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  created_id  BIGINT       NOT NULL COMMENT '생성자 userId',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  updated_id  BIGINT       NOT NULL COMMENT '수정자 userId',

  PRIMARY KEY (group_id),
  KEY idx_ccg_active (is_active, group_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='공통코드 그룹';


CREATE TABLE IF NOT EXISTS tb_common_code (
  group_id    VARCHAR(30)  NOT NULL COMMENT '그룹 ID (논리 FK: tb_common_code_group.group_id)',
  code_id     VARCHAR(30)  NOT NULL COMMENT '코드 ID (직접입력, 그룹 내 유일)',
  code_name   VARCHAR(300) NOT NULL COMMENT '코드명',

  is_active   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '사용여부(1=사용, 0=미사용)',

  -- ✅ 부가정보 컬럼명 추천: extra_info1/extra_info2
  extra_info1 VARCHAR(255) NULL COMMENT '부가정보1',
  extra_info2 VARCHAR(255) NULL COMMENT '부가정보2',

  sort_seq    INT          NOT NULL DEFAULT 0 COMMENT '노출 순서',

  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  created_id  BIGINT       NOT NULL COMMENT '생성자 userId',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  updated_id  BIGINT       NOT NULL COMMENT '수정자 userId',

  PRIMARY KEY (group_id, code_id),

  KEY idx_cc_group_active_sort (group_id, is_active, sort_seq),
  KEY idx_cc_group_code_name (group_id, code_name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='공통코드';