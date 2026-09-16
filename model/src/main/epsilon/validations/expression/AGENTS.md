# AGENTS.md — `model/src/main/epsilon/validations/expression`

Epsilon Validation Language (EVL) rule definitions validating expression model AST semantics and typing.

| File | Purpose |
| --- | --- |
| `_importExpression.evl` | Manifest importing all specialized EVL validation modules for attributes, collections, constants, bindings, and type expressions. |
| `attribute.evl` | Validates `AttributeSelector` expressions ensure the target attribute exists on the evaluated object type via `modelAdapter`. |
| `attributeBinding.evl` | Validates `AttributeBinding` rules ensuring assigned expressions match the data type (numeric, boolean, string, enumeration, date, timestamp) of the target attribute. |
| `collection.evl` | Enforces structural constraints on collections, navigations, and filtering, checking target collection multiplicities and element type resolution. |
| `constant.evl` | Validates constant literals, instances, custom primitives, and environment variables ensuring values and variable names are non-null and defined. |
| `custom.evl` | Validates custom expression attributes and switch cases ensuring branch types match custom target definitions. |
| `enumeration.evl` | Enforces type compatibility on `EnumerationAttribute` access and enumeration switch construct cases. |
| `expression.evl` | Validates type name resolution in model namespaces and ensures lambda variable references fall within allowed scope bounds. |
| `filterBinding.evl` | Enforces that filter binding expressions assigned to a target type evaluate to a `LogicalExpression`. |
| `logical.evl` | Validates logical predicates, comparisons, and type checks (`InstanceOfExpression`, `TypeOfExpression`), enforcing type compatibility with model hierarchies. |
| `measured.evl` | Enforces unit measurement consistency across numeric constants, attributes, difference expressions, and arithmetic operations. |
| `numeric.evl` | Enforces constraints on numeric operations, roundings, and counts, critiquing integer operations masquerading as decimal arithmetic. |
| `object.evl` | Validates object navigation references, single-object selector operators, object casting rules, and variable references. |
| `referenceBinding.evl` | Enforces type and multiplicity matching between reference bindings and assigned reference, object, or collection expressions. |
| `string.evl` | Validates string operations, concatenation, casing, substrings, replacements, string switch expressions, and aggregations. |
| `temporal.evl` | Validates temporal attributes, additions, extractions, and millisecond conversions against supported temporal measurement units. |
