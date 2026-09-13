# `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/rules/custom` — Zeta rules for custom-typed expressions

`custom.evl` counterpart. `resolved()` is unconditional — it marks `RESOLVED` and
always passes, so its `"Unsupported expression"` message is unreachable and a passing
`RESOLVED` is not evidence the expression resolved. The other two rules are guarded on
that recorded fact or on a non-null default case.

| File | Purpose |
|---|---|
| `CustomExpressionValidations.java` | `@ValidationContext(CustomExpression.class)`. Three rules: `resolved()` publishes `RESOLVED`; `attributeTypeIsCustom()` (guard `isCustomAttributeAndResolved` = `instanceof CustomAttribute` plus `satisfies(RESOLVED)`) requires `modelAdapter.isCustom` on the unwrapped `getAttributeType` `Optional`; `typeOfDefaultCaseIsCustom()` fires only when `CustomSwitchExpression.getDefaultExpression()` is non-null. |
