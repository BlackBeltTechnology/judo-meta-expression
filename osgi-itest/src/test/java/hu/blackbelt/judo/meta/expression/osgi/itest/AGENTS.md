# AGENTS.md — `osgi-itest/src/test/java/hu/blackbelt/judo/meta/expression/osgi/itest`

Pax Exam OSGi integration tests validating expression bundles and Karaf container feature deployment.

| File | Purpose |
| --- | --- |
| `ExpressionBundleITest.java` | Pax Exam integration test verifying OSGi bundle activation and expression model validation within Apache Karaf. Exports `ExpressionBundleITest`, `config()`, `testBundleActive()`, `getProvisonModelBundle()`. Injects `BundleTrackerManager` and checks bundle lifecycle. |
| `KarafFeatureProvider.java` | Configures Apache Karaf test container distribution and OSGi bundle options for Pax Exam. Exports `KarafFeatureProvider`, `karafUrl()`, `karafConfig()`, `getFreePort()`, `getOsgiService()`, `assertBundleStarted()`. |
