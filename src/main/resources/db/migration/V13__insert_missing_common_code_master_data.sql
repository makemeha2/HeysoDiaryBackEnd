-- V13__insert_missing_common_code_master_data.sql
-- 공통코드 마스터 데이터 보강
-- - application.yaml 기본 Flyway 경로(db/migration)에서 모든 환경 공통 반영
-- - 기존 migration/seed에 이미 있는 데이터는 건드리지 않고, 누락분만 추가
-- - 중복 충돌 방지를 위해 모두 "없는 경우에만" INSERT

SET NAMES utf8mb4;
SET time_zone = '+09:00';

-- =========================================================
-- 1) 공통코드 그룹
--    - V6에는 AIFB_* 코드만 있고 그룹은 없음
--    - MBTI는 local seed에만 있어 기본 환경에서는 누락됨
--    - AI_MODELS / AITP_DOMAIN 은 레포 내 미등록
-- =========================================================
INSERT INTO tb_common_code_group (
  group_id, group_name, is_active,
  created_at, created_id, updated_at, updated_id
)
SELECT * FROM (
  SELECT 'AIFB_SPEECH_TONE' AS group_id, '피드백 말투' AS group_name, 1 AS is_active, CURRENT_TIMESTAMP AS created_at, 0 AS created_id, CURRENT_TIMESTAMP AS updated_at, 1 AS updated_id
  UNION ALL
  SELECT 'AIFB_STYLE', '피드백 스타일', 1, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AIFB_INTENSITY', '피드백 강도', 1, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AIFB_QUESTION', '피드백 질문 포함 여부', 1, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AIFB_LENGTH', '피드백 길이', 1, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AIFB_LANG_MODE', '피드백 언어 설정', 1, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AI_MODELS', 'AI 모델', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AITP_DOMAIN', '템플릿 도메인', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'MBTI', 'MBTI', 1, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
) v
WHERE NOT EXISTS (
  SELECT 1
  FROM tb_common_code_group g
  WHERE g.group_id = v.group_id
);

-- =========================================================
-- 2) 공통코드
--    - MBTI는 seed에만 있으므로 모든 환경 공통 반영을 위해 migration에 보강
--    - AI_MODELS / AITP_DOMAIN 은 로컬 DB 기준값 반영
--    - 기존 V6의 AIFB_* 코드는 중복 등록하지 않음
-- =========================================================
INSERT INTO tb_common_code (
  group_id, code_id, code_name, is_active,
  extra_info1, extra_info2, sort_seq,
  created_at, created_id, updated_at, updated_id
)
SELECT * FROM (
  SELECT 'AI_MODELS' AS group_id, 'OPENAI_003' AS code_id, 'GPT-4 Turbo' AS code_name, 1 AS is_active, 'OPENAI' AS extra_info1, NULL AS extra_info2, 0 AS sort_seq, CURRENT_TIMESTAMP AS created_at, 1 AS created_id, CURRENT_TIMESTAMP AS updated_at, 1 AS updated_id
  UNION ALL
  SELECT 'AI_MODELS', 'OPENAI_001', 'GPT-4o', 1, 'OPENAI', NULL, 10, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AI_MODELS', 'OPENAI_002', 'GPT-4o-mini', 1, 'OPENAI', NULL, 20, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AI_MODELS', 'ATRP_001', 'Claude 3.5 Sonnet', 1, 'Anthropic', NULL, 200, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AI_MODELS', 'ATRP_002', 'Claude 3 Haiku', 1, 'Anthropic', NULL, 210, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AI_MODELS', 'ATRP_003', 'Claude 3 Opus', 1, 'Anthropic', NULL, 230, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AITP_DOMAIN', 'DIARY_AI', '일기장', 1, NULL, NULL, 10, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AITP_DOMAIN', 'DIARY_AI_POLISH', '일기 교정', 1, NULL, NULL, 20, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'AITP_DOMAIN', 'DIARY_NUDGE', '인사말', 1, NULL, NULL, 30, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
  UNION ALL
  SELECT 'MBTI', 'INTJ', 'INTJ', 1, '깊이 생각하고 멀리 보는 전략가로, 감정보다 방향과 의미를 중요하게 여깁니다.', NULL, 10, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'INTP', 'INTP', 1, '호기심이 많고 논리적 사고를 즐기며, 스스로 생각을 정리하는 시간을 소중히 여깁니다.', NULL, 20, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ENTJ', 'ENTJ', 1, '목표를 정하면 추진력이 강하고, 효율과 성취를 중시합니다.', NULL, 30, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ENTP', 'ENTP', 1, '새로운 아이디어를 탐구하고 토론을 즐기며, 변화를 두려워하지 않습니다.', NULL, 40, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'INFJ', 'INFJ', 1, '깊은 통찰과 공감을 바탕으로 사람과 의미를 연결하려 합니다.', NULL, 50, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'INFP', 'INFP', 1, '자신의 가치와 감정을 소중히 여기며, 진심 어린 위로를 중요하게 생각합니다.', NULL, 60, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ENFJ', 'ENFJ', 1, '사람을 북돋우고 응원하는 데 기쁨을 느끼며, 따뜻한 소통을 지향합니다.', NULL, 70, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ENFP', 'ENFP', 1, '감정 표현이 풍부하고, 가능성과 희망을 이야기하는 걸 좋아합니다.', NULL, 80, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ISTJ', 'ISTJ', 1, '책임감이 강하고, 원칙과 안정감을 중요하게 생각합니다.', NULL, 90, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ISFJ', 'ISFJ', 1, '조용하지만 깊은 배려를 실천하며, 주변 사람을 세심하게 챙깁니다.', NULL, 100, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ESTJ', 'ESTJ', 1, '체계적이고 현실적인 판단을 하며, 문제를 빠르게 정리하려 합니다.', NULL, 110, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ESFJ', 'ESFJ', 1, '관계를 소중히 여기며, 따뜻하고 직접적인 표현을 선호합니다.', NULL, 120, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ISTP', 'ISTP', 1, '상황을 냉정하게 분석하고, 필요한 행동을 빠르게 선택합니다.', NULL, 130, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ISFP', 'ISFP', 1, '조용하지만 감성이 깊고, 자신의 속도대로 감정을 다루고 싶어합니다.', NULL, 140, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ESTP', 'ESTP', 1, '즉각적인 행동과 현실적인 해결을 선호합니다.', NULL, 150, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
  UNION ALL
  SELECT 'MBTI', 'ESFP', 'ESFP', 1, '감정 표현이 솔직하고, 즐거움과 활기를 중요하게 생각합니다.', NULL, 160, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, 0
) v
WHERE NOT EXISTS (
  SELECT 1
  FROM tb_common_code c
  WHERE c.group_id = v.group_id
    AND c.code_id = v.code_id
);
