SET NAMES utf8mb4;
SET @ADMIN_USER_ID = 1;

INSERT INTO tb_mst_ai_runtime_profile
  (profile_key, profile_name, domain_type, provider, model, temperature, top_p, max_tokens, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('USER_MEMORY_SNAPSHOT_DEFAULT', '사용자 장기 메모리 snapshot 기본 프로파일', 'USER_MEMORY', 'OPENAI', 'gpt-4o-mini',
   0.200, NULL, 2500, 'UserMemorySnapshotBatch 요약 JSON 기본값', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
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
  ('USER_MEMORY_SNAPSHOT_SYSTEM_ROOT', '사용자 장기 메모리 snapshot 시스템 프롬프트', 'USER_MEMORY', 'SNAPSHOT_SUMMARY', 'SYSTEM', 'ROOT',
   '너는 사용자의 장기 메모리 입력을 AI 피드백에서 읽기 쉬운 snapshot으로 요약하는 분석기다.\n\n원칙:\n- 출력은 JSON object 하나만 반환한다. Markdown, 설명문, code fence를 붙이지 않는다.\n- 입력 events_json과 trait_profiles_json에 없는 사실을 만들지 않는다.\n- 최근 주요 사건, 반복 주제, 스트레스 요인, 회복 요인, 중요한 사람, trait 요약을 간결하게 정리한다.\n- 불확실한 내용은 낮은 confidence 또는 cautious wording으로 표현한다.\n- 모든 배열 항목은 evidence 또는 source_count처럼 입력 근거를 추적할 수 있는 값을 포함한다.\n\n반환 스키마:\n{\n  "summary": "전체 장기 메모리 요약",\n  "recurring_themes": [],\n  "important_people": [],\n  "stress_factors": [],\n  "recovery_factors": [],\n  "trait_summary": []\n}',
   'UserMemorySnapshotBatch SNAPSHOT_SUMMARY 시스템 지침', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  content     = VALUES(content),
  description = VALUES(description),
  is_active   = VALUES(is_active),
  updated_id  = VALUES(updated_id);

INSERT INTO tb_mst_ai_prompt_template
  (template_key, template_name, domain_type, feature_key, template_role, template_type, content, description, revision_no, is_active, created_id, updated_id)
VALUES
  ('USER_MEMORY_SNAPSHOT_USER_ROOT', '사용자 장기 메모리 snapshot 사용자 프롬프트', 'USER_MEMORY', 'SNAPSHOT_SUMMARY', 'USER', 'ROOT',
   '[source window]\nsource_from_date: {{source_from_date}}\nsource_to_date: {{source_to_date}}\n\n[active events json]\n{{events_json}}\n\n[active trait profiles json]\n{{trait_profiles_json}}\n\n위 입력만 근거로 장기 메모리 snapshot JSON을 생성해라.',
   'UserMemorySnapshotBatch SNAPSHOT_SUMMARY 사용자 입력 템플릿', 1, 1, @ADMIN_USER_ID, @ADMIN_USER_ID)
ON DUPLICATE KEY UPDATE
  content     = VALUES(content),
  description = VALUES(description),
  is_active   = VALUES(is_active),
  updated_id  = VALUES(updated_id);

INSERT INTO tb_mst_ai_prompt_binding
  (binding_name, domain_type, feature_key, system_template_id, user_template_id, runtime_profile_id, description, is_active, created_id, updated_id)
SELECT
  '사용자 장기 메모리 snapshot 기본 바인딩',
  'USER_MEMORY',
  'SNAPSHOT_SUMMARY',
  s.template_id,
  u.template_id,
  p.runtime_profile_id,
  'UserMemorySnapshotBatch snapshot 요약 바인딩',
  1,
  @ADMIN_USER_ID,
  @ADMIN_USER_ID
FROM tb_mst_ai_prompt_template s
   , tb_mst_ai_prompt_template u
   , tb_mst_ai_runtime_profile p
WHERE s.template_key = 'USER_MEMORY_SNAPSHOT_SYSTEM_ROOT'
  AND u.template_key = 'USER_MEMORY_SNAPSHOT_USER_ROOT'
  AND p.profile_key  = 'USER_MEMORY_SNAPSHOT_DEFAULT'
ON DUPLICATE KEY UPDATE
  system_template_id = VALUES(system_template_id),
  user_template_id   = VALUES(user_template_id),
  runtime_profile_id = VALUES(runtime_profile_id),
  description        = VALUES(description),
  is_active          = VALUES(is_active),
  updated_id         = VALUES(updated_id);
