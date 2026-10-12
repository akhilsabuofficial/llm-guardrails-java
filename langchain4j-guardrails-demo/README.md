# LangChain4j Guardrails Demo

A complete, production-ready reference architecture for building, composing, and orchestrating custom input and output guardrails for LLM applications in Java (Java 21+ and LangChain4j).

## Features

- **4 Core Custom Guardrails**:
  - `PromptInjectionGuardrail` (`InputGuardrail`): Regex pattern scanning to catch prompt overrides.
  - `PiiRedactorGuardrail` (`OutputGuardrail`): Masking of emails, phone numbers, SSNs, and credit cards.
  - `JsonSchemaGuardrail` (`OutputGuardrail`): Structural field and type validator with reprompt corrective feedback.
  - `OffTopicFilterGuardrail` (`OutputGuardrail`): Domain scope filter returning corrective guidance.
- **Concurrent & Non-Blocking Evaluation**:
  - `AsyncGuardrailRunner` using `CompletableFuture.allOf` to run independent guardrails in parallel.
  - `TimeoutGuardrail` with explicit **Fail-Open** vs. **Fail-Closed** policy handling.
  - `BlockingGuardrailAdapter` for wrapping legacy blocking calls in Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`).
- **Unified Pipeline Architecture**:
  - `InputGuardrailAsyncAdapter` & `OutputGuardrailAsyncAdapter` connecting synchronous LangChain4j guardrails into concurrent pipelines.
  - `UnifiedGuardrailPipeline` handling input checks, LLM invocation, concurrent output validation, corrective reprompt retries, safe fallbacks, and real-time scorecard metrics.
- **Audit & Metrics**:
  - `GuardrailScorecard`: Thread-safe counters and structured event log for operational visibility.

---

## Project Structure

```
langchain4j-guardrails-demo/
├── pom.xml
├── README.md
└── src/
    ├── main/java/com/example/guardrails/
    │   ├── DemoApp.java
    │   ├── sync/
    │   │   ├── InputGuardrail.java
    │   │   ├── InputGuardrailResult.java
    │   │   ├── OutputGuardrail.java
    │   │   ├── OutputGuardrailResult.java
    │   │   ├── PromptInjectionGuardrail.java
    │   │   ├── PiiRedactorGuardrail.java
    │   │   ├── JsonSchemaGuardrail.java
    │   │   ├── OffTopicFilterGuardrail.java
    │   │   └── GuardrailOrchestrator.java
    │   ├── async/
    │   │   ├── GuardrailSeverity.java
    │   │   ├── GuardrailDecision.java
    │   │   ├── GuardrailContext.java
    │   │   ├── AsyncGuardrail.java
    │   │   ├── AsyncGuardrailRunner.java
    │   │   ├── StageOutcome.java
    │   │   ├── OrchestratedDecision.java
    │   │   ├── AsyncGuardrailOrchestrator.java
    │   │   ├── TimeoutGuardrail.java
    │   │   └── BlockingGuardrailAdapter.java
    │   ├── adapter/
    │   │   ├── InputGuardrailAsyncAdapter.java
    │   │   └── OutputGuardrailAsyncAdapter.java
    │   ├── scorecard/
    │   │   └── GuardrailScorecard.java
    │   └── pipeline/
    │       └── UnifiedGuardrailPipeline.java
    └── test/java/com/example/guardrails/
        ├── sync/
        ├── async/
        └── pipeline/
```

---

## Prerequisites

- **Java 21** or later.
- **Maven 3.8+**.

---

## Building and Running Tests

To run the complete automated test suite:

```bash
mvn clean test
```

To run the interactive standalone demo application:

```bash
mvn compile exec:java -Dexec.mainClass="com.example.guardrails.DemoApp"
```
