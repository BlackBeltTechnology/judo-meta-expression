# AGENTS.md — `model/src/main/epsilon/validations`

Root Epsilon Validation Language (EVL) scripts initializing expression model validation runs.

| File | Purpose |
| --- | --- |
| `expression-plugin-validation.evl` | Root EVL script importing `expression.evl` and instantiating `hu.blackbelt.judo.meta.expression.runtime.ExpressionUtils` in the `pre` block for plugin-mode validation. |
| `expression.evl` | Master validation suite entry point importing sub-validations; exports `isDimensionDefined()` on `NumericExpression` and controls evaluator lifecycle via `evaluator.init()` and `evaluator.cleanup()`. |
