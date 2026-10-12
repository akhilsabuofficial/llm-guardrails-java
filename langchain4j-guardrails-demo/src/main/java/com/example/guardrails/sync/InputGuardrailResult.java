package com.example.guardrails.sync;

import java.util.Objects;

/**
 * Result of evaluating an InputGuardrail.
 */
public record InputGuardrailResult(
        boolean isFatal,
        String reason
) {
    public static InputGuardrailResult success() {
        return new InputGuardrailResult(false, null);
    }

    public static InputGuardrailResult fatal(String reason) {
        return new InputGuardrailResult(true, Objects.requireNonNull(reason, "reason must not be null"));
    }
}
