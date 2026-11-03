# AGENTS.md — `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/rules/expression`

Validation rules for generic expressions and type names validating scope variable references and namespace resolution.

| File | Purpose |
| --- | --- |
| `ExpressionValidations.java` | Zeta validation rules for `Expression` AST nodes. Exports `lambdaVariableIsValid()` constraint and `isLambdaFunction` guard; checks that all `VariableReference` terms inside a lambda expression belong to `evaluator.getVariablesOfScope(self)`. Requires `ExpressionValidationContext` with `ExpressionEvaluator`. |
| `TypeNameValidations.java` | Zeta validation rules for `TypeName` AST nodes. Exports `objectTypeIsValid()` constraint; verifies `self.get(modelAdapter)` resolves the type name within `self.getNamespace()`. Requires `ExpressionValidationContext` with `ModelAdapter`. |
