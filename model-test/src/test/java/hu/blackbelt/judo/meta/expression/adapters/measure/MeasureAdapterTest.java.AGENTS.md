# `MeasureAdapterTest.java`

Exercises `MeasureAdapter` over `DummyMeasureProvider` with a mocked `ModelAdapter` whose `getUnit(NumericExpression)` resolves `NumericAttribute` names through the `attributeUnits` map and `MeasuredDecimal` through the adapter itself.
Covers `get(MeasureName)`, duration measure lookup, `refreshDimensions`, `getUnit`, and `isMeasured` for measured decimals, numeric attributes, arithmetic, opposite, aggregated, switch and timestamp-difference expressions.
`@AfterEach` nulls the adapter and clears `attributeUnits`, so tests must not rely on cross-test state.