INSERT INTO team (name)
VALUES ('finoptech'),
       ('hcdev'),
       ('compliance');

INSERT INTO feature (name)
VALUES ('design'),
       ('code'),
       ('test');

CREATE INDEX idx_project_team_id
    ON project(team_id);

CREATE INDEX idx_pm_project_id
    ON project_manager(project_id);

CREATE INDEX idx_task_dependency_task_id
    ON task_dependency (task_id);
