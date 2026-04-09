-- =============================================================================
-- AI Template Management - 초기 데이터
-- Runtime Profile 3건 → Template 6건 → Binding 3건 순서로 삽입
-- UNIQUE KEY 기반 멱등성 보장 (ON DUPLICATE KEY UPDATE로 내용 갱신)
-- =============================================================================

SET @ADMIN_USER_ID = 1;

-- -------------------------
-- 1. Runtime Profile (3건)
-- -------------------------
INSERT INTO tb_mst_ai_runtime_profile
  (profile_key, profile_name, domain_type, provider, model, temperature, top_p, max_tokens, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('DIARY_AI_COMMENT_DEFAULT', '일기 AI 멘토 댓글 기본 프로파일', 'DIARY_AI', 'OPENAI', 'gpt-4o',
   NULL, NULL, NULL, '기존 DiaryAiService 기본값 기준', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
  ('DIARY_AI_POLISH_DEFAULT', '일기 AI 글다듬기 기본 프로파일', 'DIARY_AI_POLISH', 'OPENAI', 'gpt-4o-mini',
   0.200, NULL, 2500, '기존 DiaryAiPolishAiClient 기본값 기준', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID),
  ('DIARY_NUDGE_DEFAULT', '일기 넛지 기본 프로파일', 'DIARY_NUDGE', 'OPENAI', 'gpt-4o',
   NULL, NULL, NULL, '기존 DiaryNudgeService 기본값 기준', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  profile_name = VALUES(profile_name),
  model        = VALUES(model),
  temperature  = VALUES(temperature),
  top_p        = VALUES(top_p),
  max_tokens   = VALUES(max_tokens),
  updated_id   = VALUES(updated_id);

-- -------------------------
-- 2. Prompt Template (6건)
-- -------------------------

-- DIARY_AI SYSTEM ROOT
INSERT INTO tb_mst_ai_prompt_template
  (template_key, template_name, domain_type, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('DIARY_AI_COMMENT_SYSTEM_ROOT', '일기 AI 멘토 댓글 시스템 프롬프트', 'DIARY_AI', 'SYSTEM', 'ROOT',
   '너는 사용자의 일기를 읽고 따뜻하고 성실한 멘토처럼 댓글을 남기는 AI다.\n- 공감 → 관찰 → 제안 순서로, 짧지만 밀도 있게 작성하라.\n- 사실을 지어내지 말고, 주어진 정보에 근거해라.\n- 비난하거나 단정하지 말고, 선택지를 제시하는 어조를 유지하라.\n- 너무 뻔한 말은 아니였으면 좋겠다.\n- [과거 일기 컨텍스트]에서 유의미한 연관내용이 있으면 그 맥락도 같이 연관하여 작성하라.\n\n[과거 일기 컨텍스트]\n{{context_block}}',
   '기존 DiaryAiService.MENTOR_SYSTEM_PROMPT 기준', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  content    = VALUES(content),
  updated_id = VALUES(updated_id);

-- DIARY_AI USER ROOT
INSERT INTO tb_mst_ai_prompt_template
  (template_key, template_name, domain_type, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('DIARY_AI_COMMENT_USER_ROOT', '일기 AI 멘토 댓글 사용자 프롬프트', 'DIARY_AI', 'USER', 'ROOT',
   '[오늘 일기]\n날짜: {{diary_date}}\n제목: {{title}}\n내용:\n{{content_snippet}}\n\n위 일기를 읽고, 너무 길지 않게 한국어로 따뜻하고 구체적인 멘토 댓글을 작성해줘.',
   '기존 DiaryAiService.MENTOR_USER_PROMPT 기준', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  content    = VALUES(content),
  updated_id = VALUES(updated_id);

-- DIARY_AI_POLISH SYSTEM ROOT
INSERT INTO tb_mst_ai_prompt_template
  (template_key, template_name, domain_type, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('DIARY_AI_POLISH_SYSTEM_ROOT', '일기 AI 글다듬기 시스템 프롬프트', 'DIARY_AI_POLISH', 'SYSTEM', 'ROOT',
   '너는 사용자의 일기 문장을 다듬는 편집 도우미다.\n목표는 사용자의 스타일과 톤을 최대한 유지하면서 오탈자, 띄어쓰기, 맞춤법, 어색한 문맥만 자연스럽게 바로잡는 것이다.\n아래 원칙을 반드시 지켜라.\n- 사용자의 말투, 감정선, 시점, 분위기를 유지한다.\n- 새로운 사실, 감정, 해석, 묘사를 추가하지 않는다.\n- 과장, 요약, 재구성, 문체 변질, 불필요한 미사여구 추가를 피한다.\n- 문장 순서와 표현은 꼭 필요한 범위에서만 최소한으로 수정한다.\n- 내용이 다소 투박해도 사용자의 개성을 해치지 않는 방향을 우선한다.\n- 결과는 설명 없이 다듬어진 최종 본문만 plain text로 반환한다.\n- Markdown 문법, 따옴표, 제목, 불릿, 안내문을 붙이지 않는다.\n- 사용자의 줄바꿈(문단 구조)은 그대로 유지한다. 줄을 합치거나 새로 나누지 않는다.',
   '기존 DiaryAiPolishAiClient.SYSTEM_PROMPT 기준', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  content    = VALUES(content),
  updated_id = VALUES(updated_id);

-- DIARY_AI_POLISH USER ROOT
INSERT INTO tb_mst_ai_prompt_template
  (template_key, template_name, domain_type, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('DIARY_AI_POLISH_USER_ROOT', '일기 AI 글다듬기 사용자 프롬프트', 'DIARY_AI_POLISH', 'USER', 'ROOT',
   '아래 일기 본문을 위 원칙에 맞게 다듬어줘.\n\n[원본 일기]\n{{original_content}}',
   '기존 DiaryAiPolishAiClient.USER_PROMPT 기준', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  content    = VALUES(content),
  updated_id = VALUES(updated_id);

-- DIARY_NUDGE SYSTEM ROOT
INSERT INTO tb_mst_ai_prompt_template
  (template_key, template_name, domain_type, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('DIARY_NUDGE_SYSTEM_ROOT', '일기 넛지 시스템 프롬프트', 'DIARY_NUDGE', 'SYSTEM', 'ROOT',
   '너는 사용자의 일기 내용을 바탕으로 오늘 하루를 정리하는 일기를 쓸수 있게 도와주는 안부 질문을 만든다.\n- 1~2문장, 한국어\n- 출력은 \'메시지 텍스트\'만 반환할 것 (마크다운 불필요)\n- 사용자가 메세지를 읽고 일기 작성을 하고 싶어지도록 노굴적이거나 직접적이지 않은 메세지도 포함시켜도 된다.',
   '기존 DiaryNudgeService.SYSTEM_PROMPT 기준', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  content    = VALUES(content),
  updated_id = VALUES(updated_id);

-- DIARY_NUDGE USER ROOT
INSERT INTO tb_mst_ai_prompt_template
  (template_key, template_name, domain_type, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('DIARY_NUDGE_USER_ROOT', '일기 넛지 사용자 프롬프트', 'DIARY_NUDGE', 'USER', 'ROOT',
   '[오늘 이전 최신 일기]\n날짜: {{diary_date}}\n제목: {{title}}\n내용:\n{{content_snippet}}\n\n위 내용을 바탕으로 짧게 안부로 말을 건낼 수 있는 질문을 던져줘.',
   '기존 DiaryNudgeService.USER_PROMPT 기준', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  content    = VALUES(content),
  updated_id = VALUES(updated_id);

-- -------------------------
-- 3. Binding (3건)
-- -------------------------

-- DIARY_AI COMMENT 바인딩
INSERT INTO tb_mst_ai_prompt_binding
  (binding_name, domain_type, feature_key, system_template_id, user_template_id, runtime_profile_id, description, is_active, created_id, updated_id)
SELECT
  '일기 AI 멘토 댓글 기본 바인딩',
  'DIARY_AI',
  'COMMENT',
  s.template_id,
  u.template_id,
  p.runtime_profile_id,
  '기존 DiaryAiService 기반 바인딩',
  1,
  @ADMIN_USER_ID,
  @ADMIN_USER_ID
FROM tb_mst_ai_prompt_template s
   , tb_mst_ai_prompt_template u
   , tb_mst_ai_runtime_profile p
WHERE s.template_key = 'DIARY_AI_COMMENT_SYSTEM_ROOT'
  AND u.template_key = 'DIARY_AI_COMMENT_USER_ROOT'
  AND p.profile_key  = 'DIARY_AI_COMMENT_DEFAULT'
ON DUPLICATE KEY UPDATE
  system_template_id = VALUES(system_template_id),
  user_template_id   = VALUES(user_template_id),
  runtime_profile_id = VALUES(runtime_profile_id),
  updated_id         = VALUES(updated_id);

-- DIARY_AI_POLISH POLISH 바인딩
INSERT INTO tb_mst_ai_prompt_binding
  (binding_name, domain_type, feature_key, system_template_id, user_template_id, runtime_profile_id, description, is_active, created_id, updated_id)
SELECT
  '일기 AI 글다듬기 기본 바인딩',
  'DIARY_AI_POLISH',
  'POLISH',
  s.template_id,
  u.template_id,
  p.runtime_profile_id,
  '기존 DiaryAiPolishAiClient 기반 바인딩',
  1,
  @ADMIN_USER_ID,
  @ADMIN_USER_ID
FROM tb_mst_ai_prompt_template s
   , tb_mst_ai_prompt_template u
   , tb_mst_ai_runtime_profile p
WHERE s.template_key = 'DIARY_AI_POLISH_SYSTEM_ROOT'
  AND u.template_key = 'DIARY_AI_POLISH_USER_ROOT'
  AND p.profile_key  = 'DIARY_AI_POLISH_DEFAULT'
ON DUPLICATE KEY UPDATE
  system_template_id = VALUES(system_template_id),
  user_template_id   = VALUES(user_template_id),
  runtime_profile_id = VALUES(runtime_profile_id),
  updated_id         = VALUES(updated_id);

-- DIARY_NUDGE NUDGE 바인딩
INSERT INTO tb_mst_ai_prompt_binding
  (binding_name, domain_type, feature_key, system_template_id, user_template_id, runtime_profile_id, description, is_active, created_id, updated_id)
SELECT
  '일기 넛지 기본 바인딩',
  'DIARY_NUDGE',
  'NUDGE',
  s.template_id,
  u.template_id,
  p.runtime_profile_id,
  '기존 DiaryNudgeService 기반 바인딩',
  1,
  @ADMIN_USER_ID,
  @ADMIN_USER_ID
FROM tb_mst_ai_prompt_template s
   , tb_mst_ai_prompt_template u
   , tb_mst_ai_runtime_profile p
WHERE s.template_key = 'DIARY_NUDGE_SYSTEM_ROOT'
  AND u.template_key = 'DIARY_NUDGE_USER_ROOT'
  AND p.profile_key  = 'DIARY_NUDGE_DEFAULT'
ON DUPLICATE KEY UPDATE
  system_template_id = VALUES(system_template_id),
  user_template_id   = VALUES(user_template_id),
  runtime_profile_id = VALUES(runtime_profile_id),
  updated_id         = VALUES(updated_id);
