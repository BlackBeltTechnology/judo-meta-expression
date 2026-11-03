# AGENTS.md — `model/src/main/java/hu/blackbelt/judo/eclipse/expression`

Eclipse UI plugin activator managing lifecycle and bundle context.

| File | Purpose |
| --- | --- |
| `Activator.java` | Controls OSGi plug-in lifecycle for bundle `hu.blackbelt.judo.meta.expression`. Exports singleton accessor `getDefault()` and manages plugin context in `start()` and `stop()`. |
