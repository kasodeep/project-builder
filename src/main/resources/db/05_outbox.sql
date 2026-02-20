CREATE TABLE outbox_event
(
    id             UUID PRIMARY KEY,

    aggregate_type VARCHAR(100) NOT NULL,                   -- TASK
    aggregate_id   VARCHAR(255) NOT NULL,                   -- taskId
    project_id     VARCHAR(255) NOT NULL,                   -- for partitioning / filtering

    event_type     VARCHAR(255) NOT NULL,                   -- TaskCreatedEvent etc
    payload        JSONB        NOT NULL,                   -- serialized record

    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING | PROCESSING | SUCCESS | FAILED
    retry_count    INTEGER      NOT NULL DEFAULT 0,
    next_retry_at  TIMESTAMPTZ,

    error_message  TEXT,

    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processed_at   TIMESTAMPTZ,

    version        INTEGER      NOT NULL DEFAULT 0          -- optimistic locking
);

-- polling index
CREATE INDEX idx_outbox_status_retry
    ON outbox_event (status, next_retry_at, created_at);

-- aggregate ordering (important for projections)
CREATE INDEX idx_outbox_aggregate
    ON outbox_event (aggregate_type, aggregate_id, created_at);

-- project analytics batching
CREATE INDEX idx_outbox_project
    ON outbox_event (project_id, created_at);

-- JSON search if needed later
CREATE INDEX idx_outbox_payload_gin
    ON outbox_event USING GIN (payload);