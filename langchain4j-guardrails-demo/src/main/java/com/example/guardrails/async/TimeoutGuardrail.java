package com.example.guardrails.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public final class TimeoutGuardrail implements AsyncGuardrail {

    private final AsyncGuardrail delegate;
    private final long timeoutMillis;
    private final boolean failClosed;

    public TimeoutGuardrail(
            AsyncGuardrail delegate,
            long timeoutMillis,
            boolean failClosed
    ) {
        this.delegate = delegate;
        this.timeoutMillis = timeoutMillis;
        this.failClosed = failClosed;
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public CompletionStage<GuardrailDecision> check(GuardrailContext context) {
        CompletableFuture<GuardrailDecision> future =
                delegate.check(context).toCompletableFuture();

        return future
                .orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .exceptionally(error -> {
                    if (failClosed) {
                        return GuardrailDecision.block(
                                delegate.name(),
                                "Guardrail failed or timed out; failing closed."
                        );
                    }

                    return GuardrailDecision.warn(
                            delegate.name(),
                            "Guardrail failed or timed out; failing open."
                    );
                });
    }
}
