SET NAMES utf8mb4;
SET time_zone = '+09:00';

SET @drop_admin_batch_execution_requested_by_fk = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLE_CONSTRAINTS
            WHERE CONSTRAINT_SCHEMA = DATABASE()
              AND TABLE_NAME = 'tb_admin_batch_execution'
              AND CONSTRAINT_NAME = 'fk_admin_batch_execution_requested_by'
              AND CONSTRAINT_TYPE = 'FOREIGN KEY'
        ),
        'ALTER TABLE tb_admin_batch_execution DROP FOREIGN KEY fk_admin_batch_execution_requested_by',
        'SELECT 1'
    )
);

PREPARE drop_admin_batch_execution_requested_by_fk
FROM @drop_admin_batch_execution_requested_by_fk;
EXECUTE drop_admin_batch_execution_requested_by_fk;
DEALLOCATE PREPARE drop_admin_batch_execution_requested_by_fk;
