package com.example.guardrails.sync;

import dev.langchain4j.data.message.AiMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OffTopicFilterGuardrailTest {

    private final OffTopicFilterGuardrail guardrail = new OffTopicFilterGuardrail(List.of("banking", "account", "balance"));

    @Test
    void shouldAllowOnTopic() {
        OutputGuardrailResult result = guardrail.validate(AiMessage.from("Your bank account balance is $500."));
        assertFalse(result.isReprompt());
        assertFalse(result.isFatal());
    }

    @Test
    void shouldRepromptWhenOffTopic() {
        OutputGuardrailResult result = guardrail.validate(AiMessage.from("Here is a recipe for chocolate cake."));
        assertTrue(result.isReprompt());
        assertTrue(result.correctiveInstruction().contains("banking, account, balance"));
    }
}
