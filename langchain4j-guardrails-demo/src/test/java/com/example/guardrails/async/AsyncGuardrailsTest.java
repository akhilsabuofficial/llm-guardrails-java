package com.example.guardrails.async;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class AsyncGuardrailsTest {

    private final AsyncGuardrailRunner runner = new AsyncGuardrailRunner();
    private final AsyncGuardrailOrchestrator orchestrator = new AsyncGuardrailOrchestrator(runner);

    @Test
    void shouldAggregatePrecedenceCorrectly_BlockBeatsReprompt() {
        List<GuardrailDecision> decisions = List.of(
                GuardrailDecision.allow("allowCheck"),
                GuardrailDecision.warn("warnCheck", "warning"),
                GuardrailDecision.reprompt("repromptCheck", "bad format", "fix it"),
                GuardrailDecision.block("blockCheck", "fatal injection")
        );

        OrchestratedDecision orchestrated = orchestrator.aggregate(decisions);
        assertEquals(StageOutcome.BLOCKED, orchestrated.outcome());
    }

    @Test
    void shouldTimeoutAndFailClosed() {
        AsyncGuardrail slowGuardrail = new AsyncGuardrail() {
            @Override
            public String name() {
                return "SlowGuardrail";
            }

            @Override
            public CompletableFuture<GuardrailDecision> check(GuardrailContext context) {
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignored) {}
                    return GuardrailDecision.allow(name());
                });
            }
        };

        TimeoutGuardrail timeoutGuardrail = new TimeoutGuardrail(slowGuardrail, 50, true);
        GuardrailDecision decision = timeoutGuardrail.check(new GuardrailContext("u1", "test", null))
                .toCompletableFuture()
                .join();

        assertEquals(GuardrailSeverity.BLOCK, decision.severity());
        assertTrue(decision.reason().contains("failing closed"));
    }

    @Test
    void shouldTimeoutAndFailOpen() {
        AsyncGuardrail slowGuardrail = new AsyncGuardrail() {
            @Override
            public String name() {
                return "SlowGuardrail";
            }

            @Override
            public CompletableFuture<GuardrailDecision> check(GuardrailContext context) {
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignored) {}
                    return GuardrailDecision.allow(name());
                });
            }
        };

        TimeoutGuardrail timeoutGuardrail = new TimeoutGuardrail(slowGuardrail, 50, false);
        GuardrailDecision decision = timeoutGuardrail.check(new GuardrailContext("u1", "test", null))
                .toCompletableFuture()
                .join();

        assertEquals(GuardrailSeverity.WARN, decision.severity());
        assertTrue(decision.reason().contains("failing open"));
    }
}
