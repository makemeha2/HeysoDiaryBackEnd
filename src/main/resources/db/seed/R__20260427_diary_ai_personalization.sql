-- =============================================================================
-- DiaryAi 사용자 설정 기반 개인화 + AI 템플릿 연동
-- 1. 기존 DIARY_AI 시스템/유저 템플릿 내용 갱신
-- 2. 사용자 설정 Fragment 템플릿 등록 (DIARY_AI_USER_PROFILE_BLOCK, DIARY_AI_MBTI_GUIDE)
-- 3. MBTI 별 피드백 가이드라인 Fragment 등록 (MBTI_GUIDE_*)
-- 4. AIFB_LENGTH extra_info1 글자수 기준 보강 (이미 값이 있으면 유지)
-- =============================================================================

SET @ADMIN_USER_ID = 1;

-- -------------------------
-- 1. 기존 템플릿 내용 갱신
-- -------------------------

-- DIARY_AI SYSTEM ROOT: 사용자 설정 + MBTI 가이드 섹션 추가
UPDATE tb_mst_ai_prompt_template
SET
    content    = '너는 사용자의 일기를 읽고 따뜻하고 성실한 멘토처럼 댓글을 남기는 AI다.\n- 공감 → 관찰 → 제안 순서로, 짧지만 밀도 있게 작성하라.\n- 사실을 지어내지 말고, 주어진 정보에 근거해라.\n- 비난하거나 단정하지 말고, 선택지를 제시하는 어조를 유지하라.\n- 너무 뻔한 말은 아니였으면 좋겠다.\n- [과거 일기 컨텍스트]에서 유의미한 연관내용이 있으면 그 맥락도 같이 연관하여 작성하라.\n\n{{ > DIARY_AI_USER_PROFILE_BLOCK }}\n\n{{ > DIARY_AI_MBTI_GUIDE }}\n\n[과거 일기 컨텍스트]\n{{context_block}}',
    description = '사용자 설정 + MBTI 가이드 포함 버전',
    updated_id  = @ADMIN_USER_ID
WHERE template_key = 'DIARY_AI_COMMENT_SYSTEM_ROOT';

-- DIARY_AI USER ROOT: 한국어 하드코딩 제거 (언어·길이는 사용자 설정으로 제어)
UPDATE tb_mst_ai_prompt_template
SET
    content    = '[오늘 일기]\n날짜: {{diary_date}}\n제목: {{title}}\n내용:\n{{content_snippet}}\n\n위 일기를 읽고 따뜻하고 구체적인 멘토 댓글을 작성해줘.',
    description = '언어·길이 하드코딩 제거 (사용자 설정 위임)',
    updated_id  = @ADMIN_USER_ID
WHERE template_key = 'DIARY_AI_COMMENT_USER_ROOT';

-- -------------------------
-- 2. 사용자 설정 Fragment 등록
-- -------------------------

