-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================
-- TEAM
-- =========================
CREATE TABLE team
(
    id   VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,

    CONSTRAINT unique_team UNIQUE (name)
);

-- =========================
-- FEATURE
-- =========================
CREATE TABLE feature
(
    id   VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL
);

-- =========================
-- USER
-- =========================
CREATE TABLE "user"
(
    id       VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) NOT NULL,
    email    VARCHAR(255),
    password VARCHAR(255),
    role     VARCHAR(255),
    team_id  VARCHAR(255),

    CONSTRAINT unique_username UNIQUE (username),
    CONSTRAINT fk_user_team
        FOREIGN KEY (team_id) REFERENCES team (id)
);

-- =========================
-- PROJECT
-- =========================
CREATE TABLE project
(
    id         VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       VARCHAR(255) NOT NULL,
    team_id    VARCHAR(255) NOT NULL,
    owner      VARCHAR(255) NOT NULL,
    progress   INTEGER CHECK (progress BETWEEN 0 AND 100),

    start_date DATE         NOT NULL,
    end_date   DATE         NOT NULL,

    updated_by VARCHAR(255),
    updated_at TIMESTAMPTZ  NOT NULL    DEFAULT now(),

    CONSTRAINT fk_project_team
        FOREIGN KEY (team_id) REFERENCES team (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_project_dates
        CHECK (end_date >= start_date)
);

-- =========================
-- PROJECT MANAGER
-- =========================
CREATE TABLE project_manager
(
    project_id VARCHAR(255) NOT NULL,
    user_id    VARCHAR(255) NOT NULL,

    PRIMARY KEY (project_id, user_id),

    CONSTRAINT fk_pm_project
        FOREIGN KEY (project_id) REFERENCES project (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_pm_user
        FOREIGN KEY (user_id) REFERENCES "user" (id)
            ON DELETE CASCADE
);

-- =========================
-- TASK
-- =========================
CREATE TABLE task
(
    id           VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id   VARCHAR(255) NOT NULL,
    feature_id   VARCHAR(255),

    name         VARCHAR(255) NOT NULL,
    priority     INTEGER,
    status       VARCHAR(50)  NOT NULL,

    start_date   DATE,
    end_date     DATE,

    started_at   TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,

    updated_by   VARCHAR(255),
    updated_at   TIMESTAMPTZ  NOT NULL    DEFAULT now(),

    CONSTRAINT fk_task_project
        FOREIGN KEY (project_id) REFERENCES project (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_task_feature
        FOREIGN KEY (feature_id) REFERENCES feature (id),

    CONSTRAINT chk_task_dates
        CHECK (
            end_date IS NULL
                OR start_date IS NULL
                OR end_date >= start_date
            )
);

-- =========================
-- TASK ASSIGNEE
-- =========================
CREATE TABLE task_assignee
(
    task_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,

    PRIMARY KEY (task_id, user_id),

    CONSTRAINT fk_ta_task
        FOREIGN KEY (task_id) REFERENCES task (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_ta_user
        FOREIGN KEY (user_id) REFERENCES "user" (id)
            ON DELETE CASCADE
);

-- =========================
-- TASK DEPENDENCY
-- =========================
CREATE TABLE task_dependency
(
    task_id            VARCHAR(255) NOT NULL,
    depends_on_task_id VARCHAR(255) NOT NULL,

    PRIMARY KEY (task_id, depends_on_task_id),

    CONSTRAINT fk_td_task
        FOREIGN KEY (task_id) REFERENCES task (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_td_depends_on
        FOREIGN KEY (depends_on_task_id) REFERENCES task (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_no_self_dependency
        CHECK (task_id <> depends_on_task_id)
);
