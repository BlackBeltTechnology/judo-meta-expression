# `ConstantExpressionValidations.java`

Zeta counterpart of `constant.evl`. Asserts that leaf expressions — literal constants,
environment variables, and `Instance` — actually carry the value they stand for, and
publishes the `RESOLVED` fact that other rule classes guard on.

## Registered against `Expression`, not a constant type

`@ValidationContext(Expression.class)` — the broadest possible context. Every rule is
therefore `@Guard`-ed on a bare `element instanceof <Subtype>` predicate
(`isIntegerConstant`, `isLiteral`, `isCustomData`, `isInstance`, …) and the class is
invoked for *every* expression in the model. Narrowing the `@ValidationContext` to a
constant supertype would drop the environment-variable and `Instance` rules.

## Two checked families plus one catch-all

| Family | Guards | Checked field |
|---|---|---|
| Constants | `isIntegerConstant`, `isDecimalConstant`, `isBooleanConstant`, `isStringConstant`, `isLiteral`, `isCustomData` | `self.getValue() != null` |
| Environment variables | `isIntegerEnvironmentVariable`, `isDecimalEnvironmentVariable`, `isBooleanEnvironmentVariable`, `isStringEnvironmentVariable`, `isLiteralEnvironmentVariable`, `isCustomEnvironmentVariable` | `self.getVariableName() != null` |
| `Instance` | `isInstance` | nothing — see below |

Constraint names are string literals (`IntegerConstantResolved`,
`StringEnvironmentVariableResolved`, `InstanceResolved`, …) rather than
`ValidationConstants` fields; only the recorded fact uses
`ValidationConstants.RESOLVED`.

- Environment-variable rules check the **variable name**, not a value. A declared but
  unset variable passes here; absence is a name problem, not a value problem.
- `instanceResolved()` is unconditional: it marks `RESOLVED` and returns
  `ValidationResult.pass()` without touching the element. Its `"Unsupported
  expression"` message is unreachable, so a passing `InstanceResolved` is not
  evidence the instance resolved.

## Contracts a caller can violate

- All thirteen rules record `markSatisfied(element, RESOLVED)` under the *same* key,
  so `RESOLVED` on a leaf means "some constant rule passed", never which one.
- A failing rule does **not** mark `RESOLVED`, so a downstream guard reading
  `satisfies(element, RESOLVED)` correctly skips valueless constants.
- Each lambda casts `element` to its concrete subtype without re-checking; invoking a
  rule outside its `@Guard` throws `ClassCastException`.
- `ctx` is cast to `ExpressionValidationContext` unguarded.
- Failure messages append `self` (the `EObject`), so the emitted text carries the EMF
  `toString`, not a model-level identifier.
