# JUDO Expression Metamodel - Project Documentation

## Project Overview


**Repository:** BlackBeltTechnology/judo-meta-expression
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.9.9+ with Eclipse Tycho 4.0.13

1. Defines the EMF (Eclipse Modeling Framework) metamodel for type-safe expressions in the JUDO framework — covering numeric, string, logical, temporal, collection, enumeration, and custom expression types.
2. Provides runtime utilities for expression evaluation, validation (via Epsilon EVL), and model manipulation.
3. Includes an adapter layer (`adapter-measure`) for measure/unit-aware numeric and temporal operations with dimension calculation.
4. Ships a JQL (JUDO Query Language) builder (`builder-jql`) with 55+ function transformers that convert JQL ASTs into Expression metamodel instances.
5. Dual-packaged as Eclipse plugins (with features and P2 update site) and a standalone OSGi bundle for non-Eclipse environments.

## Code Instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Directory Structure

```
judo-meta-expression/
├── model/                        # Core EMF metamodel (eclipse-plugin)
│   ├── model/                    # expression.ecore + expression.genmodel
│   ├── src/main/java/            # Runtime: ExpressionUtils, ExpressionEvaluator, ExpressionValidator
│   ├── src/main/epsilon/         # EVL validation constraint files (16 files)
│   ├── src/workflow/             # MWE2 code generation workflow
│   ├── src-gen/                  # EMF generated Java code (DO NOT EDIT)
│   └── META-INF/MANIFEST.MF     # OSGi bundle manifest
├── adapter-measure/              # Measure/unit adapter (eclipse-plugin)
├── builder-jql/                  # JQL→Expression transformer (eclipse-plugin)
├── model-test/                   # Unit tests (JUnit 5)
├── osgi/                         # Standalone OSGi bundle
├── osgi-itest/                   # OSGi integration tests (PAX Exam + Karaf)
├── feature-model/                # Eclipse feature for model
├── feature-adapter-measure/      # Eclipse feature for measure adapter
├── feature-builder-jql/          # Eclipse feature for JQL builder
├── site/                         # P2 update site
└── pom.xml                       # Root POM (aggregator)
```

## Core Modules

### Metamodel & Runtime

| Module | Type | Purpose |
|--------|------|---------|
| `model/` | eclipse-plugin | Core EMF metamodel defined in `expression.ecore`. Contains 12 subpackages (binding, collection, constant, custom, enumeration, logical, numeric, object, operator, string, temporal, variable) plus runtime utilities for evaluation, validation, and model loading. Generated code in `src-gen/`. |
| `adapter-measure/` | eclipse-plugin | `MeasureAdapter` — resolves measures and units for numeric expressions, calculates dimensions (base measures with exponents), handles unit conversion, and supports temporal duration measures. Uses `MeasureProvider` interface and `MeasureChangedHandler` for cache synchronization. |
| `builder-jql/` | eclipse-plugin | `ExpressionTransformer` — converts JQL expression ASTs into Expression metamodel instances. Contains 55+ function transformers organized by category (collection, temporal, string, numeric, object, variable). Uses `ExpressionBuildingVariableResolver` for context and `ExpressionMeasureProvider` for measure resolution. |

### Testing

| Module | Type | Purpose |
|--------|------|---------|
| `model-test/` | jar | JUnit 5 tests: `ExpressionValidatorTest`, `MeasureAdapterTest`, `MeasureChangeAdapterTest`. Uses `MinimalExpressionFactory` for test instance creation and `DummyMeasureProvider` for measure testing. |
| `osgi-itest/` | jar | OSGi integration tests using PAX Exam on Apache Karaf 4.4.7. `ExpressionBundleITest` verifies bundle activation and service registration. |

### OSGi & Packaging

| Module | Type | Purpose |
|--------|------|---------|
| `osgi/` | bundle | Standalone OSGi bundle. `ExpressionModelBundleTracker` dynamically loads expression models from bundles with `Expression-Models` header and registers them as OSGi services. |
| `feature-model/` | eclipse-feature | Eclipse feature for the core model plugin |
| `feature-adapter-measure/` | eclipse-feature | Eclipse feature for the measure adapter plugin |
| `feature-builder-jql/` | eclipse-feature | Eclipse feature for the JQL builder plugin |
| `site/` | eclipse-repository | P2 update site aggregating all features |

## Technology Stack

### Core Technologies
- **Eclipse EMF** (2.21+) — metamodel definition and code generation from Ecore
- **Eclipse Tycho** (4.0.13) — Maven integration for Eclipse plugin builds
- **Epsilon Runtime** (2.8.0) — EVL (Epsilon Validation Language) for model constraint validation
- **Xtext** (2.39.0) — DSL processing support
- **MWE2** (2.13.0) — Modeling Workflow Engine for code generation
- **OSGi** (Apache Felix) — modular runtime framework
- **Guava** (30.0-jre) — caching (`LoadingCache` in ExpressionUtils) and collections

