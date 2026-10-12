package com.example.guardrails.sync;

import java.util.Objects;

/**
 * Result of evaluating an OutputGuardrail.
 */
public record OutputGuardrailResult(
        boolean isFatal,
        boolean isReprompt,
        String reason,
        String correctiveInstruction,
        String rewrittenContent
) {
    public static OutputGuardrailResult success() {
        return new OutputGuardrailResult(false, false, null, null, null);
    }

    public static OutputGuardrailResult successWith(String rewrittenContent, String reason) {
        return new OutputGuardrailResult(false, false, reason, null, Objects.requireNonNull(rewrittenContent, "rewrittenContent must not be null"));
    }

    public static OutputGuardrailResult reprompt(String reason, String correctiveInstruction) {
        return new OutputGuardrailResult(false, true, reason, Objects.requireNonNull(correctiveInstruction, "correctiveInstruction must not be null"), null);
    }

    public static OutputGuardrailResult fatal(String reason) {
        return new OutputGuardrailResult(true, false, Objects.requireNonNull(reason, "reason must not be null"), null, null);
    }

    public boolean hasRewrittenContent() {
        return rewrittenContent != null;
    }
}
