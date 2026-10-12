package com.example.guardrails.async;

import java.util.concurrent.CompletionStage;

public interface AsyncGuardrail {
    String name();

    CompletionStage<GuardrailDecision> check(GuardrailContext context);
}
