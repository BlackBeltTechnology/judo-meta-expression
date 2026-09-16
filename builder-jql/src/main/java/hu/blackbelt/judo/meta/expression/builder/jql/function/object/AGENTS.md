# AGENTS.md — `builder-jql/src/main/java/hu/blackbelt/judo/meta/expression/builder/jql/function/object`

Object nullability and definition function transformers.

| File | Purpose |
| --- | --- |
| `JqlIsDefinedFunctionTransformer.java` | Transforms `isDefined` and `isUndefined` checks into `UndefinedComparison` expressions, negating via `NegationExpression` when testing for defined presence. |
