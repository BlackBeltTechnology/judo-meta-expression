# `model-test/src/test/java/hu/blackbelt/judo/meta/expression` — dual-engine (EVL vs Zeta) validation test harness

| File | Purpose |
|---|---|
| `AbstractExpressionValidationTest.java` | Dual-engine (EVL/Zeta) fixture — `runValidation`, `validatorType`, `ExpressionZetaValidator.validateExpression` → see `AbstractExpressionValidationTest.java.AGENTS.md` |
| `ExpressionValidationPerformanceTest.java` | EVL-vs-Zeta benchmark — `benchmarkEvlVsJavaValidation`, `EXPRESSION_COUNT`, `WARMUP_ITERATIONS`, `BENCHMARK_ITERATIONS` → see `ExpressionValidationPerformanceTest.java.AGENTS.md` |
| `ValidatorType.java` | Engine selector feeding the parameterized dual-engine tests. `enum` with `EVL` (Epsilon, `ExpressionEpsilonValidator`) and `JAVA` (`ExpressionZetaValidator`); `AbstractExpressionValidationTest.runValidation` switches on it, so a new constant needs a new branch there. |
