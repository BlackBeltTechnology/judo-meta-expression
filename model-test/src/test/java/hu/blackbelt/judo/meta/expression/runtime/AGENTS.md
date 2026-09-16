# AGENTS.md — `model-test/src/test/java/hu/blackbelt/judo/meta/expression/runtime`

Runtime expression test fixtures and validator unit tests.

| File | Purpose |
| --- | --- |
| `ExpressionModelForTest.java` | Builds comprehensive test EMF expression model populating collection filters, navigations, string, logical, numeric, and temporal expressions. Exports `ExpressionModelForTest`, `createExpressionModel()`. Requires valid EMF resource context. |
| `ExpressionValidatorTest.java` | Unit tests for `ExpressionValidator` formatting and validation log behavior. Verifies error and warning report strings via `ExpressionValidator.format()` and mock logger interactions across empty and populated diagnostics. |
| `MinimalExpressionFactory.java` | Factory generating minimal test expression elements across constant types and measured decimals. Package-private abstract class exporting `createMinimalExpression()`. Returns immutable list of `EObject` instances with demo measure names. |