### Build & Quality
- **Maven** 3.9.9+ with Maven Wrapper (`mvnw`)
- **JUnit 5** (Jupiter) — unit testing
- **Mockito** — mocking in tests
- **PAX Exam** (4.13.5) + **Apache Karaf** (4.4.7) — OSGi integration testing
- **JaCoCo** — code coverage
- **SonarQube** — code quality analysis (sonar.judo.technology)
- **Logback** (1.5.12) + **SLF4J** (2.0.16) — logging
- **Lombok** (1.18.34) — used only in non-Tycho modules

### External Metamodel Dependencies
- **judo-meta-measure** — measure/unit metamodel (used by adapter-measure)
- **judo-meta-jql** — JQL query language metamodel (used by builder-jql)

## Build Commands

```bash
# Full build (requires JDK 21, Maven 3.9.9+)
mvn clean install

# Run unit tests only
mvn test

# Run a single test class
mvn test -pl model-test -Dtest=ExpressionValidatorTest

# Full verification including OSGi integration tests
mvn verify

# Update site category versions
mvn clean install -P update-category-versions -f site/pom.xml
```

> **Note:** Maven Wrapper is available: `./mvnw clean install`

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Activates all submodules (default when `skipModules` is not set) |
| `sign-artifacts` | GPG signs artifacts for release |
| `release-dummy` | Deploys to a dummy local repository |
| `release-judong` | Deploys to internal Nexus (nexus.judo.technology) |
| `release-central` | Deploys to Maven Central with Javadoc and source JARs |
| `generate-github-asciidoc-diagrams` | Generates diagram images for GitHub rendering |
| `update-source-code-license` | Updates license headers across source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Root aggregator POM — version properties, dependency management, profiles |
| `model/model/expression.ecore` | Core EMF metamodel definition (12 subpackages, 31+ EClasses) |
| `model/model/expression.genmodel` | EMF code generation model |
| `model/src/workflow/generateModel.mwe2` | MWE2 workflow driving EMF code generation |
| `model/META-INF/MANIFEST.MF` | OSGi bundle manifest for the core model plugin |
| `model/src/main/epsilon/validations/expression/*.evl` | 16 EVL validation constraint files |
| `logback-test.xml` | Shared test logging configuration |

## Development Environment

**Required:**
- Java 21 JDK (recommended: Zulu distribution via SdkMan)
- Maven 3.9.9+

**Recommended:**
- SdkMan for Java version management: `sdk use java 21.x.y-zulu`
- Eclipse IDE with: m2e, Epsilon, Modeling Tools, XTend, XText, MWE/MWE2

## Git Workflow

- **Main Branch:** `develop`
- **Production Branch:** `master`
- **Versioning:** `1.0.5-SNAPSHOT` (current), semantic versioning with GitFlow
- **Branch Naming:** `feature/JNG-XXX_summary`, `bugfix/JNG-XXX_summary`, `release/X.Y-betaN`, `hotfix/JNG-XXX_summary`
- **Commit Format:** Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`)
- **Requirement:** Every commit must reference a JIRA ticket (`JNG-XXX`)

## Important Notes

1. **Never edit `src-gen/` directories** — these contain EMF-generated code that is regenerated on every build.
2. **Do not modify build configuration** (`pom.xml`, `feature.xml`, `site.xml`, `MANIFEST.MF`) without explicit human approval.
3. **Tycho vs Maven dependencies** — Tycho plugin-to-plugin dependencies go in `MANIFEST.MF` (`Require-Bundle`), not in `pom.xml`. See the dependency rules in [CONTRIBUTING.md](CONTRIBUTING.md).
4. **Version duality** — Maven uses `-SNAPSHOT`, Eclipse uses `.qualifier`. Tycho reconciles these automatically during the build.
5. **Lombok is not used in Eclipse plugins** — Tycho does not support Lombok generation. All Eclipse plugin source is generated or hand-written.
6. **Expression metamodel packages**: `binding`, `collection`, `constant`, `custom`, `enumeration`, `logical`, `numeric`, `object`, `operator`, `string`, `temporal`, `variable` — each in its own EMF subpackage under `hu.blackbelt.judo.meta.expression`.
7. **ModelAdapter** is the key abstraction for metamodel independence — it has 12 type parameters and is implemented by downstream adapters to connect expressions to specific model types.

## Related Documentation

- [README.md](README.md) — Project overview and quick start
- [CONTRIBUTING.md](CONTRIBUTING.md) — Contribution guidelines, code style, and build verification
- [.github/CIFLOW.md](.github/CIFLOW.md) — CI/CD pipeline and branching strategy
