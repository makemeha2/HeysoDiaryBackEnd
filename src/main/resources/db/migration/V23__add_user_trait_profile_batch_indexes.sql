SET NAMES utf8mb4;
SET time_zone = '+09:00';

ALTER TABLE tb_diary_trait_evidence
    ADD KEY idx_trait_evidence_user_updated (user_id, updated_at);

ALTER TABLE tb_user_trait_profile
    ADD KEY idx_user_trait_profile_user_updated (user_id, updated_at);
