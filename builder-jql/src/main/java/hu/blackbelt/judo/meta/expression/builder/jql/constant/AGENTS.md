# `builder-jql/src/main/java/hu/blackbelt/judo/meta/expression/builder/jql/constant` — temporal JQL literals parsed into expression-model constants

| File | Purpose |
|---|---|
| `JqlDateLiteralTransformer.java` | `JqlExpressionTransformerFunction` turning a `DateLiteral` into a `DateConstant` via `newDateConstantBuilder().withValue(LocalDate)`. Parses strictly with `DateTimeFormatter.ISO_LOCAL_DATE`, so any other date shape raises `DateTimeParseException` uncaught. Casts the incoming `JqlExpression` to `DateLiteral` without a check — registering it for another node class yields `ClassCastException`. |
| `JqlTimeLiteralTransformer.java` | `JqlExpressionTransformerFunction` turning a `TimeLiteral` into a `TimeConstant` via `newTimeConstantBuilder().withValue(LocalTime)`, parsed with `DateTimeFormatter.ISO_LOCAL_TIME`. Zone and offset are not accepted — only a local time-of-day parses. Ignores the `ExpressionBuildingVariableResolver` argument entirely. |
| `JqlTimestampLiteralTransformer.java` | `JqlExpressionTransformerFunction` mapping `TimestampLiteral` → `TimestampConstant` via `ISO_LOCAL_DATE_TIME`/`ISO_OFFSET_DATE_TIME` (UTC-normalised) → see `JqlTimestampLiteralTransformer.java.AGENTS.md` |
