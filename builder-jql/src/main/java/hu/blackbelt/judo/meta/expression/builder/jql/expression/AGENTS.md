# AGENTS.md — `builder-jql/src/main/java/hu/blackbelt/judo/meta/expression/builder/jql/expression`

Transformers converting JQL AST expression constructs into meta expression nodes.

| File | Purpose |
| --- | --- |
| `AbstractJqlExpressionTransformer.java` | Base template converting AST `JqlExpression` nodes into `Expression` elements via `apply(JqlExpression, ExpressionBuildingVariableResolver)` and `doTransform()`. Type casts expression operand to typed node `T`. |
| `JqlExpressionTransformerFunction.java` | Functional interface extending `BiFunction<JqlExpression, ExpressionBuildingVariableResolver, Expression>` to transform JQL AST nodes in a resolution context. |
| `JqlFunctionedExpressionTransformer.java` | Transforms `FunctionedExpression` nodes into meta expressions by evaluating operand and delegating to `jqlTransformers.applyFunctions()`. Operand must resolve to `ObjectExpression`, `CollectionExpression`, or null target type. |
| `JqlMeasuredLiteralTransformer.java` | Parses `MeasuredLiteral` AST nodes with regex `((.*)#)?(.*)` into typed `MeasuredDecimal`. Missing unit name throws `IllegalStateException`; unregistered measure names throw `IllegalArgumentException`. |
| `JqlNavigationFeatureTransformer.java` | Maps navigation feature segments into attribute, reference, or sequence selectors. Exports `transform()`, `createAttributeSelector()`, and `createReferenceSelector()`; non-`ObjectExpression` base selector throws `IllegalArgumentException`. |
| `JqlNavigationTransformer.java` | Evaluates navigational paths by resolving base variables via `createVariableReference()` or literal values and iteratively applying features. Unresolved base variable throws `IllegalArgumentException`. |
