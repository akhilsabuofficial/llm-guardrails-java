package com.example.guardrails.adapter;

import com.example.guardrails.async.AsyncGuardrail;
import com.example.guardrails.async.GuardrailContext;
import com.example.guardrails.async.GuardrailDecision;
import com.example.guardrails.sync.InputGuardrail;
import com.example.guardrails.sync.InputGuardrailResult;
import dev.langchain4j.data.message.UserMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

public final class InputGuardrailAsyncAdapter implements AsyncGuardrail {

    private final InputGuardrail delegate;
    private final ExecutorService executor;

    public InputGuardrailAsyncAdapter(InputGuardrail delegate, ExecutorService executor) {
        this.delegate = delegate;
        this.executor = executor;
    }

    @Override
    public String name() {
        return delegate.getClass().getSimpleName();
    }

    @Override
    public CompletionStage<GuardrailDecision> check(GuardrailContext context) {
        return CompletableFuture.supplyAsync(() -> {
            InputGuardrailResult result = delegate.validate(UserMessage.from(context.input()));

            if (result.isFatal()) {
                return GuardrailDecision.block(name(), result.reason());
            }
            return GuardrailDecision.allow(name());
        }, executor);
    }
}
