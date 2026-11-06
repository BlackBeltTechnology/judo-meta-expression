# `CustomExpressionValidations.java`

`@ValidationContext(CustomExpression.class)`. Three rules.
`resolved()` publishes `RESOLVED`.
`attributeTypeIsCustom()` (guard `isCustomAttributeAndResolved` = `instanceof CustomAttribute` plus `satisfies(RESOLVED)`) requires `modelAdapter.isCustom` on the unwrapped `getAttributeType` `Optional`.
`typeOfDefaultCaseIsCustom()` fires only when `CustomSwitchExpression.getDefaultExpression()` is non-null.