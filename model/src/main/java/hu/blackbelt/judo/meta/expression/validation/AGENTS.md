# `model/src/main/java/hu/blackbelt/judo/meta/expression/validation` — Zeta (native Java) validation entry point for the expression model

| File | Purpose |
|---|---|
| `ExpressionValidationContext.java` | `ValidationContext` subclass carrying adapter, evaluator and inter-constraint `satisfiedConstraints` state to Zeta rules → see `ExpressionValidationContext.java.AGENTS.md` |
| `ExpressionZetaValidator.java` | Java EVL alternative — runs the 16 `VALIDATOR_CLASSES` rules, exports static `validateExpression` 3/5/6-arg forms → see `ExpressionZetaValidator.java.AGENTS.md` |
