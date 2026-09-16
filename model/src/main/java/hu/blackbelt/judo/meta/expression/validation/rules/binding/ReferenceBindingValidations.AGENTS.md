# `ReferenceBindingValidations.java`

`@ValidationContext(ReferenceBinding.class)`. Asserts that the expression assigned to
a transfer-object relation resolves to the relation's target entity type and matches
its cardinality.

## Rules

| Rule | Guard | Constraint |
|---|---|---|
| `referenceBindingExpressionIsValid()` | none | `REFERENCE_BINDING_EXPRESSION_IS_VALID` |
| `referenceExpressionMatchesBinding()` | `hasValidReferenceExpression` | `REFERENCE_EXPRESSION_MATCHES_BINDING` |
| `objectExpressionMatchesBinding()` | `hasObjectExpression` | `OBJECT_EXPRESSION_MATCHES_BINDING` |
| `collectionExpressionMatchesBinding()` | `hasCollectionExpression` | `COLLECTION_EXPRESSION_MATCHES_BINDING` |

- `referenceBindingExpressionIsValid()` requires `self.getExpression() instanceof
  ReferenceExpression` and, on pass, calls `exprCtx.markSatisfied(element,
  REFERENCE_BINDING_EXPRESSION_IS_VALID)`. This is the only recorded fact in the file.
  `hasValidReferenceExpression` is the only guard that reads it back, via
  `exprCtx.satisfies(...)`; the cardinality guards test `instanceof` only.
- `referenceExpressionMatchesBinding()` resolves
  `modelAdapter.getEntityTypeOfTransferObjectRelationTarget(self.getTypeName(),
  self.getReferenceName())`. An absent entity type fails immediately. It then accepts
  `refExpr.getObjectType(modelAdapter)` when it equals the entity type, or when
  `modelAdapter.getSuperTypes(...)` contains it — so subtype assignment is legal.
- `objectExpressionMatchesBinding()` and `collectionExpressionMatchesBinding()` are
  mirror checks over `modelAdapter.isCollectionReference(self.getTypeName(),
  self.getReferenceName())`: an `ObjectExpression` bound to a collection reference
  fails, and a `CollectionExpression` bound to a non-collection reference fails.
  Neither consults the expression's own type.

## Contracts a caller can violate

- A null `refExpr.getObjectType(modelAdapter)` returns `ValidationResult.pass()`, so
  an unresolvable expression type is **not** reported by this file. Resolution must be
  validated separately or the mismatch goes silent.
- Guards re-test `element instanceof ReferenceBinding` and return `false` otherwise;
  a mis-registered `@ValidationContext` disables the rules silently instead of
  throwing.
- Rules cast `ctx` to `ExpressionValidationContext` unguarded — a plain
  `ValidationContext` throws `ClassCastException`.
- `referenceExpressionMatchesBinding()` runs only after
  `referenceBindingExpressionIsValid()` recorded its fact, so a validator that calls
  `clearSatisfiesCache` mid-pass suppresses that rule. The two cardinality rules are
  unaffected.

## Note on the supertype check

After the `superTypes.contains(entityType)` test the method loops the same collection
comparing each element again — the loop is redundant with the preceding `contains`
call and changes no outcome.
