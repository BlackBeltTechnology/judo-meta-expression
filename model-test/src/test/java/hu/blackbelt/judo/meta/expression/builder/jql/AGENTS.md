# AGENTS.md — `model-test/src/test/java/hu/blackbelt/judo/meta/expression/builder/jql`

Unit tests for package-private `builder-jql` classes (same package; `builder-jql` is a test dependency of `model-test`).

| File | Purpose |
| --- | --- |
| `ExpressionResourceIndexTest.java` | `ExpressionResourceIndex`: bindings present before first use (role / type mismatch), single add + `addAll` after first use, removal forgets (rebuild), newly added resource, one index per resource set, type names (first in resource order, referenced-not-contained not found, incremental add, rebuild after removal). See change: JNG-6454. |
