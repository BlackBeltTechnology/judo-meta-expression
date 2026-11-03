# AGENTS.md — `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/rules/enumeration`

Validation rules for enumeration expression elements checking resolution, enumeration attribute types, and switch default cases.

| File | Purpose |
| --- | --- |
| `EnumerationExpressionValidations.java` | Zeta validation rules for `EnumerationExpression` AST nodes. Exports `resolved()`, `attributeTypeIsEnumeration()`, `typeOfDefaultCaseIsEnumeration()`, plus guards `isEnumerationAttributeAndResolved` and `isEnumerationSwitchWithDefault`. Requires `ValidationContext` to be an `ExpressionValidationContext` providing `ModelAdapter`. |
