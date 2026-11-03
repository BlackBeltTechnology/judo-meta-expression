# `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/rules/constant` — Zeta rules asserting literal constants and environment variables carry a value

| File | Purpose |
|---|---|
| `ConstantExpressionValidations.java` | Thirteen guarded rules over constant, environment-variable and `Instance` leaf expressions; each records `RESOLVED` so downstream rules can guard on it. → see `ConstantExpressionValidations.AGENTS.md` |