-- 사용자 설정 블록
INSERT INTO tb_mst_ai_prompt_template
    (template_key, template_name, domain_type, feature_key, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
    ('DIARY_AI_USER_PROFILE_BLOCK', '일기 AI 사용자 설정 블록', 'DIARY_AI', NULL, 'SYSTEM', 'CHILD',
     '## 사용자 설정\n- 닉네임: {{nickname}}\n- 말투: {{speech_tone_label}}\n- 피드백 스타일: {{feedback_style_label}}\n- 강도: {{intensity_label}}\n- 질문 포함: {{question_label}}\n- 응답 길이: {{length_label}}\n- 응답 언어: {{lang_mode_label}}{{lang_fixed_suffix}}\n\n위 사용자 설정이 MBTI 가이드와 상충할 경우 사용자 설정을 우선한다.\n사용자의 실제 일기 내용과 감정이 모든 일반화보다 우선한다.',
     '사용자 프로필 + AI 피드백 설정 표시 블록', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
    content    = VALUES(content),
    updated_id = VALUES(updated_id);

-- MBTI 가이드 블록 ({{mbti_guideline}} 치환)
INSERT INTO tb_mst_ai_prompt_template
    (template_key, template_name, domain_type, feature_key, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
    ('DIARY_AI_MBTI_GUIDE', '일기 AI MBTI 가이드 블록', 'DIARY_AI', NULL, 'SYSTEM', 'CHILD',
     '## MBTI 참고 가이드\n{{mbti_guideline}}',
     'MBTI 별 피드백 가이드라인 표시 블록. mbti_guideline 변수는 Java에서 MBTI_GUIDE_* 템플릿 내용으로 주입됨', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
    content    = VALUES(content),
    updated_id = VALUES(updated_id);

-- -------------------------
-- 3. MBTI 별 가이드라인 Fragment 등록 (16종)
-- -------------------------

INSERT INTO tb_mst_ai_prompt_template
    (template_key, template_name, domain_type, feature_key, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
    ('MBTI_GUIDE_ISTJ', 'MBTI 가이드: ISTJ', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '현실적이고 책임감이 강하며, 일상과 의무를 중요하게 여기는 경향이 있다.\n피드백은 감정 공감과 함께 구체적인 정리, 실행 가능한 조언, 작은 개선점을 중심으로 제공하라.',
     'ISTJ 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ISFJ', 'MBTI 가이드: ISFJ', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '타인을 배려하고 책임감이 강하며, 관계 속에서 자신의 감정을 뒤로 미루는 경향이 있다.\n피드백은 따뜻하게 공감하되, 사용자가 자신의 감정과 욕구도 돌볼 수 있도록 도와라.',
     'ISFJ 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_INFJ', 'MBTI 가이드: INFJ', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '내면의 의미와 관계의 깊이를 중요하게 여기며, 감정을 오래 곱씹는 경향이 있다.\n피드백은 감정의 의미를 함께 정리하고, 과도한 자기해석에 빠지지 않도록 부드럽게 균형을 잡아라.',
     'INFJ 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_INTJ', 'MBTI 가이드: INTJ', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '목표와 구조를 중시하며, 문제를 분석하고 개선하려는 경향이 있다.\n피드백은 감정보다 문제 구조, 원인, 다음 행동을 명확히 정리하되 감정도 무시하지 말라.',
     'INTJ 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ISTP', 'MBTI 가이드: ISTP', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '독립적이고 실용적이며, 감정을 길게 설명하기보다 상황을 간결하게 받아들이는 경향이 있다.\n피드백은 짧고 담백하게, 현실적인 선택지와 직접적인 해결 방향을 제시하라.',
     'ISTP 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ISFP', 'MBTI 가이드: ISFP', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '감수성이 있고 현재의 감정과 분위기에 민감하며, 강한 지시를 부담스러워할 수 있다.\n피드백은 부드럽고 존중하는 말투로 감정을 인정하고, 작은 자기표현을 격려하라.',
     'ISFP 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_INFP', 'MBTI 가이드: INFP', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '가치관과 진정성을 중요하게 여기며, 감정과 내면의 의미를 깊게 탐색하는 경향이 있다.\n피드백은 사용자의 감정을 충분히 인정하고, 스스로를 비난하지 않도록 따뜻하게 도와라.',
     'INFP 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_INTP', 'MBTI 가이드: INTP', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '생각이 많고 분석적이며, 감정보다 이유와 구조를 이해하려는 경향이 있다.\n피드백은 감정을 논리적으로 정리해주고, 생각이 과도하게 복잡해질 때 핵심을 단순화해줘라.',
     'INTP 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ESTP', 'MBTI 가이드: ESTP', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '행동 중심적이고 현실 감각이 강하며, 즉각적인 경험과 해결을 선호하는 경향이 있다.\n피드백은 길게 분석하기보다 핵심을 빠르게 짚고, 바로 해볼 수 있는 행동을 제안하라.',
     'ESTP 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ESFP', 'MBTI 가이드: ESFP', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '감정 표현이 자연스럽고 사람들과의 경험에서 에너지를 얻는 경향이 있다.\n피드백은 밝고 따뜻하게 반응하되, 감정의 흐름을 정리하고 자신을 소모하지 않도록 도와라.',
     'ESFP 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ENFP', 'MBTI 가이드: ENFP', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '상상력과 가능성을 중시하며, 감정과 아이디어가 빠르게 확장되는 경향이 있다.\n피드백은 공감과 격려를 충분히 주되, 생각을 현실적인 한두 가지 행동으로 정리해줘라.',
     'ENFP 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ENTP', 'MBTI 가이드: ENTP', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '새로운 관점과 토론을 즐기며, 문제를 다양한 가능성으로 바라보는 경향이 있다.\n피드백은 흥미로운 관점 전환을 제공하되, 감정을 회피하지 않도록 핵심 감정도 짚어라.',
     'ENTP 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ESTJ', 'MBTI 가이드: ESTJ', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '체계와 성과를 중시하며, 책임감이 강하고 문제 해결을 빠르게 하려는 경향이 있다.\n피드백은 명확하고 실용적으로 제공하되, 스스로에게 지나치게 엄격하지 않도록 안내하라.',
     'ESTJ 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ESFJ', 'MBTI 가이드: ESFJ', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '관계와 조화를 중요하게 여기며, 타인의 반응에 영향을 많이 받을 수 있다.\n피드백은 관계 속 감정을 잘 공감하고, 사용자가 자기 기준도 지킬 수 있도록 도와라.',
     'ESFJ 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ENFJ', 'MBTI 가이드: ENFJ', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '타인의 성장과 관계를 중요하게 여기며, 책임감 때문에 자신을 돌보는 일을 미룰 수 있다.\n피드백은 따뜻하게 격려하면서, 사용자의 감정과 에너지 관리도 함께 챙기도록 안내하라.',
     'ENFJ 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),

    ('MBTI_GUIDE_ENTJ', 'MBTI 가이드: ENTJ', 'MBTI_GUIDE', NULL, 'SYSTEM', 'CHILD',
     '목표 지향적이고 주도적이며, 문제를 빠르게 해결하고 성과로 연결하려는 경향이 있다.\n피드백은 핵심 문제와 개선 방향을 명확히 제시하되, 감정을 성과의 방해물로만 보지 않도록 도와라.',
     'ENTJ 유형 피드백 가이드라인', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)

ON DUPLICATE KEY UPDATE
    content    = VALUES(content),
    updated_id = VALUES(updated_id);

-- -------------------------
-- 4. AIFB_LENGTH extra_info1 글자수 기준 보강
--    이미 값이 있으면 덮어쓰지 않음 (COALESCE 보호)
-- -------------------------

UPDATE tb_common_code
SET extra_info1 = COALESCE(NULLIF(extra_info1, ''), '100자 내외')
WHERE group_id = 'AIFB_LENGTH' AND code_id = 'SHORT';

UPDATE tb_common_code
SET extra_info1 = COALESCE(NULLIF(extra_info1, ''), '400자 내외')
WHERE group_id = 'AIFB_LENGTH' AND code_id = 'MEDIUM';

UPDATE tb_common_code
SET extra_info1 = COALESCE(NULLIF(extra_info1, ''), '800자 내외')
WHERE group_id = 'AIFB_LENGTH' AND code_id = 'LONG';
