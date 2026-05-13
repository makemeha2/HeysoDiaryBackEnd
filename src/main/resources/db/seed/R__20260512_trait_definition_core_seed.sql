SET NAMES utf8mb4;
SET time_zone = '+09:00';

SET @ADMIN_USER_ID = 1;

INSERT INTO tb_mst_trait_definition
    (trait_key, trait_category, trait_name, trait_description, trait_scope, approval_status, auto_apply_enabled, score_direction, sort_seq, is_active, created_id, updated_id)
VALUES
    ('SELF_REFLECTION', 'SELF', '자기 성찰', '자신의 생각, 행동, 선택을 돌아보고 의미를 찾는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 1010, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('IDENTITY_CLARITY', 'SELF', '자기 정체감 명료성', '자신이 어떤 사람인지, 무엇을 중요하게 여기는지 비교적 분명하게 인식하는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 1020, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('VALUE_AWARENESS', 'SELF', '가치 인식', '판단과 선택에서 개인의 가치관과 기준을 의식하는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 1030, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('SELF_ACCEPTANCE', 'SELF', '자기 수용', '부족함이나 실수를 포함해 자신의 상태를 인정하고 받아들이는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 1040, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('EMOTION_AWARENESS', 'EMOTION', '감정 인식', '자신의 감정 상태와 변화의 이유를 알아차리는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 2010, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('EMOTION_EXPRESSION', 'EMOTION', '감정 표현', '느낀 감정을 말이나 행동으로 드러내고 공유하는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 2020, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('EMOTION_REGULATION', 'EMOTION', '감정 조절', '감정에 압도되지 않고 상황에 맞게 다루려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 2030, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('POSITIVE_AFFECT', 'EMOTION', '긍정 정서', '기쁨, 만족, 기대, 감사 같은 긍정 감정을 자주 경험하거나 포착하는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 2040, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('ANXIETY_SENSITIVITY', 'EMOTION', '불안 민감도', '불확실성, 평가, 실수 가능성에 대해 불안을 민감하게 느끼는 경향', 'CORE', 'APPROVED', 1, 'NEGATIVE', 2050, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('GOAL_ORIENTATION', 'MOTIVATION', '목표 지향', '목표를 세우고 현재 행동을 목표와 연결해 보려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 3010, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('GROWTH_MINDSET', 'MOTIVATION', '성장 지향', '경험과 시행착오를 배움과 성장의 기회로 해석하는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 3020, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('PURPOSE_SEEKING', 'MOTIVATION', '의미 추구', '일상과 선택에서 더 큰 의미나 방향성을 찾으려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 3030, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('PERSISTENCE', 'MOTIVATION', '끈기', '어려움이 있어도 쉽게 포기하지 않고 이어가려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 3040, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('INITIATIVE', 'MOTIVATION', '주도성', '기다리기보다 스스로 선택하고 시작하려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 3050, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('EXECUTION_FOCUS', 'PRODUCTIVITY', '실행 집중', '생각이나 계획을 실제 행동으로 옮기는 데 초점을 두는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 4010, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('PLANNING_ORGANIZATION', 'PRODUCTIVITY', '계획과 정리', '해야 할 일을 구조화하고 순서나 기준을 세워 정리하는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 4020, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('ROUTINE_STABILITY', 'PRODUCTIVITY', '루틴 안정성', '반복되는 생활 리듬과 습관을 유지하려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 4030, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('TIME_MANAGEMENT', 'PRODUCTIVITY', '시간 관리', '시간 사용을 의식하고 일정, 마감, 우선순위를 조절하려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 4040, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('RELATIONSHIP_ORIENTATION', 'RELATIONSHIP', '관계 지향', '사람들과의 연결과 관계의 질을 중요하게 여기는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 5010, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('COMMUNICATION_OPENNESS', 'RELATIONSHIP', '소통 개방성', '생각과 감정을 대화로 풀고 상대와 조율하려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 5020, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('EMPATHY', 'RELATIONSHIP', '공감성', '타인의 감정과 입장을 이해하고 배려하려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 5030, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('BOUNDARY_SETTING', 'RELATIONSHIP', '관계 경계 설정', '관계 속에서 자신의 한계와 필요를 지키려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 5040, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('SOCIAL_ENERGY', 'RELATIONSHIP', '사회적 에너지', '사람들과 함께하며 에너지를 얻거나 사회적 활동을 선호하는 경향', 'CORE', 'APPROVED', 1, 'NEUTRAL', 5050, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('RESILIENCE', 'RECOVERY', '회복 탄력성', '어려움 이후 다시 균형을 찾고 일상으로 돌아오는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 6010, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('STRESS_MANAGEMENT', 'RECOVERY', '스트레스 관리', '부담과 압박을 인식하고 조절 방법을 찾으려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 6020, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('REST_RECOVERY', 'RECOVERY', '휴식과 회복', '휴식, 수면, 여유 시간을 통해 에너지를 회복하려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 6030, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
    ('SELF_CARE', 'RECOVERY', '자기 돌봄', '몸과 마음의 필요를 챙기고 스스로를 돌보려는 경향', 'CORE', 'APPROVED', 1, 'POSITIVE', 6040, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
    trait_category = VALUES(trait_category),
    trait_name = VALUES(trait_name),
    trait_description = VALUES(trait_description),
    trait_scope = VALUES(trait_scope),
    approval_status = VALUES(approval_status),
    auto_apply_enabled = VALUES(auto_apply_enabled),
    score_direction = VALUES(score_direction),
    sort_seq = VALUES(sort_seq),
    is_active = VALUES(is_active),
    updated_id = VALUES(updated_id);
