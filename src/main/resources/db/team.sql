INSERT INTO team (name)
VALUES ('finoptech'),
       ('hcdev'),
       ('compliance');

CREATE INDEX idx_project_team_id
    ON project(team_id);

CREATE INDEX idx_pm_project_id
    ON project_manager(project_id);
