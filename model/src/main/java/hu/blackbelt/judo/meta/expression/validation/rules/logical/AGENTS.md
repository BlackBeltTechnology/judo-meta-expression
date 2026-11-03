# AGENTS.md — `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/rules/logical`

Validation rules and critiques for logical expression elements including type compatibility, quantified expressions, and comparison critiques.

| File | Purpose |
| --- | --- |
| `LogicalExpressionValidations.java` | Zeta validation rules for `LogicalExpression` AST nodes. Exports `resolved()`, `attributeTypeIsBoolean()`, `instanceOfElementTypeIsCompatible()`, `typeOfElementTypeIsCompatible()`, `containsTypesAreCompatible()`, `memberOfTypesAreCompatible()`, `integerComparisonIsRecommended()`, `existsResolved()`, `forAllResolved()`, `emptyResolved()`. Requires `ExpressionValidationContext` with `ModelAdapter`. |
