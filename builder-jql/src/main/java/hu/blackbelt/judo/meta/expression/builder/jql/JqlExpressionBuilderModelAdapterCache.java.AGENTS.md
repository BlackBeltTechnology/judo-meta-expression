# `JqlExpressionBuilderModelAdapterCache.java`

Per-`ModelAdapter` prebuilt lookup of `Instance`, `TypeName` and `MeasureName` maps over a throwaway `expression:filter-<uuid>` resource.
`getCache(ModelAdapter)` is the only entry point — the constructor is private and populates measures, entity types, enums, sequences, transfer object types, actors and primitive types eagerly.
Guava `LoadingCache` expires entries after `JqlExpressionBuilderModelAdapterCacheExpiration` seconds without access (default 86400).