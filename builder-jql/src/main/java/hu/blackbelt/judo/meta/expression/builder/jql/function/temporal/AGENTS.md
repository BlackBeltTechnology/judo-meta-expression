# AGENTS.md — `builder-jql/src/main/java/hu/blackbelt/judo/meta/expression/builder/jql/function/temporal`

Temporal function transformers for date, time, and timestamp construction, extraction, arithmetic, and conversion.

| File | Purpose |
| --- | --- |
| `ConstructorTransformer.java` | Builds date, time, and timestamp literal expressions from constructor parameters on `TypeNameExpression`. Argument must identify a temporal primitive type in the model adapter. |
| `ExtractFromDateTransformer.java` | Extracts specified `DatePart` components from a `DateExpression` into an `ExtractDateExpression`; parameters list must be empty. |
| `ExtractFromTimestampTransformer.java` | Extracts `DATE` or `TIME` components from a `TimestampExpression`; unsupported parts or non-timestamp arguments throw `IllegalArgumentException`. |
| `ExtractTransformer.java` | Extracts temporal unit parts (`ChronoUnit`) from date, timestamp, or time expressions into numeric expressions. |
| `JqlDifferenceFunctionTransformer.java` | Calculates duration difference between temporal expressions (`DateExpression`, `TimestampExpression`, `TimeExpression`) using configured measurement units. |
| `NowFunctionTransformer.java` | Resolves current system clock variable expressions (`current_date`, `current_timestamp`, `current_time`) from temporal type name tokens. Must have zero parameters. |
| `TemporalAsMillisecondsTransformer.java` | Converts time or timestamp expressions into millisecond numeric expressions; parameter count must be 0. |
| `TemporalFromMillisecondsTransformer.java` | Instantiates time or timestamp expressions from millisecond numeric values passed to a temporal type name expression. |
| `TimestampArithmeticTransformer.java` | Adds or subtracts integer intervals on timestamp expressions along a target `TimestampPart`. Requires exactly 1 `IntegerExpression` parameter. |
