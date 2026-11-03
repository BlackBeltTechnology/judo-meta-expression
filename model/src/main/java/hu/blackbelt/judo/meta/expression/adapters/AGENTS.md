# AGENTS.md — `model/src/main/java/hu/blackbelt/judo/meta/expression/adapters`

Core metamodel abstraction adapter interface for expression evaluation.

| File | Purpose |
| --- | --- |
| `ModelAdapter.java` | Metamodel abstraction decoupling expression evaluation from concrete underlying meta-models (entity, transfer object, primitive, measure). Exports `buildTypeName()`, `get()`, `isObjectType()`, and fraction calculators `getSecondUnitFraction()`, `getDayUnitFraction()`. |
