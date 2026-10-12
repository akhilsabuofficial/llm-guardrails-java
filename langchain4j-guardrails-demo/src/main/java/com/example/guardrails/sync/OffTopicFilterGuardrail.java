package com.example.guardrails.sync;

import dev.langchain4j.data.message.AiMessage;

import java.util.List;
import java.util.Locale;

import static com.example.guardrails.sync.OutputGuardrailResult.reprompt;
import static com.example.guardrails.sync.OutputGuardrailResult.success;

public class OffTopicFilterGuardrail implements OutputGuardrail {

    private final List<String> allowedTopicKeywords;

    public OffTopicFilterGuardrail(List<String> allowedTopicKeywords) {
        this.allowedTopicKeywords = allowedTopicKeywords;
    }

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String text = responseFromLLM.text();
        if (text == null || text.isBlank()) {
            return success();
        }
        String lowerText = text.toLowerCase(Locale.ROOT);

        boolean matchesAllowedTopic = allowedTopicKeywords.stream()
            .anyMatch(keyword -> lowerText.contains(keyword.toLowerCase(Locale.ROOT)));

        if (!matchesAllowedTopic) {
            return reprompt(
                "Response does not appear related to any allowed topic",
                "Your previous response drifted outside the assistant's supported topics. "
                    + "Politely decline to answer and redirect the user to ask about: "
                    + String.join(", ", allowedTopicKeywords)
            );
        }

        return success();
    }
}
