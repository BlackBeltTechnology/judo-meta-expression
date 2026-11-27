# AGENTS.md — `model-test/src/test/java/hu/blackbelt/judo/meta/expression/builder/jql`

Unit tests for package-private `builder-jql` classes (same package; `builder-jql` is a test dependency of `model-test`).

| File | Purpose |
| --- | --- |
| `BindingIndexTest.java` | `BindingIndex`: bindings present before first use (role / type mismatch), single add + `addAll` after first use, removal forgets (rebuild), bindings of a newly added resource. See change: JNG-6454. |
