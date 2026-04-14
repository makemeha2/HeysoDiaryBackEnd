-- ======================================================
-- R20251119__0_seed.sql
-- Repeatable Seed Data (개발/테스트 전용)
-- ======================================================

-- 1) 테이블 초기화 (FK 사용 안 하므로 순서만 맞추면 됨)
TRUNCATE TABLE tb_diary_comment;
TRUNCATE TABLE tb_diary_tag;
TRUNCATE TABLE tb_tag;
TRUNCATE TABLE tb_diary_image;
TRUNCATE TABLE tb_diary;
TRUNCATE TABLE tb_user_auth;
TRUNCATE TABLE tb_user;

-- 2) AUTO_INCREMENT 리셋 (선택)
ALTER TABLE tb_user AUTO_INCREMENT = 1;
ALTER TABLE tb_user_auth AUTO_INCREMENT = 1;
ALTER TABLE tb_diary AUTO_INCREMENT = 1;
ALTER TABLE tb_diary_image AUTO_INCREMENT = 1;
ALTER TABLE tb_tag AUTO_INCREMENT = 1;
ALTER TABLE tb_diary_tag AUTO_INCREMENT = 1;
ALTER TABLE tb_diary_comment AUTO_INCREMENT = 1;

-- ======================================================
-- 3) SEED DATA INSERT
-- ======================================================

-- 사용자
INSERT INTO tb_user (user_id, email, nickname, role, status, created_at, updated_at)
VALUES
(1, 'admin@example.com',  '관리자', 'ADMIN',  'ACTIVE', '2025-11-01 09:00:00', '2025-11-01 09:00:00'),
(2, 'member1@example.com','소영',   'MEMBER', 'ACTIVE', '2025-11-01 09:10:00', '2025-11-01 09:10:00'),
(3, 'member2@example.com','혜진',   'MEMBER', 'ACTIVE', '2025-11-01 09:20:00', '2025-11-01 09:20:00');

-- 사용자 인증 정보
INSERT INTO tb_user_auth (
    user_auth_id, user_id, auth_provider, provider_user_id,
    login_id, password_hash, last_login_at, created_at, updated_at
) VALUES
(1, 1, 'LOCAL',  'admin_local',
 'admin', '$2a$10$HNOpwqwgkJhL8iRanNxBcerU376kgMg3qfzzTpFdI2niS.wOAUZua', '2025-11-18 08:00:00', '2025-11-01 09:00:00', '2025-11-18 08:00:00'),

(2, 1, 'GOOGLE', 'google-sub-1234567890',
 NULL, NULL, '2025-11-18 09:00:00', '2025-11-01 09:05:00', '2025-11-18 09:00:00'),

(3, 2, 'LOCAL',  'member1_local',
 'member1', '$2a$10$LOCAL_MEMBER1_HASH_EXAMPLE','2025-11-18 10:00:00','2025-11-01 09:10:00','2025-11-18 10:00:00'),

(4, 3, 'NAVER',  'naver-id-abcdef',
 NULL, NULL, '2025-11-18 11:00:00','2025-11-01 09:20:00','2025-11-18 11:00:00');

-- 다이어리
INSERT INTO tb_diary (
    diary_id, user_id, title, content_md,
    diary_date, is_deleted, deleted_at,
    created_at, updated_at
) VALUES
(1, 2,
 '첫 번째 일기 - 소영',
 '# 첫 일기\n\n오늘은 다이어리 프로젝트를 시작했다.\n\n- DB 스키마 설계\n- 마크다운 에디터 구상',
 '2025-11-01', 0, NULL,
 '2025-11-01 21:30:00', '2025-11-01 21:30:00'),

(2, 2,
 '두 번째 일기 - 날짜 테스트',
 '## 두 번째 일기\n\n작성일과 노출일을 다르게 설정해보았다.\n\n노출일=11/2, 작성일=11/3',
 '2025-11-02', 0, NULL,
 '2025-11-03 08:15:00', '2025-11-03 08:15:00'),

(3, 3,
 '혜진의 일기',
 '오늘은 쇼핑을 했다.\n\n- 장을 봤다\n- 책상 고민 중...',
 '2025-11-03', 0, NULL,
 '2025-11-03 21:00:00', '2025-11-03 21:00:00');

-- 이미지
INSERT INTO tb_diary_image (
    diary_image_id, diary_id, file_name, content_type,
    image_blob, sort_order, created_at
) VALUES
(1, 1, 'diary1_img1.jpg', 'image/jpeg', X'FFD8FF', 1, '2025-11-01 21:31:00'),
(2, 1, 'diary1_img2.jpg', 'image/jpeg', X'FFD8FF', 2, '2025-11-01 21:32:00'),
(3, 2, 'diary2_img1.png', 'image/png',  X'89504E47', 1, '2025-11-03 08:16:00');

-- 태그
INSERT INTO tb_tag (tag_id, tag_name, created_at)
VALUES
(1, '일상',     '2025-11-01 20:00:00'),
(2, '개발',     '2025-11-01 20:00:00'),
(3, '가족',     '2025-11-01 20:00:00'),
(4, '생각정리', '2025-11-01 20:00:00');

-- 태그 매핑
INSERT INTO tb_diary_tag (diary_id, tag_id)
VALUES
(1, 1), (1, 2),
(2, 2), (2, 4),
(3, 1), (3, 3);

-- 댓글
INSERT INTO tb_diary_comment (
    comment_id, diary_id, user_id,
    content_md, parent_comment_id,
    is_deleted, deleted_at,
    created_at, updated_at
) VALUES
(1, 1, 1, '첫 일기 축하드립니다! 🎉', NULL,
 0, NULL, '2025-11-02 09:00:00', '2025-11-02 09:00:00'),

(2, 1, 2, '감사합니다! 이제 기능을 만들어볼게요.', 1,
 0, NULL, '2025-11-02 09:10:00', '2025-11-02 09:10:00'),

(3, 2, 3, 'diary_date 테스트 재밌네 👍', NULL,
 0, NULL, '2025-11-03 09:00:00', '2025-11-03 09:00:00'),

(4, 3, 2, '오늘도 수고 많았어 🙂', NULL,
 0, NULL, '2025-11-03 22:00:00', '2025-11-03 22:00:00');
