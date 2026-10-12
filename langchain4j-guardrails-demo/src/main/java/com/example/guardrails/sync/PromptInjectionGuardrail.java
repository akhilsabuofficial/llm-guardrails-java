package com.example.guardrails.sync;

import dev.langchain4j.data.message.UserMessage;

import java.util.List;
import java.util.regex.Pattern;

import static com.example.guardrails.sync.InputGuardrailResult.fatal;
import static com.example.guardrails.sync.InputGuardrailResult.success;

public class PromptInjectionGuardrail implements InputGuardrail {

    private static final List<Pattern> SUSPICIOUS_PATTERNS = List.of(
        Pattern.compile("(?i)ignore (all|previous|the|all previous) instructions"),
        Pattern.compile("(?i)disregard (your|the) (system|previous) prompt"),
        Pattern.compile("(?i)you are now (in )?(developer|debug|dan) mode"),
        Pattern.compile("(?i)pretend (you are|to be) (not|no longer) (an? )?assistant"),
        Pattern.compile("(?i)reveal (your|the) (system prompt|instructions)")
    );

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String text = userMessage.singleText();
        if (text == null || text.isBlank()) {
            return success();
        }

        for (Pattern pattern : SUSPICIOUS_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return fatal(
                    "Potential prompt injection detected: matched pattern '"
                        + pattern.pattern() + "'"
                );
            }
        }

        return success();
    }
}
