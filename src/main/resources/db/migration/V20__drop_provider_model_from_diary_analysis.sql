SET NAMES utf8mb4;
SET time_zone = '+09:00';

ALTER TABLE tb_diary_analysis
    DROP COLUMN provider,
    DROP COLUMN model;
