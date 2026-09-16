# `AttributeValidations.java`

`@ValidationContext(AttributeSelector.class)`. Sole rule `resolved()` unwraps the `Optional` returned by `self.getAttributeType(modelAdapter)` and passes when an attribute type is present, then records `markSatisfied(element, RESOLVED)` for downstream guards.
Drops EVL's precondition that the object expression be `Resolved` first, because Zeta runs constraints in one unordered pass.
Fails with "Attribute not found".