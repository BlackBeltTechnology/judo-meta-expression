# `AdaptableJqlExtractor.java`

Generic `JqlExtractor` driver over any `ModelAdapter`.
`extractExpressions()` walks `getAllEntityTypes()`, then mapped, then unmapped transfer object types, and builds every derived getter/setter JQL into the expression `ResourceSet`.
Skips a feature when an `AttributeBinding`/`ReferenceBinding` with the same `TypeName`, name and role already exists, so re-extraction is idempotent.
`setBuilderConfig` retunes the wrapped `JqlExpressionBuilder`.