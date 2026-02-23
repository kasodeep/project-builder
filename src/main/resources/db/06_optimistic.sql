-- ============================================================
-- Migration: add optimistic locking version column to project
-- ============================================================

ALTER TABLE project
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- ============================================================
-- Trigger: auto-increment version on every UPDATE.
-- This means the application never manually sets version —
-- it only sends WHERE version = #{supplied} and checks rows affected.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_increment_project_version()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.version    = OLD.version + 1;
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_project_version
    BEFORE UPDATE
    ON project
    FOR EACH ROW
EXECUTE FUNCTION fn_increment_project_version();