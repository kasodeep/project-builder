package fury.deep.project_builder.repository.outbox;

import fury.deep.project_builder.entity.outbox.OutboxEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper
public interface OutboxMapper {

    /**
     * Persist a new outbox row.
     */
    void insertEvent(@Param("event") OutboxEvent event);

    /**
     * Atomically claim up to {@code limit} PENDING/FAILED rows that are due
     * for processing. Uses {@code SELECT … FOR UPDATE SKIP LOCKED} so
     * concurrent workers partition work without contention.
     * Returns the claimed rows with status already flipped to PROCESSING.
     */
    List<OutboxEvent> pollAndLock(@Param("limit") int limit, @Param("now") Instant now);

    /**
     * Bulk-mark a set of events as SUCCESS in a single round-trip.
     * Use after a full batch has been published successfully.
     */
    void bulkMarkSuccess(
            @Param("ids") List<UUID> ids,
            @Param("processedAt") Instant processedAt);

    /**
     * Increment retry counter and schedule next attempt.
     * The DB owns the authoritative retry_count — never trust the in-memory value.
     */
    void incrementRetry(
            @Param("id") UUID id,
            @Param("error") String error,
            @Param("nextRetryAt") Instant nextRetryAt);

    /**
     * Move an event to DEAD — no further processing.
     */
    void markDead(
            @Param("id") UUID id,
            @Param("error") String error);
}