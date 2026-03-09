# model Specification

## Purpose

Defines the core EMF metamodel for type-safe expressions in the JUDO framework, providing generated model classes, runtime utilities for evaluation and validation, and Epsilon EVL constraint definitions.

## Architecture

The metamodel is defined in `expression.ecore` with a root `Expression` interface and 12 subpackages organized by expression category. EMF generates implementation classes into `src-gen/`. Hand-written runtime classes in `src/main/java` provide `ExpressionUtils` (caching utilities), `ExpressionEvaluator` (dependency tracking and traversal), `ExpressionValidator` (constraint checking), and `ModelAdapter` (abstraction for target metamodels). Validation constraints are defined in 16 EVL files under `src/main/epsilon/validations/expression/`.

### Key Classes

- `Expression` — root interface for all expression types
- `DataExpression` — expressions returning typed data values (numeric, string, logical, temporal, enumeration, custom)
- `ReferenceExpression` — expressions navigating object references (ObjectExpression, CollectionExpression)
- `SwitchExpression` / `SwitchCase` — conditional branching
- `AggregatedExpression` — aggregation over collections
- `ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U>` — generic adapter for metamodel-independent evaluation
- `ExpressionUtils` — utility methods with Guava `LoadingCache` for reflection-based property access
- `ExpressionEvaluator` — expression dependency analysis (operands, lambda functions, leaves)
- `ExpressionValidator` / `ExpressionEpsilonValidatorExecutor` — Java and EVL-based validation

### Expression Subpackages

| Subpackage | Key EClasses |
|------------|-------------|
| `constant` | IntegerConstant, DecimalConstant, BooleanConstant, StringConstant, Literal, MeasuredDecimal, DateConstant, TimestampConstant, TimeConstant, Instance, CustomData |
| `variable` | TypedVariable, ObjectVariable, CollectionVariable, EnvironmentVariable variants (Integer, Decimal, Boolean, String, etc.) |
| `operator` | IntegerOperator, DecimalOperator, LogicalOperator, StringComparator, NumericComparator enumerations |
| `numeric` | IntegerArithmeticExpression, DecimalArithmeticExpression, IntegerAggregatedExpression, DecimalAggregatedExpression, CountExpression, RoundExpression |
| `logical` | StringComparison, NumericComparison, LogicalOperation, EnumerationComparison, ObjectComparison |
| `string` | StringConcatenation, SubstringExpression, Trim, Upper, Lower, Like |
| `temporal` | DateDifference, TimestampDifference, TemporalArithmetic, Extract operations |
| `collection` | Filter, Limit, OrderBy, Head, Tail, ImmutableCollection |
| `object` | ObjectNavigationExpression, ObjectSelector, ObjectVariableReference |
| `binding` | AttributeBinding, ReferenceBinding, FilterBinding |
| `enumeration` | EnumerationExpression types |
| `custom` | Custom type expressions |

## Requirements

### Requirement: Expression type hierarchy SHALL be defined in EMF Ecore

The metamodel SHALL define all expression types as EClasses in `expression.ecore`, with a root `Expression` interface and typed subinterfaces for each data type category.

#### Scenario: DataExpression subtypes
- **GIVEN** the expression metamodel is loaded
- **WHEN** a `NumericExpression`, `StringExpression`, `LogicalExpression`, `DateExpression`, `TimestampExpression`, `TimeExpression`, `EnumerationExpression`, or `CustomExpression` is created
- **THEN** it SHALL be an instance of `DataExpression` and `Expression`

#### Scenario: ReferenceExpression subtypes
- **GIVEN** the expression metamodel is loaded
- **WHEN** an `ObjectExpression` or `CollectionExpression` is created
- **THEN** it SHALL be an instance of `ReferenceExpression` and `Expression`

### Requirement: ExpressionUtils SHALL provide cached utility methods

`ExpressionUtils` SHALL provide utility methods for expression manipulation using Guava `LoadingCache` for performance-sensitive reflection-based property access.

#### Scenario: Property access caching
- **GIVEN** an expression model is loaded
- **WHEN** `ExpressionUtils` accesses a property of an expression multiple times
- **THEN** the reflection lookup SHALL be cached and subsequent accesses SHALL use the cache

### Requirement: ExpressionEvaluator SHALL analyze expression dependencies

`ExpressionEvaluator` SHALL traverse expression trees and identify operands, lambda functions, and leaf expressions for dependency tracking.

#### Scenario: Operand extraction
- **GIVEN** an `IntegerArithmeticExpression` with left and right operands
- **WHEN** `ExpressionEvaluator` analyzes the expression
- **THEN** it SHALL return both operands as dependencies

#### Scenario: Lambda function identification
- **GIVEN** a `Filter` collection expression with an iterator variable and a predicate
- **WHEN** `ExpressionEvaluator` analyzes the expression
- **THEN** it SHALL identify the predicate as a lambda function and the iterator variable as a bound variable

### Requirement: EVL validations SHALL enforce type correctness

The 16 EVL validation files SHALL validate type consistency, dimension compatibility, variable scoping, and reference validity across all expression categories.

#### Scenario: Type mismatch in arithmetic expression
- **GIVEN** an `IntegerArithmeticExpression` where one operand is not an `IntegerExpression`
- **WHEN** EVL validation is executed via `ExpressionEpsilonValidatorExecutor`
- **THEN** a validation error SHALL be reported

#### Scenario: Dimension compatibility in measured expressions
- **GIVEN** a `DecimalArithmeticExpression` with `MeasuredDecimal` operands of incompatible dimensions
- **WHEN** EVL validation is executed
- **THEN** a validation error SHALL be reported for dimension mismatch

### Requirement: ModelAdapter SHALL provide metamodel-independent evaluation

`ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U>` SHALL define operations for type checking, measure/unit resolution, and attribute/reference navigation that can be implemented for any target metamodel.

#### Scenario: Type resolution through adapter
- **GIVEN** a `ModelAdapter` implementation for a specific metamodel
- **WHEN** an expression references a type name
- **THEN** the adapter SHALL resolve it to the corresponding type in the target metamodel

### Requirement: SwitchExpression SHALL enforce uniform case types

`SwitchExpression` SHALL contain `SwitchCase` entries that all return the same expression type.

#### Scenario: Switch with mixed return types
- **GIVEN** a `SwitchExpression` with cases returning different expression types
- **WHEN** validation is performed
- **THEN** an error SHALL be reported for non-uniform case types

### Requirement: Variable scoping SHALL be enforced in iterable expressions

`IterableExpression` instances (Filter, OrderBy, etc.) SHALL declare iterator variables whose scope is limited to the body of the iterable operation.

#### Scenario: Iterator variable scope
- **GIVEN** a `Filter` expression with an iterator variable `v`
- **WHEN** the predicate expression references `v`
- **THEN** the reference SHALL resolve correctly within scope
