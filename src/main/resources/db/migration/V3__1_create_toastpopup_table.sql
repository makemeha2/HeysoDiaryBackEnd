CREATE TABLE tb_diary_nudge_event_log (
  id            BIGINT NOT NULL AUTO_INCREMENT,
  user_id       BIGINT NOT NULL,
  local_date    DATE NOT NULL COMMENT 'Asia/Seoul 기준 날짜',
  event_type    VARCHAR(20) NOT NULL COMMENT 'shown|closed|dismiss_today|go_write',
  message_key   VARCHAR(64) NULL COMMENT '문구 템플릿 키 (ex: NUDGE_V1_001)',
  message_text  VARCHAR(255) NULL,
  client_time   DATETIME NULL COMMENT '클라이언트 기준 발생 시간(옵션)',
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  metadata_json JSON NULL COMMENT '버전/라우트/디바이스 등 추가정보',

  PRIMARY KEY (id),
  KEY idx_user_date (user_id, local_date),
  KEY idx_user_created (user_id, created_at),
  KEY idx_type_date (event_type, local_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;