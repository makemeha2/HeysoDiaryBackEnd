-- V12__create_ai_template_tables.sql
-- AI Template Management Schema

SET NAMES utf8mb4;

-- ============================================================================
-- 1. Prompt Template Master
-- ============================================================================
CREATE TABLE `tb_mst_ai_prompt_template` (
  `template_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `template_key` VARCHAR(255) NOT NULL COMMENT '템플릿 키(영문 대문자 스네이크, 수정불가, 유일)',
  `template_name` VARCHAR(300) NOT NULL COMMENT '템플릿명',
  `domain_type` VARCHAR(50) NOT NULL COMMENT '도메인 구분 (예: DIARY_AI, DIARY_AI_POLISH, DIARY_NUDGE)',
  `feature_key` VARCHAR(255) DEFAULT NULL COMMENT '기능 키(관리 편의를 위한 참조용, 자유입력)',
  `template_role` VARCHAR(20) NOT NULL COMMENT '템플릿 역할(SYSTEM, USER, COMPONENT)',
  `template_type` VARCHAR(20) NOT NULL COMMENT '템플릿 타입(ROOT, FRAGMENT)',
  `content` MEDIUMTEXT NOT NULL COMMENT '템플릿 본문(Mustache-lite: {{variable}}, {{> template_key}})',
  `variables_schema_json` JSON DEFAULT NULL COMMENT '변수 스키마 저장용(JSON, MVP에서는 저장만 수행)',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT '설명',
  `revision_no` INT NOT NULL DEFAULT 1 COMMENT '개정번호(현재는 의미 약함, 자유수정 가능)',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '사용여부(1=사용, 0=미사용/soft delete)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `created_id` BIGINT NOT NULL COMMENT '생성자 userId',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  `updated_id` BIGINT NOT NULL COMMENT '수정자 userId',
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_ai_prompt_template_key` (`template_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 프롬프트 템플릿 마스터';

-- ============================================================================
-- 2. Prompt Template Relation Master
--    ROOT -> FRAGMENT 관계만 허용
--    FRAGMENT는 하위 FRAGMENT를 가질 수 없음 (애플리케이션에서 검증)
-- ============================================================================
CREATE TABLE `tb_mst_ai_prompt_template_rel` (
  `rel_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `parent_template_id` BIGINT NOT NULL COMMENT '부모 템플릿 ID(ROOT)',
  `child_template_id` BIGINT NOT NULL COMMENT '자식 템플릿 ID(FRAGMENT)',
  `merge_type` VARCHAR(20) NOT NULL COMMENT '조합 방식(APPEND, PREPEND)',
  `sort_seq` INT NOT NULL DEFAULT 0 COMMENT '정렬 순서(중복 허용)',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '사용여부(1=사용, 0=미사용/soft delete)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `created_id` BIGINT NOT NULL COMMENT '생성자 userId',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  `updated_id` BIGINT NOT NULL COMMENT '수정자 userId',
  PRIMARY KEY (`rel_id`),
  KEY `idx_ai_prompt_template_rel_parent_active_sort` (`parent_template_id`, `is_active`, `sort_seq`),
  KEY `idx_ai_prompt_template_rel_child` (`child_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 프롬프트 템플릿 관계 마스터(ROOT-FRAGMENT)';

-- ============================================================================
-- 3. Runtime Profile Master
--    Binding은 이 프로파일을 항상 따르며, 서비스 override는 허용하지 않음.
-- ============================================================================
CREATE TABLE `tb_mst_ai_runtime_profile` (
  `runtime_profile_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `profile_key` VARCHAR(255) NOT NULL COMMENT '프로파일 키(영문 대문자 스네이크, 수정불가, 유일)',
  `profile_name` VARCHAR(300) NOT NULL COMMENT '프로파일명',
  `domain_type` VARCHAR(50) NOT NULL COMMENT '도메인 구분 (예: DIARY_AI, DIARY_AI_POLISH, DIARY_NUDGE)',
  `provider` VARCHAR(50) DEFAULT NULL COMMENT 'AI 제공자(예: OPENAI)',
  `model` VARCHAR(200) NOT NULL COMMENT '모델명(필수)',
  `temperature` DECIMAL(5,3) DEFAULT NULL COMMENT 'temperature(nullable)',
  `top_p` DECIMAL(5,3) DEFAULT NULL COMMENT 'top_p(nullable)',
  `max_tokens` INT DEFAULT NULL COMMENT 'max_tokens(nullable)',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT '설명',
  `revision_no` INT NOT NULL DEFAULT 1 COMMENT '개정번호(현재는 의미 약함, 자유수정 가능)',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '사용여부(1=사용, 0=미사용/soft delete)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `created_id` BIGINT NOT NULL COMMENT '생성자 userId',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  `updated_id` BIGINT NOT NULL COMMENT '수정자 userId',
  PRIMARY KEY (`runtime_profile_id`),
  UNIQUE KEY `uk_ai_runtime_profile_key` (`profile_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 런타임 프로파일 마스터';

-- ============================================================================
-- 4. Prompt Binding Master
--    domain + feature 단위 1건 고정
--    system_template_id / user_template_id / runtime_profile_id 모두 필수
-- ============================================================================
CREATE TABLE `tb_mst_ai_prompt_binding` (
  `binding_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `binding_name` VARCHAR(300) NOT NULL COMMENT '바인딩명',
  `domain_type` VARCHAR(50) NOT NULL COMMENT '도메인 구분',
  `feature_key` VARCHAR(255) NOT NULL COMMENT '기능 키(자유입력, domain 내 유일)',
  `system_template_id` BIGINT NOT NULL COMMENT 'SYSTEM ROOT 템플릿 ID',
  `user_template_id` BIGINT NOT NULL COMMENT 'USER ROOT 템플릿 ID',
  `runtime_profile_id` BIGINT NOT NULL COMMENT '런타임 프로파일 ID',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT '설명',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '사용여부(1=사용, 0=미사용/soft delete)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  `created_id` BIGINT NOT NULL COMMENT '생성자 userId',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  `updated_id` BIGINT NOT NULL COMMENT '수정자 userId',
  PRIMARY KEY (`binding_id`),
  UNIQUE KEY `uk_ai_prompt_binding_domain_feature` (`domain_type`, `feature_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 프롬프트 바인딩 마스터';

-- ============================================================================
-- 5. Existing Table Alter - tb_diary_ai_run
--    실행 시점 참조 ID를 보관한다. (PK: run_id)
-- ============================================================================
ALTER TABLE `tb_diary_ai_run`
  ADD COLUMN `binding_id` BIGINT DEFAULT NULL COMMENT '실행 시 사용한 binding ID' AFTER `run_id`,
  ADD COLUMN `system_template_id` BIGINT DEFAULT NULL COMMENT '실행 시 사용한 system template ID' AFTER `binding_id`,
  ADD COLUMN `user_template_id` BIGINT DEFAULT NULL COMMENT '실행 시 사용한 user template ID' AFTER `system_template_id`,
  ADD COLUMN `runtime_profile_id` BIGINT DEFAULT NULL COMMENT '실행 시 사용한 runtime profile ID' AFTER `user_template_id`;

-- ============================================================================
-- 6. Existing Table Alter - tb_diary_ai_polish_log
--    실행 시점 참조 ID를 보관한다. (PK: id)
-- ============================================================================
ALTER TABLE `tb_diary_ai_polish_log`
  ADD COLUMN `binding_id` BIGINT DEFAULT NULL COMMENT '실행 시 사용한 binding ID' AFTER `id`,
  ADD COLUMN `system_template_id` BIGINT DEFAULT NULL COMMENT '실행 시 사용한 system template ID' AFTER `binding_id`,
  ADD COLUMN `user_template_id` BIGINT DEFAULT NULL COMMENT '실행 시 사용한 user template ID' AFTER `system_template_id`,
  ADD COLUMN `runtime_profile_id` BIGINT DEFAULT NULL COMMENT '실행 시 사용한 runtime profile ID' AFTER `user_template_id`;
