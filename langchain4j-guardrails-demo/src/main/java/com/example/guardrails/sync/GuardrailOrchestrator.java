package com.example.guardrails.sync;

import com.example.guardrails.scorecard.GuardrailScorecard;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.util.List;

public class GuardrailOrchestrator {

    private final ChatLanguageModel model;
    private final List<InputGuardrail> inputGuardrails;
    private final List<OutputGuardrail> outputGuardrails;
    private final int maxRetries;
    private final String fallbackResponse;
    private final GuardrailScorecard scorecard;

    public GuardrailOrchestrator(ChatLanguageModel model,
                                 List<InputGuardrail> inputGuardrails,
                                 List<OutputGuardrail> outputGuardrails,
                                 int maxRetries,
                                 String fallbackResponse,
                                 GuardrailScorecard scorecard) {
        this.model = model;
        this.inputGuardrails = inputGuardrails;
        this.outputGuardrails = outputGuardrails;
        this.maxRetries = maxRetries;
        this.fallbackResponse = fallbackResponse;
        this.scorecard = scorecard;
    }

    public String process(UserMessage userMessage) {
        for (InputGuardrail inputGuardrail : inputGuardrails) {
            InputGuardrailResult inputResult = inputGuardrail.validate(userMessage);
            if (inputResult.isFatal()) {
                scorecard.recordBlocked(inputGuardrail.getClass().getSimpleName(), inputResult.reason());
                return fallbackResponse;
            }
        }

        String currentPromptText = userMessage.singleText();
        int attempt = 0;

        while (attempt <= maxRetries) {
            AiMessage response = model.generate(UserMessage.from(currentPromptText)).content();

            boolean allPassed = true;
            for (OutputGuardrail outputGuardrail : outputGuardrails) {
                OutputGuardrailResult result = outputGuardrail.validate(response);

                if (result.isFatal()) {
                    scorecard.recordBlocked(outputGuardrail.getClass().getSimpleName(), result.reason());
                    return fallbackResponse;
                }

                if (result.isReprompt()) {
                    scorecard.recordRetry(outputGuardrail.getClass().getSimpleName(), result.reason());
                    currentPromptText = currentPromptText + "\n\n[Correction requested]: "
                        + result.correctiveInstruction();
                    allPassed = false;
                    break;
                }

                if (result.hasRewrittenContent()) {
                    response = AiMessage.from(result.rewrittenContent());
                }
            }

            if (allPassed) {
                scorecard.recordSuccess();
                return response.text();
            }

            attempt++;
        }

        scorecard.recordFallback("Max retries exceeded");
        return fallbackResponse;
    }
}
