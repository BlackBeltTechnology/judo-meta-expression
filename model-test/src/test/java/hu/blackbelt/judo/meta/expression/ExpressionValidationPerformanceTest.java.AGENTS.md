# `ExpressionValidationPerformanceTest.java`

Benchmarks EVL against Zeta on a generated model.
`@BeforeEach` builds `expressionModel` `urn:expression.performance-test` and a Mockito `ModelAdapter` whose `get(TypeName)` always resolves.
Single `@Test benchmarkEvlVsJavaValidation` generates `EXPRESSION_COUNT = 2500` expressions, warms up `WARMUP_ITERATIONS = 1`, then times `BENCHMARK_ITERATIONS = 3` runs of EVL, sequential Zeta and parallel Zeta and logs averages.
Measures wall-clock only — reports timings, never fails on a regression.