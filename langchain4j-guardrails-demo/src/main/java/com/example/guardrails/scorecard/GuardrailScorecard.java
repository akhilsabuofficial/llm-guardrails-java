package com.example.guardrails.scorecard;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class GuardrailScorecard {

    public record LogEntry(Instant timestamp, String category, String guardrailName, String reason) {}

    private final List<LogEntry> entries = new CopyOnWriteArrayList<>();
    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger retryCount = new AtomicInteger();
    private final AtomicInteger blockedCount = new AtomicInteger();
    private final AtomicInteger fallbackCount = new AtomicInteger();

    public void recordSuccess() {
        successCount.incrementAndGet();
        entries.add(new LogEntry(Instant.now(), "SUCCESS", null, null));
    }

    public void recordRetry(String guardrailName, String reason) {
        retryCount.incrementAndGet();
        entries.add(new LogEntry(Instant.now(), "RETRY", guardrailName, reason));
    }

    public void recordBlocked(String guardrailName, String reason) {
        blockedCount.incrementAndGet();
        entries.add(new LogEntry(Instant.now(), "BLOCKED", guardrailName, reason));
    }

    public void recordFallback(String reason) {
        fallbackCount.incrementAndGet();
        entries.add(new LogEntry(Instant.now(), "FALLBACK", null, reason));
    }

    public List<LogEntry> getEntries() {
        return List.copyOf(entries);
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    public int getBlockedCount() {
        return blockedCount.get();
    }

    public int getFallbackCount() {
        return fallbackCount.get();
    }

    public String summary() {
        return String.format(
            "Success: %d | Retries: %d | Blocked: %d | Fallbacks: %d",
            successCount.get(), retryCount.get(), blockedCount.get(), fallbackCount.get()
        );
    }
}
