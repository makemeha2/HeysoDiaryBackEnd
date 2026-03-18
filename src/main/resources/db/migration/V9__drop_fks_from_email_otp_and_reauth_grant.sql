/* ============================================================
   Drop FKs from tb_email_otp / tb_reauth_grant
   ============================================================ */

SET @fk_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tb_email_otp'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
      AND CONSTRAINT_NAME = 'fk_email_otp_user'
);
SET @sql := IF(@fk_exists > 0,
    'ALTER TABLE tb_email_otp DROP FOREIGN KEY fk_email_otp_user',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tb_reauth_grant'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
      AND CONSTRAINT_NAME = 'fk_reauth_grant_user'
);
SET @sql := IF(@fk_exists > 0,
    'ALTER TABLE tb_reauth_grant DROP FOREIGN KEY fk_reauth_grant_user',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tb_reauth_grant'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
      AND CONSTRAINT_NAME = 'fk_reauth_grant_otp'
);
SET @sql := IF(@fk_exists > 0,
    'ALTER TABLE tb_reauth_grant DROP FOREIGN KEY fk_reauth_grant_otp',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
