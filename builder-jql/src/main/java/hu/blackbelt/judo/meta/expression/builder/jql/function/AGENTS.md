# AGENTS.md — `builder-jql/src/main/java/hu/blackbelt/judo/meta/expression/builder/jql/function`

Base contracts and parameterized transformers for JQL AST function invocations.

| File | Purpose |
| --- | --- |
| `AbstractJqlFunctionTransformer.java` | Skeletal base for JQL function transformers wrapping an `ExpressionTransformer` delegate instance. |
| `JqlFunctionTransformer.java` | Functional interface transforming an AST `JqlFunction` applied on argument `B` into an `Expression` via `apply()`. |
| `JqlParameterizedFunctionTransformer.java` | Transforms single-parameter JQL functions into target `RESULT` expressions via `apply()`. Exactly one parameter required; multiple or zero parameters throw `IllegalArgumentException`. |
