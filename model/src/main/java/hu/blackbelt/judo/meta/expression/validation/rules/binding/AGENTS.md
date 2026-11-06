# `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/rules/binding` — Zeta rules asserting a binding's expression matches its target feature

| File | Purpose |
|---|---|
| `AttributeBindingValidations.java` | Eight guarded rules pairing an `AttributeBinding` expression kind with the bound attribute's primitive type. → see `AttributeBindingValidations.AGENTS.md` |
| `FilterBindingValidations.java` | `@ValidationContext(FilterBinding.class)`. Single unguarded rule `logicalExpressionMatchesBinding()` passes only when `self.getExpression() instanceof LogicalExpression`; every other expression kind fails naming `self.getTypeName()`. Consults no `ModelAdapter` and records no `markSatisfied`, so it is the one binding rule that cannot be skipped by an unresolved type. |
| `ReferenceBindingValidations.java` | Four rules checking a `ReferenceBinding` expression against its relation's target entity type and `isCollectionReference` cardinality. → see `ReferenceBindingValidations.AGENTS.md` |
