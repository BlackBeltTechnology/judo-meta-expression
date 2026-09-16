# AGENTS.md — `builder-jql/src/main/java/hu/blackbelt/judo/meta/expression/builder/jql/function/string`

String manipulation function transformers for capitalizing, pattern matching, padding, replacing, and slicing.

| File | Purpose |
| --- | --- |
| `JqlCapitalizeFunctionTransformer.java` | Transforms string capitalization by combining uppercase of first character and substring of remainder; non-`StringExpression` operand throws `IllegalArgumentException`. |
| `JqlLikeFunctionTransformer.java` | Transforms `like` pattern matching into a `LikeExpression` supporting case sensitivity settings; operand must be `StringExpression` and parameter count must equal 1. |
| `JqlPaddingFunctionTransformer.java` | Converts string padding invocations into `PaddingExpression` with left or right padding types; defaults pad character to space when omitted. |
| `JqlReplaceFunctionTransformer.java` | Transforms string replace calls into `Replace` expressions with pattern and replacement string expressions. |
| `JqlSubstringFunctionTransformer.java` | Transforms substring operations on `StringExpression` using 0-based position and length integer parameters. |
