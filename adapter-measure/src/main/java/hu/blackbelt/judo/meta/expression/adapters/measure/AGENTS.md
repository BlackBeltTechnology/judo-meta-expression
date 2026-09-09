# `adapter-measure/src/main/java/hu/blackbelt/judo/meta/expression/adapters/measure` — measure/unit resolution for numeric expressions

| File | Purpose |
|---|---|
| `MeasureAdapter.java` | Resolves measure and dimension of a `NumericExpression` against a `MeasureProvider`; caches dimensions keyed by base-measure exponent map. → see `MeasureAdapter.java.AGENTS.md` |
| `MeasureChangedHandler.java` | Callback contract a `MeasureProvider` fires so `MeasureAdapter` can keep its dimension cache in sync. Declares `measureAdded(M)`, `measureChanged(M)`, `measureRemoved(M)`. One handler per provider, installed through `MeasureProvider.setMeasureChangeHandler`; a provider that mutates measures without firing these leaves stale dimensions. |
| `MeasureProvider.java` | Read side of the underlying (PSM or measure) metamodel that `MeasureAdapter` walks. Declares `getMeasure(namespace, name)`, `getMeasures()`, `getUnits(M)`, `getBaseMeasures(M)` returning an `EMap<M, Integer>` of exponents, `getUnitByNameOrSymbol`, `isBaseMeasure`, `isDurationSupportingAddition`, `setMeasureChangeHandler`. Month and year units must be reported non-additive. |
