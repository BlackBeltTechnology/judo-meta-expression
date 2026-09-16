# `CreateExpressionArguments.java`

Immutable argument bag for `JqlExpressionBuilder.createExpression`: `clazz`, `inputParameterType`, `contextNamespacePreset`, `jqlExpression`, `jqlExpressionAsString`, `context` (`ExpressionBuildingVariableResolver`).
Constructor private — instances come only from `builder()` plus `CreateExpressionArgumentsBuilder.withX(...).build()`.
At least one of `jqlExpression` / `jqlExpressionAsString` must be set or `createExpression` rejects the bag.