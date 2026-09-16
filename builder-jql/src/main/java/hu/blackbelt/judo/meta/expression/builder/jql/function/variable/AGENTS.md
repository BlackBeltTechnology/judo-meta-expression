# AGENTS.md — `builder-jql/src/main/java/hu/blackbelt/judo/meta/expression/builder/jql/function/variable`

Variable access function transformers.

| File | Purpose |
| --- | --- |
| `GetVariableFunctionTransformer.java` | Transforms `getVariable(category, name)` calls on type names into typed `EnvironmentVariable` references. Requires 2 parameters; variable name must be constant when `forceConstantVariable` is enabled. |
