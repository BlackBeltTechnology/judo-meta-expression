# `MeasureAdapter.java`

Resolves the measure and the dimension of a `NumericExpression` against a `MeasureProvider`,
so numeric type checking can decide whether two operands are dimensionally compatible.

Generic over `<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U>`; `M` is the measure type and `U` the
unit type of the underlying metamodel, reached only through the injected `MeasureProvider<M, U>`
and `ModelAdapter`.

## Key exports

- `Optional<M> get(MeasureName)` — measure by expression-metamodel namespace + name.
- `boolean isMeasured(NumericExpression)` — `MeasuredDecimal`, `MeasuredDecimalEnvironmentVariable`
  and the three temporal difference expressions are always measured; switch expressions are
  measured when any case or the default is measured; arithmetic is measured when its dimension is
  non-empty.
- `Optional<Map<MeasureId, Integer>> getDimension(NumericExpression)` — base measures with exponents.
- `Optional<M> getMeasure(NumericExpression)`, `M getMeasure(U unit)`.
- `Optional<U> getUnit(Optional<String> measureNamespace, Optional<String> measureName, String unitNameOrSymbol)`.
- `Optional<M> getDurationMeasure()` — first measure owning an additive duration unit.
- `public static class MeasureId` — `namespace::name` value object with `fromMeasure(provider, measure)`.

## Contracts a caller can violate

- The constructor installs its own private `MeasureChangeAdapter` via
  `measureProvider.setMeasureChangeHandler(...)`. Installing another handler afterwards silently
  stops dimension-cache maintenance.
- `dimensions` is keyed by the base-measure exponent map. Adding a measure whose key is already
  present logs an error and throws `IllegalArgumentException` — two measures may not share a dimension.
- `ADD`/`SUBSTRACT` require equal operand dimensions; mixing scalar and measured operands logs
  `"Addition of scalar and measured values is not allowed"` and yields `Optional.empty()`.
- `MULTIPLY`/`DIVIDE`/`MODULO` add or subtract exponents and drop zero exponents from the map.
- Temporal difference expressions need a base duration measure to exist; without one `getDimension`
  logs `"No base measure is defined for temporal expressions"` and returns empty.
- An unsupported arithmetic operator reaches `throw new IllegalArgumentException("Unsupported operation")`.
