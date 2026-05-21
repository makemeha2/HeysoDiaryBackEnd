SET NAMES utf8mb4;
SET time_zone = '+09:00';

SET @ADMIN_USER_ID = 1;

INSERT INTO tb_common_code_group
    (group_id, group_name, is_active, created_id, updated_id)
VALUES
    ('ANALYSIS_STATUS', '일기 장기 분석 상태', 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EVENT_TYPE', '일기 장기 분석 사건 유형', 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EMOTION', '일기 장기 분석 감정', 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
    group_name = VALUES(group_name),
    is_active = VALUES(is_active),
    updated_id = VALUES(updated_id);

INSERT INTO tb_common_code
    (group_id, code_id, code_name, is_active, extra_info1, extra_info2, sort_seq, created_id, updated_id)
VALUES
    ('ANALYSIS_STATUS', 'DIRTY', '재분석 필요', 1, NULL, NULL, 10, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('ANALYSIS_STATUS', 'ANALYZING', '분석 중', 1, NULL, NULL, 20, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('ANALYSIS_STATUS', 'SUCCESS', '분석 성공', 1, NULL, NULL, 30, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('ANALYSIS_STATUS', 'FAILED', '분석 실패', 1, NULL, NULL, 40, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('ANALYSIS_STATUS', 'STALE', '분석 제외/무효', 1, NULL, NULL, 50, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('DIARY_EVENT_TYPE', 'DAILY_LIFE', '일상', 1, NULL, NULL, 10, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EVENT_TYPE', 'WORK_STUDY', '일/학업', 1, NULL, NULL, 20, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EVENT_TYPE', 'RELATIONSHIP', '관계', 1, NULL, NULL, 30, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EVENT_TYPE', 'HEALTH', '건강', 1, NULL, NULL, 40, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EVENT_TYPE', 'FAMILY', '가족', 1, NULL, NULL, 50, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EVENT_TYPE', 'FINANCE', '경제', 1, NULL, NULL, 60, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EVENT_TYPE', 'LEISURE', '여가', 1, NULL, NULL, 70, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EVENT_TYPE', 'SELF_CARE', '자기돌봄', 1, NULL, NULL, 80, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EVENT_TYPE', 'OTHER', '기타', 1, NULL, NULL, 90, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('DIARY_EMOTION', 'JOY', '기쁨', 1, NULL, NULL, 10, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EMOTION', 'CALM', '평온', 1, NULL, NULL, 20, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EMOTION', 'GRATITUDE', '감사', 1, NULL, NULL, 30, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EMOTION', 'PRIDE', '뿌듯함', 1, NULL, NULL, 40, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EMOTION', 'SADNESS', '슬픔', 1, NULL, NULL, 50, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EMOTION', 'ANXIETY', '불안', 1, NULL, NULL, 60, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EMOTION', 'ANGER', '분노', 1, NULL, NULL, 70, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EMOTION', 'FATIGUE', '피로', 1, NULL, NULL, 80, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EMOTION', 'CONFUSION', '혼란', 1, NULL, NULL, 90, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('DIARY_EMOTION', 'NEUTRAL', '중립', 1, NULL, NULL, 100, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
    code_name = VALUES(code_name),
    is_active = VALUES(is_active),
    extra_info1 = VALUES(extra_info1),
    extra_info2 = VALUES(extra_info2),
    sort_seq = VALUES(sort_seq),
    updated_id = VALUES(updated_id);
