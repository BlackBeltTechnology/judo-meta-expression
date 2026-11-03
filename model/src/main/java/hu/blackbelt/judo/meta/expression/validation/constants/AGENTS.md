# `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/constants` — shared constraint-name vocabulary for EVL and Zeta validation

| File | Purpose |
|---|---|
| `ValidationConstants.java` | Registry of the 62 constraint and critique names shared by the EVL rules under `model/src/main/epsilon/validations/expression/` and their Zeta counterparts. Exposes only `static final String` fields, grouped per EVL source file — `RESOLVED`, `TYPE_IS_DEFINED`, `MEASURES_ARE_MATCHING`. Private constructor blocks instantiation; a `@Constraint` spelling a literal name instead desyncs EVL from Zeta. |
