# AGENTS.md — `model-test/src/test/java/hu/blackbelt/judo/meta/expression/adapters/measure/model`

Mock measurement domain model representing measures and associated units for expression adapter tests.

| File | Purpose |
| --- | --- |
| `Measure.java` | Defines measure entity containing namespace, name, and unit collection. Exports `Measure`, `MeasureBuilder`, `builder()`, `getNamespace()`, `getName()`, `getUnits()`. Constructed via `MeasureBuilder.build()`. |
| `Unit.java` | Defines measurement unit containing name and symbol. Exports `Unit`, `UnitBuilder`, `builder()`, `getName()`, `getSymbol()`. Constructed via `UnitBuilder.build()`. |
