INSERT INTO team (name)
VALUES ('platform'),
       ('risk'),
       ('growth');

INSERT INTO feature (name)
VALUES ('refactor'),
       ('security'),
       ('automation');


CREATE INDEX idx_project_team_id
    ON project (team_id);

CREATE INDEX idx_pm_project_id
    ON project_manager (project_id);

CREATE INDEX idx_task_dependency_task_id
    ON task_dependency (task_id);

CREATE INDEX idx_task_dependency_depends_on
    ON task_dependency(depends_on_task_id);

ALTER TABLE task
    ADD COLUMN started_at   TIMESTAMPTZ,
    ADD COLUMN completed_at TIMESTAMPTZ;