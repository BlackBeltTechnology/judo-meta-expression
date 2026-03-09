# osgi Specification

## Purpose

Provides a standalone OSGi bundle that repackages the core expression model and adds dynamic model loading and service registration for use in transformation pipelines outside Eclipse.

## Architecture

The module centers on `ExpressionModelBundleTracker`, an OSGi component that listens for bundles containing expression models. When a bundle with an `Expression-Models` header is activated, the tracker loads the referenced models and registers them as OSGi services. This enables consumers in OSGi environments to discover and use expression models dynamically without compile-time coupling.

### Key Classes

- `ExpressionModelBundleTracker` — OSGi bundle tracker component
  - Monitors bundle lifecycle events (ACTIVE, STOPPING)
  - Reads `Expression-Models` manifest header from tracked bundles
  - Loads expression model resources from bundle entries
  - Registers/unregisters `ExpressionModel` as OSGi services

## Requirements

### Requirement: ExpressionModelBundleTracker SHALL discover expression models from active bundles

The tracker SHALL scan active bundles for the `Expression-Models` manifest header and load the referenced model resources.

#### Scenario: Bundle with expression models activates
- **GIVEN** an OSGi runtime with `ExpressionModelBundleTracker` active
- **WHEN** a bundle with header `Expression-Models: models/myExpressions.model` becomes ACTIVE
- **THEN** the tracker SHALL load the model resource and register an `ExpressionModel` OSGi service

#### Scenario: Bundle without expression models activates
- **GIVEN** an OSGi runtime with `ExpressionModelBundleTracker` active
- **WHEN** a bundle without an `Expression-Models` header becomes ACTIVE
- **THEN** the tracker SHALL ignore the bundle

### Requirement: ExpressionModelBundleTracker SHALL unregister models when bundles stop

When a tracked bundle transitions out of the ACTIVE state, the tracker SHALL unregister the corresponding OSGi service.

#### Scenario: Tracked bundle stops
- **GIVEN** an `ExpressionModel` service registered for bundle `com.example.myapp`
- **WHEN** the bundle `com.example.myapp` is stopped
- **THEN** the tracker SHALL unregister the `ExpressionModel` service

### Requirement: The OSGi bundle SHALL export all expression packages

The standalone bundle SHALL export all expression metamodel packages so that consumer bundles can import and use expression types.

#### Scenario: Consumer bundle imports expression types
- **GIVEN** the `hu.blackbelt.judo.meta.expression.osgi` bundle is installed and active
- **WHEN** a consumer bundle declares `Import-Package: hu.blackbelt.judo.meta.expression`
- **THEN** the OSGi framework SHALL resolve the import from the expression bundle

### Requirement: OSGi integration SHALL be verifiable via PAX Exam

The `osgi-itest` module SHALL verify bundle activation, package export resolution, and service registration using PAX Exam on Apache Karaf.

#### Scenario: Bundle activation test
- **GIVEN** a Karaf instance provisioned with the expression OSGi bundle
- **WHEN** `ExpressionBundleITest` runs
- **THEN** the bundle SHALL be in ACTIVE state and the expression model service SHALL be registered
