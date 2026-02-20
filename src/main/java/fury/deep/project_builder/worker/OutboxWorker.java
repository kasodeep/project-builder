package fury.deep.project_builder.worker;

import fury.deep.project_builder.entity.outbox.OutboxEvent;
import fury.deep.project_builder.events.AnalyticsEvent;
import fury.deep.project_builder.service.outbox.OutboxService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Transactional outbox worker.
 *
 * <h2>Responsibility</h2>
 * Polls the {@code outbox_event} table on a fixed-delay schedule, publishes
 * claimed events into Spring's internal event bus, and writes the outcome
 * (SUCCESS / FAILED / DEAD) back to the DB.
 *
 * <h2>Scaling design</h2>
 *
 * <h3>1. Atomic claim — {@code pollAndLock}</h3>
 * A single SQL statement does the work of the old poll + N×markProcessing:
 * {@code SKIP LOCKED} means concurrent worker instances on different pods
 * each claim a disjoint subset of rows with zero contention and no duplicate
 * processing.
 *
 * <h3>2. Bulk success flush — {@code bulkMarkSuccess}</h3>
 * All succeeded IDs are collected during the loop and flushed in a single
 * {@code UPDATE … WHERE id IN (…)} at the end — one round-trip regardless of
 * batch size, replacing the old N×markSuccess pattern.
 *
 * <h3>3. Adaptive idle backoff</h3>
 * When consecutive polls return empty batches the worker increments an
 * {@code idleStreak} counter and skips the next {@code 2^streak} cycles
 * (capped at {@code max-idle-skip}). No DB connection is acquired during a
 * skip window. The streak resets to zero the moment any batch contains at
 * least one event, so the worker returns to full polling speed instantly.
 *
 * <pre>
 *   streak 0 → poll every cycle     (base delay, default 2 s)
 *   streak 1 → skip  2 cycles       (~4 s effective pause)
 *   streak 2 → skip  4 cycles       (~8 s)
 *   streak 3 → skip  8 cycles       (~16 s)
 *   streak 4 → skip 16 cycles       (~32 s)  ← default cap
 * </pre>
 * <p>
 * This eliminates thousands of unnecessary round-trips to a remote Postgres
 * instance (e.g. Neon) during quiet periods.
 *
 * <h3>4. DB-authoritative retry count</h3>
 * The dead-letter threshold uses {@code event.getRetryCount()} from the
 * row returned by {@code pollAndLock} — the DB's true state at claim time.
 * {@code incrementRetry} does {@code retry_count = retry_count + 1}
 * server-side, so the count is never stale even when multiple worker
 * instances have previously failed the same event.
 *
 * <h3>5. Separate dead-letter path</h3>
 * Events that exhaust their retries are written with {@code status = 'DEAD'}
 * via a dedicated {@code markDead} statement — not a special-cased error
 * string inside the FAILED row — enabling clean {@code WHERE status = 'DEAD'}
 * queries for ops dashboards and alerting.
 *
 * <h3>6. Micrometer counters</h3>
 * Three counters are incremented inline so failures are visible in
 * dashboards without log-tailing:
 * <ul>
 *   <li>{@code outbox.events.processed} — successfully published</li>
 *   <li>{@code outbox.events.failed}    — processing threw an exception</li>
 *   <li>{@code outbox.events.dead}      — moved to dead-letter queue</li>
 * </ul>
 *
 * <h2>Configuration ({@code application.yml})</h2>
 * <pre>
 * outbox:
 *   worker:
 *     delay-ms:      2000   # ms between cycle end and next cycle start
 *     batch-size:    50     # max rows claimed per cycle
 *     max-retry:     5      # attempts before dead-lettering
 *     max-idle-skip: 16     # max cycles to skip when queue is empty
 * </pre>
 */
@Slf4j
@Component
public class OutboxWorker {

    // Config
    private final int batchSize;
    private final int maxRetry;
    private final int maxIdleSkip;

    /**
     * Number of consecutive empty polls seen in a row.
     * Incremented on every empty batch; reset to 0 when work arrives.
     */
    private final AtomicInteger idleStreak = new AtomicInteger(0);

    /**
     * Remaining cycles to skip before the next DB poll.
     * Set to {@code min(maxIdleSkip, 2^idleStreak)} after each empty poll;
     * decremented each skipped cycle; reset to 0 when work arrives.
     */
    private final AtomicInteger skipCounter = new AtomicInteger(0);

    private final OutboxService outboxService;
    private final ApplicationEventPublisher publisher;

    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Counter deadCounter;

    public OutboxWorker(
            OutboxService outboxService,
            ApplicationEventPublisher publisher,
            MeterRegistry meterRegistry,
            @Value("${outbox.worker.batch-size:50}") int batchSize,
            @Value("${outbox.worker.max-retry:5}") int maxRetry,
            @Value("${outbox.worker.max-idle-skip:16}") int maxIdleSkip
    ) {
        this.outboxService = outboxService;
        this.publisher = publisher;
        this.batchSize = batchSize;
        this.maxRetry = maxRetry;
        this.maxIdleSkip = maxIdleSkip;

        this.processedCounter = meterRegistry.counter("outbox.events.processed");
        this.failedCounter = meterRegistry.counter("outbox.events.failed");
        this.deadCounter = meterRegistry.counter("outbox.events.dead");
    }

