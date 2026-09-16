# `JqlTimestampLiteralTransformer.java`

`JqlExpressionTransformerFunction` turning a `TimestampLiteral` into a `TimestampConstant`.
Tries `ISO_LOCAL_DATE_TIME` first, then falls back to `ISO_OFFSET_DATE_TIME` normalised to UTC via `atZoneSameInstant(ZoneOffset.UTC)` — so an offset literal loses its original zone.
Both parses failing throws `IllegalArgumentException("Unable to parse string (...) to LocalDateTime")` chaining the first failure, not the second.