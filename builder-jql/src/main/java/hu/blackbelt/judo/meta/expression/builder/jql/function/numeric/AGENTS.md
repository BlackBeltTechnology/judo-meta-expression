# AGENTS.md — `builder-jql/src/main/java/hu/blackbelt/judo/meta/expression/builder/jql/function/numeric`

Numeric function transformers mapping JQL math operations.

| File | Purpose |
| --- | --- |
| `JqlRoundFunctionTransformer.java` | Transforms round functions on numeric expressions into `DecimalRoundExpression` (when given integer scale parameter) or `IntegerRoundExpression` (without scale). Target must be `NumericExpression`; parameter count must be 0 or 1. |
