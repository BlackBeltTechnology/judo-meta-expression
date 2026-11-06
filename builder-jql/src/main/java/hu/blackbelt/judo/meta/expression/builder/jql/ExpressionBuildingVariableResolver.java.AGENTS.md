# `ExpressionBuildingVariableResolver.java`

Contract for the mutable stack state carried through one JQL build: `pushAccessor`/`popAccessor`/`containsAccessor`, `pushVariable`/`popVariable`/`resolveVariable(String)`, base and base-expression stacks, `pushVariableScope`/`popVariableScope`, input parameter type, context namespace.
`resolveOnlyCurrentLambdaScope()` decides whether name lookup crosses lambda scopes.
Every push must be matched by a pop or later nodes resolve against a corrupted scope.