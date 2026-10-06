-- Выполнить в той же базе и схеме, которые использует HumanityDS.
-- Существующие записи получают version = 0. ID и связи не меняются.
BEGIN;

ALTER TABLE car ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE coordinates ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE human_being ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

COMMIT;
