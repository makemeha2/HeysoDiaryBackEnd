ALTER TABLE tb_diary_event
    ADD KEY idx_diary_event_user_active_updated (user_id, is_active, updated_at);

ALTER TABLE tb_user_memory_snapshot
    ADD KEY idx_user_memory_snapshot_user_active_generated (user_id, is_active, generated_at);
