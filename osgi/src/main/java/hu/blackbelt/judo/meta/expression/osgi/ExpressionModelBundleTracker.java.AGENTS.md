# `ExpressionModelBundleTracker.java`

Publishes `ExpressionModel` services found in bundle headers.
`@Component(immediate = true)`; `activate` registers register/unregister `BundleCallback`s plus an `ExpressionBundlePredicate` on the injected `BundleTrackerManager`, `deactivate` unregisters them.
Exports constant `EXPRESSION_MODELS = "Expression-Models"`: each header entry supplies `file` and `ExpressionModel.NAME`, loaded via `loadExpressionModel(...inputStream, name, bundle version)` and registered with `toDictionary()`; duplicate names are logged and skipped, so one model name may be registered only once estate-wide.
`IOException`/`ExpressionValidationException` logged, never rethrown.