    /**
     * Core poll-and-publish cycle.
     *
     * <p>{@code fixedDelay} (not {@code fixedRate}) means the next cycle
     * starts only after the current one fully completes — no overlapping
     * runs on the same node regardless of how long processing takes.
     *
     * <p>Cycle steps:
     * <ol>
     *   <li>If inside an idle skip window, decrement and return immediately — zero DB interaction.</li>
     *   <li>Call {@code pollAndLock} — single atomic SQL, claims the batch.</li>
     *   <li>If empty, increment idle streak, set skip window, return.</li>
     *   <li>Reset idle state (work arrived).</li>
     *   <li>Deserialize and publish each event; collect succeeded IDs.</li>
     *   <li>Bulk-flush all succeeded IDs in one UPDATE round-trip.</li>
     * </ol>
     */
    @Scheduled(fixedDelayString = "${outbox.worker.delay-ms:2000}")
    public void process() {

        // 1. Idle skip window
        if (skipCounter.get() > 0) {
            skipCounter.decrementAndGet();
            return;
        }

        // 2. Claim batch (single atomic SQL)
        List<OutboxEvent> batch = outboxService.pollAndLock(batchSize);

        // 3. Empty → back off
        if (batch.isEmpty()) {
            int streak = idleStreak.incrementAndGet();
            int skip = Math.min(maxIdleSkip, 1 << streak);
            skipCounter.set(skip);
            log.trace("OutboxWorker idle (streak={}, skipping {} cycles)", streak, skip);
            return;
        }

        // 4. Work arrived → reset backoff
        idleStreak.set(0);
        skipCounter.set(0);
        log.debug("OutboxWorker claimed {} event(s)", batch.size());

        // 5. Process each event
        List<UUID> succeeded = new ArrayList<>(batch.size());

        for (OutboxEvent event : batch) {
            try {
                AnalyticsEvent domainEvent = outboxService.deserialize(event);
                publisher.publishEvent(domainEvent);
                succeeded.add(event.getId());
                processedCounter.increment();
            } catch (Exception ex) {
                failedCounter.increment();
                handleFailure(event, ex);
            }
        }

        // 6. Bulk success flush (one round-trip)
        if (!succeeded.isEmpty()) {
            outboxService.bulkMarkSuccess(succeeded);
            log.debug("OutboxWorker marked {}/{} event(s) SUCCESS",
                    succeeded.size(), batch.size());
        }
    }

    /**
     * Decides whether to schedule a retry or dead-letter the event.
     *
     * <p>The threshold check uses {@code event.getRetryCount()} sourced from
     * the DB-locked row returned by {@code pollAndLock} — the authoritative
     * count at claim time.  {@code incrementRetry} increments the counter
     * server-side ({@code retry_count + 1}), so this value is never stale
     * even if another worker previously failed the same event.
     *
     * @param event the failed outbox event (retry_count = DB value at claim)
     * @param ex    the exception that caused the failure
     */
    private void handleFailure(OutboxEvent event, Exception ex) {
        int attemptsAfterThis = event.getRetryCount() + 1;

        if (attemptsAfterThis >= maxRetry) {
            log.error("Outbox event {} dead-lettered after {} attempt(s)",
                    event.getId(), attemptsAfterThis, ex);
            outboxService.markDead(
                    event.getId(),
                    "DEAD after " + attemptsAfterThis + " attempts: " + ex.getMessage());
            deadCounter.increment();
            return;
        }

        long delaySecs = computeBackoffSeconds(attemptsAfterThis);
        Instant retryAt = Instant.now().plusSeconds(delaySecs);

        log.warn("Outbox event {} failed (attempt {}/{}), retrying in {}s",
                event.getId(), attemptsAfterThis, maxRetry - 1, delaySecs, ex);

        outboxService.markFailure(event.getId(), ex.getMessage(), retryAt);
    }

    /**
     * Exponential backoff capped at 1 hour.
     *
     * <p>Uses a left bit-shift ({@code 1L << attempt}) rather than
     * {@code Math.pow(2, attempt)} to avoid floating-point arithmetic.
     *
     * <pre>
     *   attempt 1 →    2 s
     *   attempt 2 →    4 s
     *   attempt 3 →    8 s
     *   attempt 4 →   16 s
     *   attempt 5 →   32 s
     *   attempt 11 → 2048 s → capped at 3600 s (1 h)
     * </pre>
     *
     * @param attempt number of attempts including the one that just failed
     * @return seconds to wait before the next attempt
     */
    private long computeBackoffSeconds(int attempt) {
        return Math.min(3_600L, 1L << attempt);
    }
}