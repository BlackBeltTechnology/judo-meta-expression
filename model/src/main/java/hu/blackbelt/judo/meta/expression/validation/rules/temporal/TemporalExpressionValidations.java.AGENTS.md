# `TemporalExpressionValidations.java`

Zeta rule set for date/timestamp/time expressions; replaces the temporal `.evl` rules.
`@ValidationContext(Expression.class)`; every `@Constraint ValidationRule` factory (`dateAttributeResolved`, `attributeTypeIsTimestamp`, `timestampAdditionDurationIsSystemUnit`, `typeOfDefaultCaseIsTime`, `timeEnvironmentVariableResolved`, `timestampAggregatedExpressionResolved`) paired with a `@Guard` predicate (`isDateAttribute`, `isTimestampSwitchWithDefault`, …) that type-filters the element.
Rules cast `ValidationContext` to `ExpressionValidationContext` for `getModelAdapter()`, call `markSatisfied(element, ValidationConstants.RESOLVED)` before `ValidationResult.pass()`; dropping the `@Guard` lets a rule cast a foreign element type.