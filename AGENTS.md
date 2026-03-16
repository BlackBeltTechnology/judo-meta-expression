# Judo Expression Meta

EMF/Ecore metamodel for typed expressions. Eclipse/Tycho + OSGi build. Java 21, Maven 3.9.4+.

## Build

```bash
./mvnw clean install            # full build
./mvnw clean install -DskipTests # skip tests
./mvnw test -pl model-test -Dtest="ClassName" # single test
```

Profiles: `modules` (default, all submodules), `sign-artifacts`, `release-central`, `release-judong`.

## Module Map

- **`model/`** — Core metamodel (`model/model/expression.ecore`, `.genmodel`). EMF codegen via MWE2 (`src/workflow/generateModel.mwe2`). Contains both validation implementations.
- **`model-test/`** — JUnit 5 tests for metamodel and validation rules.
- **`adapter-measure/`** — Unit-of-measurement adapter.
- **`builder-jql/`** — JQL expression builder.
- **`osgi/`** — OSGi repackaging (Felix). **`osgi-itest/`** — Pax Exam/Karaf integration tests.
- **`feature-*/`** — Eclipse features. **`site/`** — P2 update site.

## Metamodel Packages (in `expression.ecore`)

`expression` (core types, TypeName, AttributeSelector, ModelAdapter) · `constant` · `variable` · `operator` · `numeric` · `logical` · `string` · `enumeration` · `object` · `collection` · `custom` · `temporal` · `binding`

## Validation

Two parallel implementations exist for the same rules:

**EVL (Epsilon):** `model/src/main/epsilon/validations/expression/*.evl` — declarative rules (attribute, collection, constant, custom, enumeration, logical, measured, numeric, object, string, temporal, attributeBinding).

**Java (Zeta):** `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/` — `ExpressionZetaValidator.java` entry point, rules in `rules/` subdirectory, constants in `constants/ValidationConstants.java`. Docs: `docs/validation/java-validation-framework.md`.

## Key Classes

- `ModelAdapter` — interface for type resolution and model navigation
- `ExpressionZetaValidator` — Java validation entry point
- `ExpressionValidationContext` — validation context with ModelAdapter access

## Code Generation Flow

1. MWE2 workflow generates EMF Java code from `expression.ecore`
2. Tycho compiles eclipse-plugin modules; Felix handles OSGi bundles
3. Features packaged → P2 update site assembled

## Tech Stack

EMF/Ecore, MWE2, Epsilon 2.8.0 (EVL), Tycho, Zeta validation framework, Karaf/Felix (OSGi), Pax Exam, JUnit 5, JaCoCo, Lombok.

## Dev Notes

- Branch: `develop`. Versioning: SNAPSHOT (1.0.5-SNAPSHOT). Placeholder: `$VERSION_PLACEHOLDER$`.
- Understand EMF/Ecore patterns before modifying generated model code.
- Respect Tycho build constraints for Eclipse plugin modules.
- `ModelAdapter` pattern — expressions use adapters for external type resolution.
- JVM config: `.mvn/jvm.config`.
- Docs: `docs/validation/README.md`, `docs/validation/tests.md`.