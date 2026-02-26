-- =========================
-- PROJECT HEALTH
-- =========================
CREATE TABLE project_health
(
    project_id    VARCHAR(255) PRIMARY KEY,

    health_score  INTEGER     NOT NULL DEFAULT 100,
    overdue_tasks INTEGER     NOT NULL DEFAULT 0,
    blocked_tasks INTEGER     NOT NULL DEFAULT 0,

    risk_level    VARCHAR(20) NOT NULL DEFAULT 'GREEN',

    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

    FOREIGN KEY (project_id)
        REFERENCES project (id)
        ON DELETE CASCADE
);

-- =========================
-- FLOW METRICS
-- =========================
CREATE TABLE project_flow_metrics
(
    project_id          VARCHAR(255) PRIMARY KEY,

    wip_count           INTEGER        NOT NULL DEFAULT 0,
    throughput_7d       INTEGER        NOT NULL DEFAULT 0,
    throughput_30d      INTEGER        NOT NULL DEFAULT 0,
    avg_cycle_time_days NUMERIC(10, 2) NOT NULL DEFAULT 0,
    reopened_tasks      INTEGER        NOT NULL DEFAULT 0,

    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),

    FOREIGN KEY (project_id)
        REFERENCES project (id)
        ON DELETE CASCADE
);

-- =========================
-- DEPENDENCY RISK
-- =========================
CREATE TABLE project_dependency_risk
(
    project_id               VARCHAR(255) PRIMARY KEY,

    total_dependencies       INTEGER       NOT NULL DEFAULT 0,
    blocked_dependency_count INTEGER       NOT NULL DEFAULT 0,

    critical_path_length     INTEGER       NOT NULL DEFAULT 0,
    dependency_density       NUMERIC(6, 2) NOT NULL DEFAULT 0,
    risk_score               INTEGER       NOT NULL DEFAULT 0,

    updated_at               TIMESTAMPTZ   NOT NULL DEFAULT now(),

    FOREIGN KEY (project_id)
        REFERENCES project (id)
        ON DELETE CASCADE
);

-- =========================
-- TEAM CAPACITY
-- =========================
CREATE TABLE team_capacity_analytics
(
    team_id                  VARCHAR(255) PRIMARY KEY,

    active_projects          INTEGER        NOT NULL DEFAULT 0,
    active_tasks             INTEGER        NOT NULL DEFAULT 0,

    avg_tasks_per_user       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    overloaded_users         INTEGER        NOT NULL DEFAULT 0,
    avg_completion_time_days NUMERIC(10, 2) NOT NULL DEFAULT 0,

    burnout_risk_score       INTEGER        NOT NULL DEFAULT 0,

    updated_at               TIMESTAMPTZ    NOT NULL DEFAULT now(),

    FOREIGN KEY (team_id)
        REFERENCES team (id)
        ON DELETE CASCADE
);

-- =============================================================
-- V2 — Analytics redesign
--   • project_health  : health_score INT → health_grade VARCHAR(2)
--                       + long_running_tasks column (was computed but never stored)
--   • project_flow    : drop reopened_tasks (was always 0)
--   • team_capacity   : avg_tasks_per_user / avg_completion_time_days
--                       precision retained as NUMERIC (were stored as INT)
-- =============================================================

-- ── project_health ──────────────────────────────────────────
ALTER TABLE project_health
    DROP COLUMN health_score,
    ADD  COLUMN health_grade       VARCHAR(2)  NOT NULL DEFAULT 'A',
    ADD  COLUMN long_running_tasks INTEGER     NOT NULL DEFAULT 0;

-- risk_level is now derived from health_grade — keep it so the
-- existing read path doesn't break; we just set it consistently.
-- GREEN → A/B · AMBER → C · RED → D/F
ALTER TABLE project_health
    ALTER COLUMN risk_level SET DEFAULT 'GREEN';

-- ── project_flow_metrics ─────────────────────────────────────
ALTER TABLE project_flow_metrics
    DROP COLUMN reopened_tasks;

-- ── team_capacity_analytics ──────────────────────────────────
-- avg_tasks_per_user and avg_completion_time_days were declared
-- NUMERIC(10,2) in DDL but the Java layer was passing ints,
-- silently truncating decimal precision.  No structural change
-- needed — the fix is in the application layer — but we reset
-- any corrupted rows to 0 so they are recomputed on next event.
UPDATE team_capacity_analytics
SET    avg_tasks_per_user       = 0,
       avg_completion_time_days = 0,
       burnout_risk_score       = 0;