# builder-jql Specification

## Purpose

Transforms JQL (JUDO Query Language) expression ASTs into Expression metamodel instances, providing a bridge between the query language layer and the type-safe expression model.

## Architecture

The transformation is driven by `ExpressionTransformer`, which traverses a JQL expression tree and produces equivalent Expression metamodel instances. Context is maintained by `ExpressionBuildingVariableResolver` for variable scoping, and `ExpressionMeasureProvider` for measure resolution. The bulk of the work is performed by 55+ function transformers, organized by category, each handling a specific JQL construct.

### Key Classes

- `ExpressionTransformer` (interface) — main entry point: `JqlExpression → Expression`
- `ExpressionBuildingVariableResolver` — maintains variable context during transformation
- `ExpressionMeasureProvider` — resolves measure references from JQL
- `PartialExpression` — intermediate representation during transformation

### Function Transformer Hierarchy

- `JqlFunctionTransformer` / `JqlParameterizedFunctionTransformer` — base interfaces
- `AbstractJqlFunctionTransformer` — abstract base with common logic

| Category | Transformers |
|----------|-------------|
| Collection | JqlAggregatedExpressionTransformer, JqlBooleanAggregatorFunctionTransformer, JqlSortFunctionTransformer, JqlObjectSelectorToFilterTransformer, JqlAnyFunctionTransformer, JqlJoinFunctionTransformer |
| Temporal | ConstructorTransformer, ExtractTransformer, ExtractFromDateTransformer, ExtractFromTimestampTransformer, JqlDifferenceFunctionTransformer, NowFunctionTransformer, TimestampArithmeticTransformer, TemporalAsMillisecondsTransformer, TemporalFromMillisecondsTransformer |
| String | JqlPaddingFunctionTransformer, JqlReplaceFunctionTransformer, JqlSubstringFunctionTransformer, JqlLikeFunctionTransformer, JqlCapitalizeFunctionTransformer |
| Numeric | JqlRoundFunctionTransformer |
| Object | JqlIsDefinedFunctionTransformer |
| Variable | GetVariableFunctionTransformer |

## Requirements

### Requirement: ExpressionTransformer SHALL convert JQL expressions to Expression metamodel instances

The transformer SHALL accept a `JqlExpression` AST node and produce an equivalent `Expression` metamodel instance that preserves the semantics of the original JQL.

#### Scenario: Simple arithmetic transformation
- **GIVEN** a JQL expression `a + b` where `a` and `b` are integer-typed
- **WHEN** `ExpressionTransformer` processes the expression
- **THEN** it SHALL produce an `IntegerArithmeticExpression` with `IntegerOperator.ADD` and references to the resolved variables

#### Scenario: Navigation expression transformation
- **GIVEN** a JQL expression `order.customer.name`
- **WHEN** `ExpressionTransformer` processes the expression
- **THEN** it SHALL produce nested `ObjectNavigationExpression` and `AttributeSelector` instances

### Requirement: Variable resolution SHALL maintain scope during transformation

`ExpressionBuildingVariableResolver` SHALL track variable declarations and resolve references within the correct scope, including iterator variables in collection operations.

#### Scenario: Iterator variable in filter
- **GIVEN** a JQL expression `items!filter(i | i.price > 100)`
- **WHEN** the transformer processes the filter
- **THEN** `ExpressionBuildingVariableResolver` SHALL create an iterator variable `i` scoped to the filter predicate and resolve references to `i` within that scope

### Requirement: Function transformers SHALL handle all JQL built-in functions

Each JQL built-in function SHALL have a corresponding function transformer that produces the correct Expression metamodel representation.

#### Scenario: String function transformation
- **GIVEN** a JQL expression `name!upperCase()`
- **WHEN** the string function transformer processes it
- **THEN** it SHALL produce an `Upper` expression with the source `StringExpression`

#### Scenario: Temporal extraction
- **GIVEN** a JQL expression `timestamp!year()`
- **WHEN** `ExtractFromTimestampTransformer` processes it
- **THEN** it SHALL produce an extract expression for the year component

#### Scenario: Collection aggregation
- **GIVEN** a JQL expression `items!sum(i | i.quantity)`
- **WHEN** `JqlAggregatedExpressionTransformer` processes it
- **THEN** it SHALL produce an aggregated expression with the appropriate aggregation operator and iterator

### Requirement: Measure resolution SHALL be available during JQL transformation

`ExpressionMeasureProvider` SHALL resolve measure and unit references encountered in JQL expressions so that `MeasuredDecimal` constants and measure-aware operations are correctly constructed.

#### Scenario: Measured decimal literal
- **GIVEN** a JQL expression containing a measured decimal value `5.0 [kg]`
- **WHEN** the transformer processes it
- **THEN** `ExpressionMeasureProvider` SHALL resolve the unit `kg` and produce a `MeasuredDecimal` constant with the correct measure and unit references

### Requirement: PartialExpression SHALL support incremental construction

During transformation of complex expressions, `PartialExpression` SHALL allow intermediate results to be accumulated and composed into the final expression.

#### Scenario: Chained operations
- **GIVEN** a JQL expression `items!filter(i | i.active)!sort(i | i.name)!head(5)`
- **WHEN** the transformer processes the chain
- **THEN** each operation SHALL produce a `PartialExpression` that feeds into the next, producing a nested `Head(OrderBy(Filter(...)))` expression
