package com.example.guardrails.sync;

import dev.langchain4j.data.message.AiMessage;

import java.util.regex.Pattern;

import static com.example.guardrails.sync.OutputGuardrailResult.success;
import static com.example.guardrails.sync.OutputGuardrailResult.successWith;

public class PiiRedactorGuardrail implements OutputGuardrail {

    private static final Pattern EMAIL =
        Pattern.compile("[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE =
        Pattern.compile("\\b(\\+?\\d{1,2}[\\s-]?)?(\\(?\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{4})\\b");
    private static final Pattern SSN_LIKE =
        Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern CREDIT_CARD_LIKE =
        Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String original = responseFromLLM.text();
        if (original == null || original.isBlank()) {
            return success();
        }

        String redacted = original;
        redacted = EMAIL.matcher(redacted).replaceAll("[REDACTED_EMAIL]");
        redacted = PHONE.matcher(redacted).replaceAll("[REDACTED_PHONE]");
        redacted = SSN_LIKE.matcher(redacted).replaceAll("[REDACTED_SSN]");
        redacted = CREDIT_CARD_LIKE.matcher(redacted).replaceAll("[REDACTED_CARD]");

        if (!redacted.equals(original)) {
            return successWith(redacted, "PII redacted before returning response");
        }

        return success();
    }
}
