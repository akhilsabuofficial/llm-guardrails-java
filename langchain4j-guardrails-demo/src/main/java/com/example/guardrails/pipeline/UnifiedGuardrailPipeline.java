package com.example.guardrails.pipeline;

import com.example.guardrails.async.*;
import com.example.guardrails.scorecard.GuardrailScorecard;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public final class UnifiedGuardrailPipeline {

    private final ChatLanguageModel model;
    private final AsyncGuardrailOrchestrator orchestrator;
    private final List<AsyncGuardrail> inputStage;
    private final List<AsyncGuardrail> outputStage;
    private final int maxRetries;
    private final String fallbackResponse;
    private final GuardrailScorecard scorecard;

    public UnifiedGuardrailPipeline(ChatLanguageModel model,
                                    AsyncGuardrailOrchestrator orchestrator,
                                    List<AsyncGuardrail> inputStage,
                                    List<AsyncGuardrail> outputStage,
                                    int maxRetries,
                                    String fallbackResponse,
                                    GuardrailScorecard scorecard) {
        this.model = model;
        this.orchestrator = orchestrator;
        this.inputStage = inputStage;
        this.outputStage = outputStage;
        this.maxRetries = maxRetries;
        this.fallbackResponse = fallbackResponse;
        this.scorecard = scorecard;
    }

    public CompletionStage<String> process(String userId, String userInput) {
        GuardrailContext inputContext = new GuardrailContext(userId, userInput, null);

        return orchestrator.evaluateInput(inputStage, inputContext)
                .thenCompose(inputDecision -> {
                    if (inputDecision.outcome() != StageOutcome.ALLOWED) {
                        recordBlocked(inputDecision);
                        return CompletableFuture.completedFuture(fallbackResponse);
                    }
                    return attemptOutputStage(userId, userInput, userInput, 0);
                });
    }

    private CompletionStage<String> attemptOutputStage(String userId,
                                                       String originalInput,
                                                       String currentPromptText,
                                                       int attempt) {
        return callLlmAsync(currentPromptText).thenCompose(modelOutput -> {
            GuardrailContext outputContext = new GuardrailContext(userId, originalInput, modelOutput);

            return orchestrator.evaluateInput(outputStage, outputContext)
                    .thenCompose(outputDecision -> handleOutputDecision(
                            userId, originalInput, currentPromptText, attempt, modelOutput, outputDecision));
        });
    }

    private CompletionStage<String> handleOutputDecision(String userId,
                                                         String originalInput,
                                                         String currentPromptText,
                                                         int attempt,
                                                         String modelOutput,
                                                         OrchestratedDecision outputDecision) {
        switch (outputDecision.outcome()) {
            case BLOCKED -> {
                recordBlocked(outputDecision);
                return CompletableFuture.completedFuture(fallbackResponse);
            }
            case REPROMPT -> {
                if (attempt >= maxRetries) {
                    scorecard.recordFallback("Max retries exceeded");
                    return CompletableFuture.completedFuture(fallbackResponse);
                }
                recordRetries(outputDecision);
                String correction = outputDecision.decisions().stream()
                        .filter(d -> d.severity() == GuardrailSeverity.REPROMPT)
                        .map(GuardrailDecision::correctiveInstruction)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("\n"));
                String nextPrompt = currentPromptText + "\n\n[Correction requested]: " + correction;
                return attemptOutputStage(userId, originalInput, nextPrompt, attempt + 1);
            }
            default -> {
                String finalText = outputDecision.decisions().stream()
                        .map(GuardrailDecision::rewrittenContent)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(modelOutput);
                scorecard.recordSuccess();
                return CompletableFuture.completedFuture(finalText);
            }
        }
    }

    private CompletionStage<String> callLlmAsync(String prompt) {
        return CompletableFuture.supplyAsync(() ->
                model.generate(UserMessage.from(prompt)).content().text());
    }

    private void recordBlocked(OrchestratedDecision decision) {
        decision.decisions().stream()
                .filter(d -> d.severity() == GuardrailSeverity.BLOCK)
                .findFirst()
                .ifPresent(d -> scorecard.recordBlocked(d.guardrailName(), d.reason()));
    }

    private void recordRetries(OrchestratedDecision decision) {
        decision.decisions().stream()
                .filter(d -> d.severity() == GuardrailSeverity.REPROMPT)
                .forEach(d -> scorecard.recordRetry(d.guardrailName(), d.reason()));
    }
}
