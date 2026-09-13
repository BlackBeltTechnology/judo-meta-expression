# `JqlTransformers.java`

Registry and dispatcher that turns a parsed JQL AST into expression-metamodel objects.
Implements `ExpressionTransformer` and `ExpressionMeasureProvider`; constructed from a
`JqlExpressionBuilder` and generic over the same twelve metamodel type parameters.

## Registration

The constructor fills two `LinkedHashMap`s by calling, in order, `literals()`, `operations()`,
`primitiveFunctions()`, `stringFunctions()`, `numericFunctions()`, `temporalFunctions()`,
`environmentVariableFunctions()`, `collectionFunctions()`, `objectFunctions()`, `sequenceFunctions()`.

- `transformers` — `Class<? extends JqlExpression>` → `JqlExpressionTransformerFunction`.
- `functionTransformers` — lower-case function name → `JqlFunctionTransformer`. Registered names include
  `isdefined`/`isundefined`, `kindof`, `typeof`, `astype`, `container`, `count`, `head`/`heads`,
  `tail`/`tails`, `any`, `filter`, `exists`, `forall`, `anytrue`/`alltrue`/`anyfalse`/`allfalse`,
  `empty`, `join`, `sort`, `min`/`max`/`sum`/`avg`, `round`/`abs`/`ceil`/`floor`, `getvariable`, `now`,
  `elapsedtimefrom`, `year`/`month`/`day`/`dayofweek`/`dayofyear`/`hour`/`minute`/`second`/`millisecond`,
  `date`/`time`, `of`, `frommilliseconds`/`asmilliseconds`, `plusyears`…`plusmilliseconds`,
  `asstring`, `length`.

## Key exports

- `transform(JqlExpression, ExpressionBuildingVariableResolver)` — the `ExpressionTransformer` entry.
- `applyFunctions(JqlExpression, Expression baseExpression, C objectType, ExpressionBuildingVariableResolver)`
  — walks the `FunctionCall` chain of a `FunctionedExpression`, feeding each result into the next call.
- `overrideTransformer(Class, Function)` and `addFunctionTransformer(String, Function)` — both replace an
  existing entry for the same key, which is how metamodel-specific builders specialise the core.
- `getModelAdapter`, `getExpressionBuilder`, `getEnumType`, `getTypeNameFromResource`, `buildTypeName`.
- `getMeasureName`, `getDurationMeasureName`, `getDurationMeasures`, `getDefaultDurationMeasure`.
- `setResolveDerived(boolean)` / `isResolveDerived()`, `newGeneratedIteratorName()`.
- Message templates `CAST_FUNCTION_INVALID_TYPES`, `INVALID_CONTAINER_TYPE`.

## Contracts a caller can violate

- `transform` picks the first registered transformer whose key `isAssignableFrom` the node class —
  registration order decides which of two compatible entries wins. No match throws
  `UnsupportedOperationException`.
- Function lookup lower-cases the name, so registering a mixed-case key makes it unreachable.
- A `FunctionCall` with a null function name raises `JqlExpressionBuildException` carrying the partial
  subject expression.
- A lambda argument opens a variable scope per call and increments a lambda depth counter that must be
  unwound — a transformer that pops the scope itself corrupts the stack.
- `getDefaultDurationMeasure()` returns a value only when the model declares exactly one duration
  measure; otherwise `Optional.empty()`.
- `newGeneratedIteratorName()` returns `_iterator_<n>` from a per-instance `AtomicInteger` starting at 1,
  so generated names are unique per `JqlTransformers`, not globally.
- `kindof`/`typeof`/`astype` throw `IllegalArgumentException` formatted with
  `CAST_FUNCTION_INVALID_TYPES` when the two fully qualified types are not cast-compatible;
  `container` throws with `INVALID_CONTAINER_TYPE`.
