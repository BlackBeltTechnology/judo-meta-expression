# `AbstractExpressionValidationTest.java`

Shared fixture letting one parameterized test case run on both validation engines.
Exposes `expressionModel`, `modelAdapter`, `adaptedResource`, `measureResource`, `adaptedName = "ADAPTED"`, `measureName = "MEASURES"`, `validatorType`; plus `initModel()`, `setModelAdapter`, `setAdaptedResource`, `setMeasureResource`, `runValidation(expectedErrors, expectedWarnings)`.
`runValidation` asserts `expressionModel.isValid()` and a non-null `modelAdapter`, then dispatches: `EVL` re-runs Epsilon with empty expectations and compares extracted constraint names, `JAVA` calls `ExpressionZetaValidator.validateExpression(..., parallel=false)` for deterministic order.
Subclasses must set `modelAdapter` before validating; an unset `validatorType` case throws `IllegalStateException`.