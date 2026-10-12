package com.example.guardrails.sync;

import dev.langchain4j.data.message.AiMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PiiRedactorGuardrailTest {

    private final PiiRedactorGuardrail guardrail = new PiiRedactorGuardrail();

    @Test
    void shouldPassCleanTextUnmodified() {
        OutputGuardrailResult result = guardrail.validate(AiMessage.from("Everything is in order."));
        assertFalse(result.isFatal());
        assertFalse(result.isReprompt());
        assertFalse(result.hasRewrittenContent());
    }

    @Test
    void shouldRedactEmailAndPhone() {
        OutputGuardrailResult result = guardrail.validate(AiMessage.from("Contact alice@example.com or call 123-456-7890"));
        assertFalse(result.isFatal());
        assertTrue(result.hasRewrittenContent());
        assertEquals("Contact [REDACTED_EMAIL] or call [REDACTED_PHONE]", result.rewrittenContent());
    }

    @Test
    void shouldRedactSSN() {
        OutputGuardrailResult result = guardrail.validate(AiMessage.from("SSN is 123-45-6789"));
        assertTrue(result.hasRewrittenContent());
        assertEquals("SSN is [REDACTED_SSN]", result.rewrittenContent());
    }
}
