# `JqlExpressionBuilderModelAdapterCache.java`

Per-`ModelAdapter` prebuilt lookup of `Instance`, `TypeName` and `MeasureName` maps over a throwaway `expression:filter-<uuid>` resource.
`getCache(ModelAdapter)` is the only entry point — the constructor is private and populates measures, entity types, enums, sequences, transfer object types, actors and primitive types eagerly.
Guava `LoadingCache` expires entries after `JqlExpressionBuilderModelAdapterCacheExpiration` seconds without access (default 86400).

## storeTypeName (JNG-6454)

- Adds the type name only when `ExpressionResourceIndex.typeName(name, namespace)` is null (same `Objects.equals` semantics as the former `noneMatch` walk over all `TypeName`s, ~18 % of `asm2expression`). See change: JNG-6454.
