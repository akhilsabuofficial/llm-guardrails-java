package com.example.guardrails.sync;

import dev.langchain4j.data.message.AiMessage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonSchemaGuardrailTest {

    private final JsonSchemaGuardrail guardrail = new JsonSchemaGuardrail(
            Map.of("summary", String.class, "status_code", Integer.class)
    );

    @Test
    void shouldAcceptValidJson() {
        OutputGuardrailResult result = guardrail.validate(
                AiMessage.from("{\"summary\": \"Task complete\", \"status_code\": 200}")
        );
        assertFalse(result.isFatal());
        assertFalse(result.isReprompt());
    }

    @Test
    void shouldRepromptOnInvalidJson() {
        OutputGuardrailResult result = guardrail.validate(AiMessage.from("Not a json string"));
        assertTrue(result.isReprompt());
        assertTrue(result.correctiveInstruction().contains("valid JSON object"));
    }

    @Test
    void shouldRepromptOnMissingField() {
        OutputGuardrailResult result = guardrail.validate(AiMessage.from("{\"summary\": \"Task complete\"}"));
        assertTrue(result.isReprompt());
        assertTrue(result.reason().contains("Missing required field: status_code"));
    }

    @Test
    void shouldRepromptOnTypeMismatch() {
        OutputGuardrailResult result = guardrail.validate(
                AiMessage.from("{\"summary\": \"Task complete\", \"status_code\": \"OK\"}")
        );
        assertTrue(result.isReprompt());
        assertTrue(result.reason().contains("incorrect type"));
    }
}
