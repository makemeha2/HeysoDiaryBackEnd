-- =========================================================
-- V2__add_user_profile_and_thumbnail.sql
-- 사용자 프로필(닉네임/MBTI) + 썸네일(Blob) 분리
-- =========================================================

-- 1) 사용자 프로필: 1 user : 1 profile
CREATE TABLE IF NOT EXISTS tb_user_profile (
  user_id     BIGINT UNSIGNED NOT NULL COMMENT 'tb_user.user_id (logical FK)',
  nickname    VARCHAR(50) NULL COMMENT '마이페이지 닉네임(없으면 tb_user.nickname 사용)',
  mbti        VARCHAR(4) NULL COMMENT 'MBTI (예: INFP)',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (user_id),
  KEY idx_tb_user_profile_mbti (mbti)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자 프로필(마이페이지 설정)';

-- 2) 사용자 썸네일: 작은 이미지이므로 Blob 저장 (1 user : 1 thumbnail)
CREATE TABLE IF NOT EXISTS tb_user_thumbnail (
  user_id       BIGINT UNSIGNED NOT NULL COMMENT 'tb_user.user_id (logical FK)',
  file_name     VARCHAR(255) NULL COMMENT '원본 파일명',
  content_type  VARCHAR(100) NULL COMMENT 'MIME 타입 (image/png, image/jpeg, image/webp)',
  image_blob    LONGBLOB NOT NULL COMMENT '썸네일 이미지 데이터(서버에서 리사이즈 후 저장 권장)',
  bytes         INT UNSIGNED NULL COMMENT '리사이즈 후 파일 크기(바이트)',
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (user_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자 썸네일(Blob)';