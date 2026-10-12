package com.example.guardrails.sync;

import dev.langchain4j.data.message.UserMessage;

/**
 * Interface for synchronous input validation.
 */
public interface InputGuardrail {
    InputGuardrailResult validate(UserMessage userMessage);
}
