package com.example.guardrails.adapter;

import com.example.guardrails.async.AsyncGuardrail;
import com.example.guardrails.async.GuardrailContext;
import com.example.guardrails.async.GuardrailDecision;
import com.example.guardrails.sync.OutputGuardrail;
import com.example.guardrails.sync.OutputGuardrailResult;
import dev.langchain4j.data.message.AiMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

public final class OutputGuardrailAsyncAdapter implements AsyncGuardrail {

    private final OutputGuardrail delegate;
    private final ExecutorService executor;

    public OutputGuardrailAsyncAdapter(OutputGuardrail delegate, ExecutorService executor) {
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
            OutputGuardrailResult result = delegate.validate(AiMessage.from(context.modelOutput()));

            if (result.isFatal()) {
                return GuardrailDecision.block(name(), result.reason());
            }
            if (result.isReprompt()) {
                return GuardrailDecision.reprompt(name(), result.reason(), result.correctiveInstruction());
            }
            if (result.hasRewrittenContent()) {
                return GuardrailDecision.allowWithRewrite(name(), result.rewrittenContent(), result.reason());
            }
            return GuardrailDecision.allow(name());
        }, executor);
    }
}
