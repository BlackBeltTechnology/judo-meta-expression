# `model-test/src/test/java/hu/blackbelt/judo/meta/expression/adapters/measure` — MeasureAdapter unit tests

| File | Purpose |
|---|---|
| `DummyMeasureProvider.java` | In-memory `MeasureProvider<Measure, Unit>` fixture — `getBaseMeasures`, `isBaseMeasure`, `setMeasureChangeHandler` → see `DummyMeasureProvider.java.AGENTS.md` |
| `MeasureAdapterTest.java` | `MeasureAdapter` tests over mocked `ModelAdapter` — `getUnit`, `refreshDimensions`, `isMeasured`, `attributeUnits` → see `MeasureAdapterTest.java.AGENTS.md` |
| `MeasureChangeAdapterTest.java` | `MeasureAdapter.dimensions` bookkeeping under provider events — `MeasureChangedHandler`, `measureAdded`/`measureChanged`/`measureRemoved` → see `MeasureChangeAdapterTest.java.AGENTS.md` |
