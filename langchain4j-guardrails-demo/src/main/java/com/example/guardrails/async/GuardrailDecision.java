package com.example.guardrails.async;

public record GuardrailDecision(
        String guardrailName,
        GuardrailSeverity severity,
        String reason,
        String correctiveInstruction,
        String rewrittenContent
) {
    public static GuardrailDecision allow(String name) {
        return new GuardrailDecision(name, GuardrailSeverity.ALLOW, "Allowed", null, null);
    }

    public static GuardrailDecision allowWithRewrite(String name, String rewrittenContent, String reason) {
        return new GuardrailDecision(name, GuardrailSeverity.ALLOW, reason, null, rewrittenContent);
    }

    public static GuardrailDecision warn(String name, String reason) {
        return new GuardrailDecision(name, GuardrailSeverity.WARN, reason, null, null);
    }

    public static GuardrailDecision reprompt(String name, String reason, String correctiveInstruction) {
        return new GuardrailDecision(name, GuardrailSeverity.REPROMPT, reason, correctiveInstruction, null);
    }

    public static GuardrailDecision block(String name, String reason) {
        return new GuardrailDecision(name, GuardrailSeverity.BLOCK, reason, null, null);
    }
}
