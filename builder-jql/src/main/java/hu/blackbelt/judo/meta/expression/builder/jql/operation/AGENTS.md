# AGENTS.md — `builder-jql/src/main/java/hu/blackbelt/judo/meta/expression/builder/jql/operation`

AST operator transformers for binary, ternary conditional, and unary expressions.

| File | Purpose |
| --- | --- |
| `JqlBinaryOperationTransformer.java` | Transforms binary operations (arithmetic, logical `AND`/`OR`/`XOR`/`IMPLIES`, string concatenation, temporal additions) into meta expression equivalents. |
| `JqlTernaryOperationTransformer.java` | Converts conditional ternary operations into typed `SwitchExpression` instances (`ObjectSwitchExpression`, `CollectionSwitchExpression`, or primitive switches) with condition and default branches. |
| `JqlUnaryOperationTransformer.java` | Transforms unary prefix operators into opposite expressions (`-` on integers or decimals) or `NegationExpression` (`not` on logical operands). Unsupported operators throw `UnsupportedOperationException`. |
