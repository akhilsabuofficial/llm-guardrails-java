package com.example.guardrails.async;

import java.util.List;
import java.util.concurrent.CompletionStage;

public final class AsyncGuardrailOrchestrator {

    private final AsyncGuardrailRunner runner;

    public AsyncGuardrailOrchestrator(AsyncGuardrailRunner runner) {
        this.runner = runner;
    }

    public CompletionStage<OrchestratedDecision> evaluateStage(
            List<AsyncGuardrail> guardrails,
            GuardrailContext context
    ) {
        return runner.runAll(guardrails, context)
                .thenApply(this::aggregate);
    }

    // Alias for evaluating input stage
    public CompletionStage<OrchestratedDecision> evaluateInput(
            List<AsyncGuardrail> guardrails,
            GuardrailContext context
    ) {
        return evaluateStage(guardrails, context);
    }

    public OrchestratedDecision aggregate(List<GuardrailDecision> decisions) {
        boolean hasBlock = decisions.stream()
                .anyMatch(d -> d.severity() == GuardrailSeverity.BLOCK);
        if (hasBlock) {
            return OrchestratedDecision.blocked(decisions);
        }

        boolean hasReprompt = decisions.stream()
                .anyMatch(d -> d.severity() == GuardrailSeverity.REPROMPT);
        if (hasReprompt) {
            return OrchestratedDecision.reprompt(decisions);
        }

        return OrchestratedDecision.allowed(decisions);
    }
}
