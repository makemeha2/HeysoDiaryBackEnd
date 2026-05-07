ALTER TABLE tb_diary
    ADD COLUMN mood_id VARCHAR(20) NOT NULL DEFAULT 'none' COMMENT '감정 ID (moodCatalog.id)' AFTER diary_date;
