package com.example.guardrails.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class AsyncGuardrailRunner {

    public CompletionStage<List<GuardrailDecision>> runAll(
            List<AsyncGuardrail> guardrails,
            GuardrailContext context
    ) {
        List<CompletableFuture<GuardrailDecision>> futures = guardrails.stream()
                .map(g -> g.check(context).toCompletableFuture())
                .toList();

        CompletableFuture<Void> allDone =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));

        return allDone.thenApply(ignored ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .toList()
        );
    }
}
