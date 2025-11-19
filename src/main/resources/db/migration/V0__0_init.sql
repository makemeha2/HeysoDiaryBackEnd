SET NAMES utf8mb4;
SET time_zone = '+09:00';

-- 사용자 테이블
CREATE TABLE tb_user (
    user_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '사용자 PK',
    
    email        VARCHAR(255) NOT NULL COMMENT '이메일 (로그인 및 식별용)',
    nickname     VARCHAR(50)  NOT NULL COMMENT '표시 이름(닉네임)',
    
    role         ENUM('ADMIN', 'MEMBER') NOT NULL DEFAULT 'MEMBER' COMMENT '권한(ADMIN / MEMBER)',
    status       ENUM('ACTIVE', 'INACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE' COMMENT '계정 상태',
    
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_tb_user_email (email),
    KEY idx_tb_user_role (role),
    KEY idx_tb_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자';

-- 로그인/인증정보
CREATE TABLE tb_user_auth (
    user_auth_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '인증 계정 PK',
    user_id           BIGINT UNSIGNED NOT NULL COMMENT 'tb_user.user_id (논리적 FK)',
    
    auth_provider     ENUM('LOCAL', 'GOOGLE', 'NAVER') NOT NULL COMMENT '인증 제공자',
    provider_user_id  VARCHAR(255) NOT NULL COMMENT '제공자 내 유일 ID (sub, id 등)',
    
    -- LOCAL 전용 필드 (Google / Naver는 NULL)
    login_id          VARCHAR(100) NULL COMMENT '사이트 자체 로그인 ID',
    password_hash     VARCHAR(255) NULL COMMENT '비밀번호 해시',
    
    last_login_at     DATETIME NULL COMMENT '마지막 로그인 일시',
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    PRIMARY KEY (user_auth_id),
    UNIQUE KEY uk_user_auth_provider_user (auth_provider, provider_user_id),
    UNIQUE KEY uk_user_auth_login_id (login_id),
    KEY idx_user_auth_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 인증 계정 (LOCAL/GOOGLE/NAVER)';

-- 다이어리
CREATE TABLE tb_diary (
    diary_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '일기 PK',
    user_id      BIGINT UNSIGNED NOT NULL COMMENT '작성자 tb_user.user_id (논리적 FK)',
    
    title        VARCHAR(200) NOT NULL COMMENT '제목',
    content_md   MEDIUMTEXT   NOT NULL COMMENT '마크다운 본문',
    
    diary_date   DATE NOT NULL COMMENT '화면 노출용 날짜(사용자 지정)',
    
    is_deleted   TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    deleted_at   DATETIME NULL COMMENT '삭제 일시',
    
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '실제 작성 시각',
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    
    PRIMARY KEY (diary_id),
    KEY idx_diary_user_id (user_id),
    KEY idx_diary_date (diary_date),
    KEY idx_diary_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일기 본문';

-- 다이어리 image
CREATE TABLE tb_diary_image (
    diary_image_id  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '일기 이미지 PK',
    diary_id        BIGINT UNSIGNED NOT NULL COMMENT 'tb_diary.diary_id (논리적 FK)',
    
    file_name       VARCHAR(255) NULL COMMENT '원본 파일명',
    content_type    VARCHAR(100) NULL COMMENT 'MIME 타입',
    
    image_blob      LONGBLOB NOT NULL COMMENT '이미지 데이터',
    sort_order      INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    
    PRIMARY KEY (diary_image_id),
    KEY idx_diary_image_diary_id (diary_id),
    KEY idx_diary_image_sort (diary_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일기 이미지 Blob';

-- tag
CREATE TABLE tb_tag (
    tag_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '태그 PK',
    tag_name    VARCHAR(50) NOT NULL COMMENT '태그명',
    
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    
    PRIMARY KEY (tag_id),
    UNIQUE KEY uk_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='태그 마스터';

-- tag / diary mapping
CREATE TABLE tb_diary_tag (
    diary_id   BIGINT UNSIGNED NOT NULL COMMENT 'tb_diary.diary_id (논리적 FK)',
    tag_id     BIGINT UNSIGNED NOT NULL COMMENT 'tb_tag.tag_id (논리적 FK)',
    
    PRIMARY KEY (diary_id, tag_id),
    KEY idx_diary_tag_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일기-태그 매핑';

-- comment
CREATE TABLE tb_diary_comment (
    comment_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '댓글 PK',
    
    diary_id          BIGINT UNSIGNED NOT NULL COMMENT 'tb_diary.diary_id (논리적 FK)',
    user_id           BIGINT UNSIGNED NOT NULL COMMENT '작성자 tb_user.user_id (논리적 FK)',
    
    content_md        TEXT NOT NULL COMMENT '댓글 내용 (마크다운 가능)',
    
    parent_comment_id BIGINT UNSIGNED NULL COMMENT '부모 댓글 ID (NULL이면 최상위)',
    
    is_deleted        TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    deleted_at        DATETIME NULL COMMENT '삭제 일시',
    
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    PRIMARY KEY (comment_id),
    KEY idx_comment_diary_id (diary_id),
    KEY idx_comment_user_id (user_id),
    KEY idx_comment_parent_id (parent_comment_id),
    KEY idx_comment_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일기 댓글';
