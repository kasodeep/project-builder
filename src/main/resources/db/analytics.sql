CREATE TABLE project_analytics
(
    project_id      VARCHAR(255) PRIMARY KEY,
    total_tasks     INTEGER     NOT NULL DEFAULT 0,
    completed_tasks INTEGER     NOT NULL DEFAULT 0,
    blocked_tasks   INTEGER     NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE CASCADE
);


CREATE TABLE project_task_status_analytics
(
    project_id VARCHAR(255) NOT NULL,
    status     VARCHAR(50)  NOT NULL,
    task_count INTEGER      NOT NULL DEFAULT 0,
    PRIMARY KEY (project_id, status),
    FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE CASCADE
);

CREATE TABLE feature_analytics
(
    feature_id      VARCHAR(255) PRIMARY KEY,
    total_tasks     INTEGER     NOT NULL DEFAULT 0,
    completed_tasks INTEGER     NOT NULL DEFAULT 0,
    blocked_tasks   INTEGER     NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (feature_id) REFERENCES feature (id) ON DELETE CASCADE
);

CREATE TABLE user_dependency_risk
(
    user_id        VARCHAR(255) PRIMARY KEY,
    blocking_tasks INTEGER     NOT NULL DEFAULT 0,
    blocked_users  INTEGER     NOT NULL DEFAULT 0,
    risk_score     INTEGER     NOT NULL DEFAULT 0,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);
