SET NAMES utf8mb4;
SET time_zone = '+09:00';

-- =========================================================
-- 1) 공통코드 그룹
-- =========================================================


-- =========================================================
-- 2) 공통코드(선택지)
-- =========================================================

-- 말투
INSERT INTO tb_common_code (
  group_id, code_id, code_name, is_active,
  extra_info1, extra_info2, sort_seq,
  created_at, created_id, updated_at, updated_id
)
SELECT * FROM (
  SELECT 'AIFB_SPEECH_TONE','POLITE','정중하게',1,NULL,NULL,10,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
  UNION ALL
  SELECT 'AIFB_SPEECH_TONE','CASUAL','편하게',  1,NULL,NULL,20,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
) v(group_id, code_id, code_name, is_active, extra_info1, extra_info2, sort_seq, created_at, created_id, updated_at, updated_id)
WHERE NOT EXISTS (
  SELECT 1 FROM tb_common_code c
  WHERE c.group_id = v.group_id AND c.code_id = v.code_id
);

-- 피드백 스타일
INSERT INTO tb_common_code (
  group_id, code_id, code_name, is_active,
  extra_info1, extra_info2, sort_seq,
  created_at, created_id, updated_at, updated_id
)
SELECT * FROM (
  SELECT 'AIFB_STYLE','EMPATHY','공감',1,NULL,NULL,10,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
  UNION ALL
  SELECT 'AIFB_STYLE','BALANCED','균형',1,NULL,NULL,20,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
  UNION ALL
  SELECT 'AIFB_STYLE','SOLUTION','해결',1,NULL,NULL,30,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
) v(group_id, code_id, code_name, is_active, extra_info1, extra_info2, sort_seq, created_at, created_id, updated_at, updated_id)
WHERE NOT EXISTS (
  SELECT 1 FROM tb_common_code c
  WHERE c.group_id = v.group_id AND c.code_id = v.code_id
);

-- 강도
INSERT INTO tb_common_code (
  group_id, code_id, code_name, is_active,
  extra_info1, extra_info2, sort_seq,
  created_at, created_id, updated_at, updated_id
)
SELECT * FROM (
  SELECT 'AIFB_INTENSITY','SOFT','부드럽게',1,NULL,NULL,10,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
  UNION ALL
  SELECT 'AIFB_INTENSITY','NORMAL','보통',   1,NULL,NULL,20,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
  UNION ALL
  SELECT 'AIFB_INTENSITY','DIRECT','직설적', 1,NULL,NULL,30,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
) v(group_id, code_id, code_name, is_active, extra_info1, extra_info2, sort_seq, created_at, created_id, updated_at, updated_id)
WHERE NOT EXISTS (
  SELECT 1 FROM tb_common_code c
  WHERE c.group_id = v.group_id AND c.code_id = v.code_id
);

-- 질문
INSERT INTO tb_common_code (
  group_id, code_id, code_name, is_active,
  extra_info1, extra_info2, sort_seq,
  created_at, created_id, updated_at, updated_id
)
SELECT * FROM (
  SELECT 'AIFB_QUESTION','NONE','없음',1,NULL,NULL,10,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
  UNION ALL
  SELECT 'AIFB_QUESTION','ASK','있음', 1,NULL,NULL,20,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
) v(group_id, code_id, code_name, is_active, extra_info1, extra_info2, sort_seq, created_at, created_id, updated_at, updated_id)
WHERE NOT EXISTS (
  SELECT 1 FROM tb_common_code c
  WHERE c.group_id = v.group_id AND c.code_id = v.code_id
);

-- 길이
INSERT INTO tb_common_code (
  group_id, code_id, code_name, is_active,
  extra_info1, extra_info2, sort_seq,
  created_at, created_id, updated_at, updated_id
)
SELECT * FROM (
  SELECT 'AIFB_LENGTH','SHORT','짧게',  1,NULL,NULL,10,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
  UNION ALL
  SELECT 'AIFB_LENGTH','MEDIUM','보통', 1,NULL,NULL,20,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
  UNION ALL
  SELECT 'AIFB_LENGTH','LONG','자세히', 1,NULL,NULL,30,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
) v(group_id, code_id, code_name, is_active, extra_info1, extra_info2, sort_seq, created_at, created_id, updated_at, updated_id)
WHERE NOT EXISTS (
  SELECT 1 FROM tb_common_code c
  WHERE c.group_id = v.group_id AND c.code_id = v.code_id
);

-- 언어 모드
INSERT INTO tb_common_code (
  group_id, code_id, code_name, is_active,
  extra_info1, extra_info2, sort_seq,
  created_at, created_id, updated_at, updated_id
)
SELECT * FROM (
  SELECT 'AIFB_LANG_MODE' as group_id,'FOLLOW_DIARY' as code_id,'일기 언어 따라가기' as code_name,1,null as extra_info1,null as extra_info2,10,current_timestamp as created_at,0 as created_id,current_timestamp as updated_at,0 as updated_id
  UNION ALL
  SELECT 'AIFB_LANG_MODE','FIXED','고정언어',               1,NULL,NULL,20,CURRENT_TIMESTAMP,0,CURRENT_TIMESTAMP,0
) v
WHERE NOT EXISTS (
  SELECT 1 FROM tb_common_code c
  WHERE c.group_id = v.group_id AND c.code_id = v.code_id
);

-- =========================================================
-- 3) 사용자 설정 테이블
-- =========================================================
CREATE TABLE IF NOT EXISTS tb_user_ai_feedback_setting (
  user_id           BIGINT UNSIGNED NOT NULL COMMENT 'tb_user.user_id (logical FK)',

  speech_tone_cd    VARCHAR(30) NOT NULL DEFAULT 'POLITE'
                    COMMENT 'AIFB_SPEECH_TONE.code_id',

  feedback_style_cd VARCHAR(30) NOT NULL DEFAULT 'BALANCED'
                    COMMENT 'AIFB_STYLE.code_id',

  intensity_cd      VARCHAR(30) NOT NULL DEFAULT 'NORMAL'
                    COMMENT 'AIFB_INTENSITY.code_id',

  question_cd       VARCHAR(30) NOT NULL DEFAULT 'ASK'
                    COMMENT 'AIFB_QUESTION.code_id',

  length_cd         VARCHAR(30) NOT NULL DEFAULT 'MEDIUM'
                    COMMENT 'AIFB_LENGTH.code_id',

  lang_mode_cd      VARCHAR(30) NOT NULL DEFAULT 'FOLLOW_DIARY'
                    COMMENT 'AIFB_LANG_MODE.code_id',

  fixed_lang        VARCHAR(10) NULL COMMENT '고정언어 코드(예: ko, en) - lang_mode_cd=FIXED 일 때 사용',

  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

  PRIMARY KEY (user_id),
  KEY idx_uafs_style (feedback_style_cd),
  KEY idx_uafs_lang_mode (lang_mode_cd)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자 AI 피드백 설정(공통코드 기반)';