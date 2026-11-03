# `AdaptableJqlExtractor.java`

Generic `JqlExtractor` driver over any `ModelAdapter`.
`extractExpressions()` walks `getAllEntityTypes()`, then mapped, then unmapped transfer object types, and builds every derived getter/setter JQL into the expression `ResourceSet`.
Skips a feature when an `AttributeBinding`/`ReferenceBinding` with the same `TypeName`, name and role already exists, so re-extraction is idempotent.
`setBuilderConfig` retunes the wrapped `JqlExpressionBuilder`.
Calls `builder.resetIteratorCounter()` before each top-level `createExpression` — in `buildAndBind` and in the mapped transfer object filter extraction.
Iterator names (`_iterator_<n>`) so do not depend on ASM extraction order (ETL vs Zeta psm2asm order differs).
Implements spec `runtime/judo-tatami-base/openspec/specs/expression-iterator-determinism` (archived change `expression-iterator-counter-reset`).
Test: `AsmJqlExtractorTest.testIteratorNamesDoNotDependOnExtractionOrder` (`judo-meta-expression-asm/builder-jql-asm-test`) — extracts, reverses every EPackage classifier list in a copy in fresh `ResourceSet` (`AsmUtilsCache` caches order per `ResourceSet`), asserts binding(type, feature, role) → iterator names equal.
See change: split-etl-artifacts (iterator determinism).
