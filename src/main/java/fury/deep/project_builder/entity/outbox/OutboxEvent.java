package fury.deep.project_builder.entity.outbox;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an event generated during the task CRUD.
 * Allows for independence between data writing and events firing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    private UUID id;

    private String aggregateType;
    private String aggregateId;
    private String projectId;

    private String eventType;
    private String payload;        // stored as JSONB

    private String status;         // PENDING | PROCESSING | SUCCESS | FAILED | DEAD
    private int retryCount;
    private Instant nextRetryAt;

    private String errorMessage;

    private Instant createdAt;
    private Instant processedAt;

    private int version;        // optimistic lock — used by pollAndLock CAS
}