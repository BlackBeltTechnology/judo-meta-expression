# `JqlExpressionBuilder.java`

Central JQL→expression facade, generic over the twelve metamodel type parameters
`<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U>` that a `ModelAdapter` binds.

## Key exports

- `parseJqlString(String)` — delegates to the injected `JqlParser`, returns a `JqlExpression` AST.
- `createExpression(CreateExpressionArguments<C, TO, NE>)` — the build entry point.
- `storeExpression(Expression)` — appends to `expressionResource.getContents()`.
- `createBinding(BindingContext, C entityType, TO transferObjectType, Expression)` and
  `createGetterBinding(C base, Expression, String feature, BindingType)`.
- Name resolution: `buildTypeName(NE)` / `buildTypeName(QualifiedName)` / `buildTypeName(namespace, name)`,
  `getTypeNameFromResource`, `getEnumTypeName`, `getMeasureName`, `getDurationMeasureName`,
  `getDurationMeasures`, static `getTypeNameOf(QualifiedName, Supplier...)`.
- Extension seams: `overrideTransformer(Class<? extends JqlExpression>, Function<JqlTransformers, ...>)`,
  `addFunctionTransformer(String, Function<JqlTransformers, JqlFunctionTransformer>)`,
  `setResolveDerived(boolean)`, `setBuilderConfig(JqlExpressionBuilderConfig)`.
- Type comparison: `isKindOf(C, C)`, `getMappedTransferObjectTypeCompatibility(TO, TO)`.
- Nested types: `enum BindingRole`, `enum BindingType`, `class BindingContext`
  (`getFeatureName`, `getType`, `getRole`), `enum MappedTransferObjectCompatibility`.
- Constants: `SELF_NAME = "self"`, `NAMESPACE_SEPARATOR = "::"`.

## Contracts a caller can violate

- `createExpression(null)` throws `IllegalArgumentException`; so does an argument bag where both
  `jqlExpression` and `jqlExpressionAsString` are null.
- `createExpression` pushes `arguments.getClazz()` as the base and, when that class has a cached
  `Instance`, pushes it as a variable and pops it again afterwards. A caller who supplies its own
  `context` gets that context mutated, not a copy.
- Context namespace is derived once: it is only set when `getContextNamespace()` is still empty, from
  `contextNamespacePreset` falling back to `clazz`. Presetting a namespace on the context wins.
- `createBinding` throws `IllegalArgumentException` unless an entity type or a transfer object type is
  given, and `IllegalStateException` for an unsupported binding role or feature.
- `getTypeNameOf` throws `IllegalArgumentException` on a null `QualifiedName` or a short-form symbol
  that is not in scope, and `NoSuchElementException` when the type or symbol is unknown.
- `storeExpression(null)` is not an error — it logs a warning and stores nothing.
