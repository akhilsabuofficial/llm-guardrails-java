package com.example.guardrails.async;

public record GuardrailContext(
        String userId,
        String input,
        String modelOutput
) {}
