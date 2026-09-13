# `model/src/workflow` — EMF code generation workflow

| File | Purpose |
|---|---|
| `generateModel.mwe2` | MWE2 workflow `ExpressionModelBuilder` regenerating the expression metamodel sources. Wipes `src-gen`, runs `EcoreGenerator` over `model/expression.genmodel` with `generateCustomClasses = false` into `src/main/java`, then the JUDO `HelperGeneratorWorkflow`, `BuilderGeneratorWorkflow` and `RuntimeModelGeneratorWorkflow`. `rootPath` must be the module dir; `platformUri` is pinned to `platform:/resource/hu.blackbelt.judo.meta.expression.model`, so hand edits under `src-gen` are lost on the next run. |
