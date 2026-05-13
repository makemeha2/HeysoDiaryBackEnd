SET NAMES utf8mb4;
SET time_zone = '+09:00';

INSERT INTO tb_common_code (
    group_id,
    code_id,
    code_name,
    is_active,
    extra_info1,
    extra_info2,
    sort_seq,
    created_at,
    created_id,
    updated_at,
    updated_id
)
SELECT
    'AITP_DOMAIN',
    'DIARY_ANALYSIS',
    '일기 장기 메모리 분석',
    1,
    NULL,
    NULL,
    40,
    NOW(),
    1,
    NOW(),
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM tb_common_code
    WHERE group_id = 'AITP_DOMAIN'
      AND code_id = 'DIARY_ANALYSIS'
);
