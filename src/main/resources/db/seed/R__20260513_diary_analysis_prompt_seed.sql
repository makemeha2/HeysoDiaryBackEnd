SET NAMES utf8mb4;
SET @ADMIN_USER_ID = 1;

INSERT INTO tb_mst_ai_runtime_profile
  (profile_key, profile_name, domain_type, provider, model, temperature, top_p, max_tokens, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('DIARY_ANALYSIS_STRUCTURED_DEFAULT', '일기 장기 메모리 구조화 분석 기본 프로파일', 'DIARY_ANALYSIS', 'OPENAI', 'gpt-4o-mini',
   0.200, NULL, 3000, 'DiaryAnalysisBatch 구조화 JSON 분석 기본값', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  profile_name = VALUES(profile_name),
  domain_type  = VALUES(domain_type),
  provider     = VALUES(provider),
  model        = VALUES(model),
  temperature  = VALUES(temperature),
  top_p        = VALUES(top_p),
  max_tokens   = VALUES(max_tokens),
  description  = VALUES(description),
  is_active    = VALUES(is_active),
  updated_id   = VALUES(updated_id);

INSERT INTO tb_mst_ai_prompt_template
  (template_key, template_name, domain_type, feature_key, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('DIARY_ANALYSIS_STRUCTURED_SYSTEM_ROOT', '일기 장기 메모리 구조화 분석 시스템 프롬프트', 'DIARY_ANALYSIS', 'STRUCTURED_ANALYSIS', 'SYSTEM', 'ROOT',
   '너는 사용자의 일기에서 장기 메모리로 보관할 명시적 사건과 core trait signal을 구조화해 추출하는 분석기다.\n\n원칙:\n- 출력은 JSON object 하나만 반환한다. Markdown, 설명문, code fence를 붙이지 않는다.\n- 일기 원문에 없는 사실을 만들지 않는다.\n- 관계 맥락, 원인/결과, 반복 패턴 후보는 추론 가능성이 있으면 confidence를 낮게 둔다.\n- trait_evidence는 제공된 trait_definitions_json의 trait_key만 사용한다.\n- event_type은 event_type_codes_json의 값만 사용한다.\n- emotion은 null 또는 emotion_codes_json의 값만 사용한다.\n- confidence와 emotion_intensity는 0 이상 1 이하 숫자다.\n- signal_score는 -1 이상 1 이하 숫자다.\n\n반환 스키마:\n{\n  "summary": "일기 분석 요약",\n  "events": [\n    {\n      "event_type": "DIARY_EVENT_TYPE 코드",\n      "event_title": "짧은 사건명 또는 null",\n      "event_summary": "명시 사건 요약",\n      "emotion": "DIARY_EMOTION 코드 또는 null",\n      "emotion_intensity": 0.0,\n      "people": [],\n      "places": [],\n      "activities": [],\n      "relationship": {},\n      "causality": {},\n      "pattern_candidate": {},\n      "confidence": 0.0,\n      "evidence_text": "근거 문장"\n    }\n  ],\n  "trait_evidence": [\n    {\n      "trait_key": "trait_definitions_json에 있는 trait_key",\n      "signal_score": 0.0,\n      "confidence": 0.0,\n      "evidence_text": "근거 문장",\n      "reason": {}\n    }\n  ]\n}',
   'DiaryAnalysisBatch STRUCTURED_ANALYSIS 시스템 지침', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  content     = VALUES(content),
  description = VALUES(description),
  is_active   = VALUES(is_active),
  updated_id  = VALUES(updated_id);

INSERT INTO tb_mst_ai_prompt_template
  (template_key, template_name, domain_type, feature_key, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('DIARY_ANALYSIS_STRUCTURED_USER_ROOT', '일기 장기 메모리 구조화 분석 사용자 프롬프트', 'DIARY_ANALYSIS', 'STRUCTURED_ANALYSIS', 'USER', 'ROOT',
   '[분석 대상 일기]\ndiary_id: {{diary_id}}\nuser_id: {{user_id}}\ndiary_date: {{diary_date}}\ndiary_updated_at: {{diary_updated_at}}\ntitle: {{title}}\nmood_id: {{mood_id}}\ntags_json: {{tags_json}}\ncontent_md:\n{{content_md}}\n\n[허용 event_type_codes_json]\n{{event_type_codes_json}}\n\n[허용 emotion_codes_json]\n{{emotion_codes_json}}\n\n[허용 trait_definitions_json]\n{{trait_definitions_json}}\n\n위 정보만 근거로 장기 메모리 분석 JSON을 생성해라.',
   'DiaryAnalysisBatch STRUCTURED_ANALYSIS 사용자 입력 템플릿', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  content     = VALUES(content),
  description = VALUES(description),
  is_active   = VALUES(is_active),
  updated_id  = VALUES(updated_id);

INSERT INTO tb_mst_ai_prompt_binding
  (binding_name, domain_type, feature_key, system_template_id, user_template_id, runtime_profile_id, description, is_active, created_id, updated_id)
SELECT
  '일기 장기 메모리 구조화 분석 기본 바인딩',
  'DIARY_ANALYSIS',
  'STRUCTURED_ANALYSIS',
  s.template_id,
  u.template_id,
  p.runtime_profile_id,
  'DiaryAnalysisBatch 구조화 분석 바인딩',
  1,
  @ADMIN_USER_ID,
  @ADMIN_USER_ID
FROM tb_mst_ai_prompt_template s
   , tb_mst_ai_prompt_template u
   , tb_mst_ai_runtime_profile p
WHERE s.template_key = 'DIARY_ANALYSIS_STRUCTURED_SYSTEM_ROOT'
  AND u.template_key = 'DIARY_ANALYSIS_STRUCTURED_USER_ROOT'
  AND p.profile_key  = 'DIARY_ANALYSIS_STRUCTURED_DEFAULT'
ON DUPLICATE KEY UPDATE
  system_template_id = VALUES(system_template_id),
  user_template_id   = VALUES(user_template_id),
  runtime_profile_id = VALUES(runtime_profile_id),
  description        = VALUES(description),
  is_active          = VALUES(is_active),
  updated_id         = VALUES(updated_id);
