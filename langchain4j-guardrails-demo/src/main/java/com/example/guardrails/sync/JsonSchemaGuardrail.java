package com.example.guardrails.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;

import java.util.Map;

import static com.example.guardrails.sync.OutputGuardrailResult.reprompt;
import static com.example.guardrails.sync.OutputGuardrailResult.success;

public class JsonSchemaGuardrail implements OutputGuardrail {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Class<?>> requiredFields;

    public JsonSchemaGuardrail(Map<String, Class<?>> requiredFields) {
        this.requiredFields = requiredFields;
    }

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String text = responseFromLLM.text();
        if (text == null || text.isBlank()) {
            return reprompt(
                "Empty response; expected JSON object",
                "Your previous response was empty. Return a valid JSON object matching the required schema."
            );
        }
        text = text.trim();

        JsonNode root;
        try {
            root = objectMapper.readTree(text);
        } catch (Exception e) {
            return reprompt(
                "Response was not valid JSON: " + e.getMessage(),
                "Your previous response was not valid JSON. Return ONLY a valid JSON object matching the required schema."
            );
        }

        for (Map.Entry<String, Class<?>> entry : requiredFields.entrySet()) {
            String field = entry.getKey();
            Class<?> expectedType = entry.getValue();

            if (!root.has(field)) {
                return reprompt(
                    "Missing required field: " + field,
                    "Your previous response was missing the field '" + field
                        + "'. Return the full JSON object including all required fields."
                );
            }

            JsonNode value = root.get(field);
            if (!isCompatibleType(value, expectedType)) {
                return reprompt(
                    "Field '" + field + "' has incorrect type",
                    "The field '" + field + "' should be of type " + expectedType.getSimpleName()
                        + ". Return the corrected JSON object."
                );
            }
        }

        return success();
    }

    private boolean isCompatibleType(JsonNode value, Class<?> expectedType) {
        if (expectedType == String.class) return value.isTextual();
        if (expectedType == Integer.class || expectedType == Long.class) return value.isIntegralNumber();
        if (expectedType == Double.class || expectedType == Float.class) return value.isNumber();
        if (expectedType == Boolean.class) return value.isBoolean();
        return true;
    }
}
