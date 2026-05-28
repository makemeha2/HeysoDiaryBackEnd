SET NAMES utf8mb4;
SET time_zone = '+09:00';

ALTER TABLE tb_diary_analysis_state
    ADD KEY idx_diary_analysis_state_user_status_updated (user_id, analysis_status, updated_at);
