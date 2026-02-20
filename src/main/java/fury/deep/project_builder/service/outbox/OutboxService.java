package fury.deep.project_builder.service.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import fury.deep.project_builder.entity.outbox.OutboxEvent;
import fury.deep.project_builder.events.*;
import fury.deep.project_builder.repository.outbox.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Outbox service class to provide services related to the events.
 *
 * @author night_fury_44
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxMapper mapper;
    private final ObjectMapper objectMapper;

    /**
     * Persist an analytics event into the outbox within the caller's
     * transaction. If JSON serialization fails the exception propagates and
     * rolls back the enclosing transaction — no silent data loss.
     */
    @Transactional
    public void save(String aggregateType, String aggregateId, AnalyticsEvent event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception ex) {
            // Fail loud — a bad payload should never be silently swallowed.
            throw new IllegalStateException(
                    "Failed to serialize outbox event " + event.getClass().getSimpleName(), ex);
        }

        OutboxEvent row = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .projectId(event.projectId())
                .eventType(event.getClass().getSimpleName())
                .payload(payload)
                .status("PENDING")
                .retryCount(0)
                .createdAt(Instant.now())
                .version(0)
                .build();

        mapper.insertEvent(row);
    }

    /**
     * Atomically claim up to {@code limit} due events in a single SQL
     * statement ({@code UPDATE … WHERE id IN (SELECT … FOR UPDATE SKIP LOCKED)
     * RETURNING *}).  No separate {@code markProcessing} call is needed.
     *
     * <p>A new transaction is opened here so the row-level locks are released
     * as soon as this method returns — the worker then processes events
     * outside any DB transaction, keeping connections free.
     */
    @Transactional
    public List<OutboxEvent> pollAndLock(int limit) {
        return mapper.pollAndLock(limit, Instant.now());
    }

    /**
     * Bulk-mark a list of event IDs as SUCCESS in a single UPDATE.
     */
    @Transactional
    public void bulkMarkSuccess(List<UUID> ids) {
        if (ids.isEmpty()) return;
        mapper.bulkMarkSuccess(ids, Instant.now());
    }

    /**
     * Increment the retry counter in the DB (authoritative source — never
     * trust the stale in-memory {@link OutboxEvent#getRetryCount()}) and
     * schedule the next attempt.
     */
    @Transactional
    public void markFailure(UUID id, String error, Instant retryAt) {
        mapper.incrementRetry(id, error, retryAt);
    }

    /**
     * Permanently dead-letter an event. No further processing will occur.
     */
    @Transactional
    public void markDead(UUID id, String error) {
        mapper.markDead(id, error);
    }

    public AnalyticsEvent deserialize(OutboxEvent e) {
        try {
            Class<?> clazz = resolveEventClass(e.getEventType());
            return (AnalyticsEvent) objectMapper.readValue(e.getPayload(), clazz);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to deserialize outbox payload for event " + e.getId(), ex);
        }
    }

    private Class<?> resolveEventClass(String type) {
        return switch (type) {
            case "TaskCreatedEvent" -> TaskCreatedEvent.class;
            case "TaskUpdatedEvent" -> TaskUpdatedEvent.class;
            case "TaskDeletedEvent" -> TaskDeletedEvent.class;
            case "TaskStatusChangedEvent" -> TaskStatusChangedEvent.class;
            case "TaskDependenciesReplacedEvent" -> TaskDependenciesReplacedEvent.class;
            case "TaskAssigneesReplacedEvent" -> TaskAssigneesReplacedEvent.class;
            default -> throw new IllegalArgumentException("Unknown event type: " + type);
        };
    }
}