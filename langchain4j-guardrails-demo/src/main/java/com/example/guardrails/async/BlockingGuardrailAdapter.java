package com.example.guardrails.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public final class BlockingGuardrailAdapter implements AsyncGuardrail {

    private final String name;
    private final ExecutorService executor;
    private final Function<GuardrailContext, GuardrailDecision> blockingCheckFunction;

    public BlockingGuardrailAdapter(String name,
                                  ExecutorService executor,
                                  Function<GuardrailContext, GuardrailDecision> blockingCheckFunction) {
        this.name = name;
        this.executor = executor;
        this.blockingCheckFunction = blockingCheckFunction;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CompletionStage<GuardrailDecision> check(GuardrailContext context) {
        return CompletableFuture.supplyAsync(() -> blockingCheckFunction.apply(context), executor);
    }
}
