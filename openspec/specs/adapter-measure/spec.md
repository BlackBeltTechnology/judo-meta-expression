# adapter-measure Specification

## Purpose

Provides measure and unit resolution for numeric and temporal expressions, enabling dimension-aware arithmetic, unit conversion, and duration support within the expression metamodel.

## Architecture

The adapter is centered on `MeasureAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U>`, which implements measure resolution for the `ModelAdapter` interface. It uses a `MeasureProvider<M, U>` interface to abstract the source of measure metadata (names, namespaces, units, rates, base measures). Dimension calculations map measures to base-measure/exponent pairs. A `MeasureChangedHandler<M>` interface and inner `MeasureChangeAdapter` keep the dimension cache synchronized when the measure model changes.

### Key Classes

- `MeasureAdapter` — core adapter implementing dimension calculation, unit resolution, and duration support
  - Inner class `MeasureId` — identifies a measure by name and namespace
  - Inner class `MeasureChangeAdapter` — synchronizes dimension cache with measure provider changes
- `MeasureProvider<M, U>` — interface for measure metadata access (measure name, namespace, units, base measures, rates)
- `MeasureChangedHandler<M>` — callback interface for measure model add/change/remove events

## Requirements

### Requirement: MeasureAdapter SHALL calculate dimensions from base measures

`MeasureAdapter` SHALL compute dimensions as maps of base measures to integer exponents, derived from the measure provider's base measure hierarchy.

#### Scenario: Simple measure dimension
- **GIVEN** a measure "Length" with base measure "Length" at exponent 1
- **WHEN** `MeasureAdapter` calculates the dimension
- **THEN** the result SHALL be `{Length: 1}`

#### Scenario: Derived measure dimension
- **GIVEN** a measure "Velocity" defined as Length/Time (base measures Length^1, Time^-1)
- **WHEN** `MeasureAdapter` calculates the dimension
- **THEN** the result SHALL be `{Length: 1, Time: -1}`

### Requirement: MeasureAdapter SHALL validate dimension compatibility for arithmetic

When two `MeasuredDecimal` operands are used in a `DecimalArithmeticExpression`, `MeasureAdapter` SHALL verify that their dimensions are compatible with the operation (identical for addition/subtraction, composable for multiplication/division).

#### Scenario: Addition of compatible measures
- **GIVEN** two `MeasuredDecimal` expressions both with dimension `{Length: 1}`
- **WHEN** they are operands of an addition expression
- **THEN** dimension validation SHALL pass

#### Scenario: Addition of incompatible measures
- **GIVEN** a `MeasuredDecimal` with dimension `{Length: 1}` and another with dimension `{Time: 1}`
- **WHEN** they are operands of an addition expression
- **THEN** dimension validation SHALL fail

#### Scenario: Multiplication of measures
- **GIVEN** a `MeasuredDecimal` with dimension `{Length: 1}` and another with dimension `{Length: 1}`
- **WHEN** they are operands of a multiplication expression
- **THEN** the result dimension SHALL be `{Length: 2}` (area)

### Requirement: MeasureAdapter SHALL support duration measures for temporal operations

`MeasureAdapter` SHALL identify duration measures (via `MeasureProvider.isDurationSupportingAddition`) and use them for temporal arithmetic (e.g., adding a duration to a timestamp).

#### Scenario: Timestamp plus duration
- **GIVEN** a `TimestampExpression` and a duration measure
- **WHEN** temporal arithmetic is performed
- **THEN** `MeasureAdapter` SHALL resolve the duration measure and validate the operation

### Requirement: MeasureAdapter SHALL cache dimension mappings

Dimension calculations SHALL be cached for performance. The cache SHALL be invalidated when the measure model changes via `MeasureChangedHandler` callbacks.

#### Scenario: Cache invalidation on measure change
- **GIVEN** a cached dimension for measure "Velocity"
- **WHEN** `MeasureChangedHandler.measureChanged()` is called for "Velocity"
- **THEN** the cached dimension SHALL be evicted and recalculated on next access

### Requirement: MeasureProvider SHALL abstract measure metadata access

`MeasureProvider<M, U>` SHALL provide operations for accessing measure names, namespaces, units (with names, symbols, rates), and base measure decomposition without coupling to a specific measure metamodel.

#### Scenario: Unit enumeration
- **GIVEN** a measure "Length" with units "meter", "kilometer", "mile"
- **WHEN** `MeasureProvider.getUnits(measure)` is called
- **THEN** it SHALL return all three units with their names and symbols
