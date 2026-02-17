-- =========================
-- PERFORMANCE INDEXES
-- =========================

-- User

CREATE UNIQUE INDEX idx_user_username
    ON "user" (username);

-- Project
-- For loading all projects of a team
CREATE INDEX idx_project_team_updated
    ON project (team_id, updated_at DESC);

-- Project Manager
-- Since PK uses project_id first
CREATE INDEX idx_pm_user_id
    ON project_manager (user_id);

-- Task
CREATE INDEX idx_task_project_id_id
ON task(project_id, id);

-- Task Assignee
CREATE INDEX idx_ta_user_id
    ON task_assignee (user_id);

-- Task Dependency
CREATE INDEX idx_task_dependency_task_id
    ON task_dependency (depends_on_task_id);

-- =========================
-- INITIAL DATA
-- =========================

INSERT INTO team (name)
VALUES ('platform'),
       ('risk'),
       ('growth');

INSERT INTO feature (name)
VALUES ('refactor'),
       ('security'),
       ('automation');
