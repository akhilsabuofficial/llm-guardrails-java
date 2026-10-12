package com.example.guardrails.async;

import java.util.List;

public record OrchestratedDecision(
        StageOutcome outcome,
        List<GuardrailDecision> decisions
) {
    public static OrchestratedDecision allowed(List<GuardrailDecision> decisions) {
        return new OrchestratedDecision(StageOutcome.ALLOWED, decisions);
    }

    public static OrchestratedDecision reprompt(List<GuardrailDecision> decisions) {
        return new OrchestratedDecision(StageOutcome.REPROMPT, decisions);
    }

    public static OrchestratedDecision blocked(List<GuardrailDecision> decisions) {
        return new OrchestratedDecision(StageOutcome.BLOCKED, decisions);
    }
}
