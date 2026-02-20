-- V5__seed_common_code_mbti.sql
-- Seed: MBTI common codes

-- 1) 그룹 업서트
INSERT INTO tb_common_code_group
  (group_id, group_name, is_active, created_id, updated_id)
VALUES
  ('MBTI', 'MBTI', 1, 0, 0)
ON DUPLICATE KEY UPDATE
  group_name = VALUES(group_name),
  is_active  = VALUES(is_active),
  updated_id = VALUES(updated_id);

-- 2) 코드 업서트 (description -> extra_info1)
INSERT INTO tb_common_code
  (group_id, code_id, code_name, is_active, extra_info1, extra_info2, sort_seq, created_id, updated_id)
VALUES
  ('MBTI', 'INTJ', 'INTJ', 1, '깊이 생각하고 멀리 보는 전략가로, 감정보다 방향과 의미를 중요하게 여깁니다.', NULL,  10, 0, 0),
  ('MBTI', 'INTP', 'INTP', 1, '호기심이 많고 논리적 사고를 즐기며, 스스로 생각을 정리하는 시간을 소중히 여깁니다.', NULL,  20, 0, 0),
  ('MBTI', 'ENTJ', 'ENTJ', 1, '목표를 정하면 추진력이 강하고, 효율과 성취를 중시합니다.', NULL,  30, 0, 0),
  ('MBTI', 'ENTP', 'ENTP', 1, '새로운 아이디어를 탐구하고 토론을 즐기며, 변화를 두려워하지 않습니다.', NULL,  40, 0, 0),

  ('MBTI', 'INFJ', 'INFJ', 1, '깊은 통찰과 공감을 바탕으로 사람과 의미를 연결하려 합니다.', NULL,  50, 0, 0),
  ('MBTI', 'INFP', 'INFP', 1, '자신의 가치와 감정을 소중히 여기며, 진심 어린 위로를 중요하게 생각합니다.', NULL,  60, 0, 0),
  ('MBTI', 'ENFJ', 'ENFJ', 1, '사람을 북돋우고 응원하는 데 기쁨을 느끼며, 따뜻한 소통을 지향합니다.', NULL,  70, 0, 0),
  ('MBTI', 'ENFP', 'ENFP', 1, '감정 표현이 풍부하고, 가능성과 희망을 이야기하는 걸 좋아합니다.', NULL,  80, 0, 0),

  ('MBTI', 'ISTJ', 'ISTJ', 1, '책임감이 강하고, 원칙과 안정감을 중요하게 생각합니다.', NULL,  90, 0, 0),
  ('MBTI', 'ISFJ', 'ISFJ', 1, '조용하지만 깊은 배려를 실천하며, 주변 사람을 세심하게 챙깁니다.', NULL, 100, 0, 0),
  ('MBTI', 'ESTJ', 'ESTJ', 1, '체계적이고 현실적인 판단을 하며, 문제를 빠르게 정리하려 합니다.', NULL, 110, 0, 0),
  ('MBTI', 'ESFJ', 'ESFJ', 1, '관계를 소중히 여기며, 따뜻하고 직접적인 표현을 선호합니다.', NULL, 120, 0, 0),

  ('MBTI', 'ISTP', 'ISTP', 1, '상황을 냉정하게 분석하고, 필요한 행동을 빠르게 선택합니다.', NULL, 130, 0, 0),
  ('MBTI', 'ISFP', 'ISFP', 1, '조용하지만 감성이 깊고, 자신의 속도대로 감정을 다루고 싶어합니다.', NULL, 140, 0, 0),
  ('MBTI', 'ESTP', 'ESTP', 1, '즉각적인 행동과 현실적인 해결을 선호합니다.', NULL, 150, 0, 0),
  ('MBTI', 'ESFP', 'ESFP', 1, '감정 표현이 솔직하고, 즐거움과 활기를 중요하게 생각합니다.', NULL, 160, 0, 0)
ON DUPLICATE KEY UPDATE
  code_name   = VALUES(code_name),
  is_active   = VALUES(is_active),
  extra_info1 = VALUES(extra_info1),
  extra_info2 = VALUES(extra_info2),
  sort_seq    = VALUES(sort_seq),
  updated_id  = VALUES(updated_id);