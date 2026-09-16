# `CollectionExpressionValidations.java`

`@ValidationContext(CollectionExpression.class)`. Zeta counterpart of `collection.evl`.
Resolves each collection-valued expression subtype and checks the cardinality of what
its navigation lands on.

## The `resolved()` catch-all — read this first

`resolved()` (`@Constraint(RESOLVED, "Unsupported expression")`) is **unguarded and
unconditional**: it calls `exprCtx.markSatisfied(element, RESOLVED)` and returns
`ValidationResult.pass()` without inspecting the element at all. It exists so that
every `CollectionExpression` carries the `RESOLVED` fact that other rule classes guard
on. It can never fail, so its `"Unsupported expression"` message is unreachable — do
not read a passing `RESOLVED` on a collection expression as evidence the expression
actually resolved.

## Rules

Thirteen further rules, each `@Guard`-ed on a subtype predicate so only the rules
matching the element's concrete class run. Per-subtype constraint names are string
literals here, not `ValidationConstants` fields (only `RESOLVED` and
`CAST_TYPE_IS_COMPATIBLE` come from the constants class).

| Subtype guard | Constraints |
|---|---|
| `isImmutableCollection` | `ImmutableCollectionResolved` |
| `isCollectionNavigationFromObject` (+`…AndResolved`) | `CollectionNavigationFromObjectResolved`, `CollectionNavigationFromObjectTargetIsCollection` |
| `isCollectionNavigationFromCollection` (+`…AndResolved`) | `CollectionNavigationFromCollectionResolved`, `CollectionNavigationFromCollectionTargetIsCollection` |
| `isObjectNavigationFromCollection` (+`…AndResolved`) | `ObjectNavigationFromCollectionResolved`, `ObjectNavigationFromCollectionTargetIsNotCollection` |
| `isCollectionFilterExpression` | `CollectionFilterResolved` |
| `isSortExpression` | `SortExpressionResolved` |
| `isCastCollection` (+`…AndResolved`) | `CastCollectionResolved`, `CAST_TYPE_IS_COMPATIBLE` |
| `isCollectionVariableReference` (+`…AndResolved`) | `CollectionVariableReferenceResolved`, `CollectionVariableReferenceTypeIsDefined` |

The paired `…AndResolved` guards implement EVL's two-phase ordering: the `Resolved`
rule marks `RESOLVED`, and the follow-up target-cardinality rule only runs once that
fact is recorded. Three navigation families therefore report a *missing reference*
before ever reporting a *wrong cardinality*.

## Contracts a caller can violate

- `castTypeIsCompatible()` passes when either `self.getObjectType(modelAdapter)` or
  `self.getCollectionExpression().getObjectType(modelAdapter)` is null, so an
  unresolved cast is silently accepted. It requires
  `modelAdapter.getSuperTypes(castType)` to *contain the source collection type* —
  i.e. only upcasts along a declared supertype chain pass.
- Rules cast `element` to their subtype (`CastCollection`, etc.) inside the lambda
  without re-checking; bypassing the `@Guard` throws `ClassCastException`.
- `ctx` is cast to `ExpressionValidationContext` unguarded.
- A `CollectionExpression` subtype covered by no guard is validated by `resolved()`
  only — which always passes. New subtypes need a guard or they go unchecked.
