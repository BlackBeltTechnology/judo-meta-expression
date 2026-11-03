# `generateModel.mwe2`

MWE2 workflow `ExpressionModelBuilder` regenerating the expression metamodel sources.
Wipes `src-gen`; runs `EcoreGenerator` over `model/expression.genmodel`, `generateCustomClasses = false`, into `src/main/java`; then JUDO `HelperGeneratorWorkflow`, `BuilderGeneratorWorkflow`, `RuntimeModelGeneratorWorkflow`.
`rootPath` must be the module dir; `platformUri` pinned to `platform:/resource/hu.blackbelt.judo.meta.expression.model`; hand edits under `src-gen` are lost on the next run.