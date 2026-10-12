package com.example.guardrails.sync;

import dev.langchain4j.data.message.AiMessage;

/**
 * Interface for synchronous output validation.
 */
public interface OutputGuardrail {
    OutputGuardrailResult validate(AiMessage responseFromLLM);
}
