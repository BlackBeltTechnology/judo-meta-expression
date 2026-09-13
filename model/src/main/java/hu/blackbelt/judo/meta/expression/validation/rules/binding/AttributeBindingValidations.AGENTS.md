# `AttributeBindingValidations.java`

`@ValidationContext(AttributeBinding.class)`. Asserts that the primitive type of the
bound attribute agrees with the kind of expression assigned to it.

## Shape

Eight `@Constraint` rules, each paired with a `@Guard(method = ...)` predicate that
dispatches on the runtime class of `self.getExpression()`. Only the rule whose guard
matches the assigned expression runs; the other seven are never evaluated for that
element.

| Guard | Expression kind | Constraint | `ModelAdapter` predicate |
|---|---|---|---|
| `hasNumericExpression` | `NumericExpression` | `NUMERIC_EXPRESSION_MATCHES_BINDING` | `isNumeric` |
| `hasLogicalExpression` | `LogicalExpression` | `BOOLEAN_EXPRESSION_MATCHES_BINDING` | `isBoolean` |
| `hasStringExpression` | `StringExpression` | `STRING_EXPRESSION_MATCHES_BINDING` | `isString` |
| `hasEnumerationExpression` | `EnumerationExpression` | `ENUMERATION_EXPRESSION_MATCHES_BINDING` | `isEnumeration` |
| `hasDateExpression` | `DateExpression` | `DATE_EXPRESSION_MATCHES_BINDING` | `isDate` |
| `hasTimestampExpression` | `TimestampExpression` | `TIMESTAMP_EXPRESSION_MATCHES_BINDING` | `isTimestamp` |
| `hasTimeExpression` | `TimeExpression` | `TIME_EXPRESSION_MATCHES_BINDING` | `isTime` |
| `hasCustomExpression` | `CustomExpression` | `CUSTOM_EXPRESSION_MATCHES_BINDING` | `isCustom` |

## Contracts a caller can violate

- Every guard re-tests `element instanceof AttributeBinding` and returns `false`
  otherwise, so registering the class against a different `@ValidationContext`
  silently disables all eight rules rather than throwing.
- Each rule resolves `modelAdapter.get(self.getTypeName())` first and returns
  `ValidationResult.pass()` when the type is absent. An unresolved transfer object
  type therefore reports **no** binding error — resolution must be validated
  elsewhere or type errors go unreported.
- `modelAdapter.getAttributeType(type, self.getAttributeName())` must likewise be
  present; an absent attribute falls through to the failure branch.
- Rules cast `ctx` to `ExpressionValidationContext` unguarded, so running them under
  a plain `ValidationContext` throws `ClassCastException`.
- No rule calls `markSatisfied`, so no other constraint can guard on these results.
- An expression kind with no matching guard (e.g. a collection-valued expression)
  passes unvalidated — the guard set is not exhaustive over `Expression`.

## Failure messages

Each failure names the offending attribute and the reason, e.g. `"Attribute named X
must be numeric type, because the assigned expression evaluates to a number."` The
short `@Constraint(message = ...)` text ("Attribute must be numeric type") is the
catalogue label, not the emitted message.
