# AGENTS.md — `builder-jql/src/main/java/hu/blackbelt/judo/meta/expression/builder/jql/function/collection`

Collection function transformers for aggregation, selection, boolean folding, concatenation, and sorting.

| File | Purpose |
| --- | --- |
| `JqlAggregatedExpressionTransformer.java` | Transforms collection aggregations into type-specific `AggregatedExpression` nodes. Factory methods `createMinInstance()`, `createMaxInstance()`, `createSumInstance()`, and `createAvgInstance()` map integer, decimal, string, and temporal aggregators. |
| `JqlAnyFunctionTransformer.java` | Converts `any()` function invocations on a `CollectionExpression` into an `ObjectSelectorExpression` configured with `ObjectSelector.ANY`. |
| `JqlBooleanAggregatorFunctionTransformer.java` | Transforms `anyTrue`, `allTrue`, `anyFalse`, and `allFalse` collection calls into logical predicates. Requires exactly one condition parameter; non-`CollectionExpression` target or invalid parameter count throws `IllegalArgumentException`. |
| `JqlJoinFunctionTransformer.java` | Converts collection join invocations into `ConcatenateCollection` expressions taking mapped text and separator parameters. |
| `JqlObjectSelectorToFilterTransformer.java` | Rewrites object selector expressions (`ANY`, `HEAD`, `HEADS`, `TAIL`) on collections into filtered collection expressions using existential sub-predicates. |
| `JqlSortFunctionTransformer.java` | Transforms collection sort operations into `SortExpression` nodes containing ordered `OrderByItem` clauses with ascending or descending flags. |
