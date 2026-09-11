# `model/src/main/java/hu/blackbelt/judo/meta/expression/validation` — Zeta (native Java) validation entry point for the expression model

| File | Purpose |
|---|---|
| `ExpressionValidationContext.java` | Carries adapter, evaluator and inter-constraint state to every Zeta rule. Extends `ValidationContext` with `getExpressionModel`, `getModelAdapter`, `getEvaluator`, `satisfies(element, constraintName)`, `satisfiesAll`, `markSatisfied`, `clearSatisfiesCache`. The `satisfiedConstraints` `ConcurrentHashMap` is empty until a rule calls `markSatisfied`, so a guard on an unrecorded constraint reads false. |
| `ExpressionZetaValidator.java` | Java alternative to the EVL run: registers the 16 rule classes listed in `VALIDATOR_CLASSES`, builds `ExpressionValidationContext`, and runs `ValidationExecutor` over every `EObject` of the model resource. Exports static `validateExpression` in 3-, 5- and 6-arg (with `parallel`) forms. Results are compared by constraint name; a null `expectedWarnings` means warnings are ignored, mismatch throws `ExpressionValidationException`. |
