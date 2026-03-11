
# Judo Expression Meta - Project Documentation

## Project Overview

**Repository:** BlackBeltTechnology/judo-meta-expression
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.9.4+ with Tycho (Eclipse build tooling)

This is an Eclipse/Tycho-based metamodel project that:
1. **Defines** an Expression metamodel via EMF/Ecore for representing typed expressions
2. **Generates** Java code from the model using MWE2 workflows
3. **Provides** both Eclipse plugin and OSGi standalone runtime
4. **Implements** validation rules using both Epsilon (EVL) and native Java (Zeta framework)
5. **Includes** adapters for measures and JQL (Judo Query Language) expression building

## Directory Structure

```
judo-meta-expression/
├── model/                          # Core Expression metamodel (Ecore)
├── model-test/                     # Unit tests for metamodel and validation
├── adapter-measure/                # Measure adapter for unit handling
├── builder-jql/                    # JQL expression builder
├── osgi/                           # OSGi bundle repackaging
├── osgi-itest/                     # OSGi integration tests (Pax Exam)
├── feature-model/                  # Eclipse feature (model)
├── feature-adapter-measure/        # Eclipse feature (measure adapter)
├── feature-builder-jql/            # Eclipse feature (JQL builder)
├── site/                           # Eclipse P2 update site
├── docs/                           # Documentation
│   └── validation/                 # Validation framework docs
└── openspec/                       # OpenSpec change management
```

## Core Modules

### Model Definition Layer

| Module | Type | Purpose |
|--------|------|---------|
| `model/` | eclipse-plugin | Core Expression metamodel via Ecore (`expression.ecore`). Generates EMF code, builders, helpers. Contains EVL and Java validation rules. |
| `model-test/` | test | Unit tests for Expression metamodel using JUnit 5, includes mock-based validation tests |

### Adapters Layer

| Module | Type | Purpose |
|--------|------|---------|
| `adapter-measure/` | bundle | Measure adapter for handling units of measurement in expressions |
| `builder-jql/` | bundle | JQL (Judo Query Language) expression builder for constructing expressions programmatically |

### Runtime/OSGi Layer

| Module | Type | Purpose |
|--------|------|---------|
| `osgi/` | bundle | Repackages model for OSGi environments using Apache Felix Bundle Plugin |
| `osgi-itest/` | test | Pax Exam integration tests for Karaf container |

### Distribution Layer

| Module | Type | Purpose |
|--------|------|---------|
| `feature-model/` | eclipse-feature | Bundles core model |
| `feature-adapter-measure/` | eclipse-feature | Bundles measure adapter |
| `feature-builder-jql/` | eclipse-feature | Bundles JQL builder |
| `site/` | eclipse-repository | P2 update site for Eclipse distribution |

## Expression Metamodel Structure

The core metamodel (`model/model/expression.ecore`) defines these packages:

| Package | Purpose |
|---------|---------|
| `expression` | Core expression types, TypeName, AttributeSelector, ModelAdapter |
| `constant` | Constant expressions (literals, instances) |
| `variable` | Variable references and declarations |
| `operator` | Operators for expressions |
| `numeric` | Numeric expressions (integer, decimal, arithmetic) |
| `logical` | Logical/boolean expressions |
| `string` | String expressions and operations |
| `enumeration` | Enumeration expressions |
| `object` | Object expressions and navigation |
| `collection` | Collection expressions |
| `custom` | Custom type expressions |
| `temporal` | Temporal expressions (date, time, timestamp) |
| `binding` | Attribute and reference bindings |

## Validation Framework

The project supports two validation implementations:

### EVL (Epsilon Validation Language)
- Located in `model/src/main/epsilon/validations/expression/`
- Files: `attribute.evl`, `attributeBinding.evl`, `collection.evl`, `constant.evl`, `custom.evl`, `enumeration.evl`, `logical.evl`, `measured.evl`, `numeric.evl`, `object.evl`, `string.evl`, `temporal.evl`, etc.
- Uses Epsilon runtime for model validation

### Java Validation (Zeta Framework)
- Located in `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/`
- Entry point: `ExpressionZetaValidator.java`
- Validation rules in `rules/` subdirectory organized by expression type
- Constants in `constants/ValidationConstants.java`
- Native Java alternative with better IDE support, debugging, and performance

### Validation Documentation
- `docs/validation/README.md` - Validation overview
- `docs/validation/java-validation-framework.md` - Java validation documentation
- `docs/validation/tests.md` - Testing documentation

## Technology Stack

### Core Technologies
- **Eclipse Modeling Framework (EMF)** - Metamodel foundation
- **Ecore** - Model definition language
- **MWE2** (Model Workflow Engine) - Code generation workflows
- **Epsilon** 2.8.0 - Model validation (EVL)
- **Tycho** - Eclipse plugin build

### Validation Technologies
- **Zeta Validation Framework** - Java-based validation with annotations
- **EVL (Epsilon)** - Declarative validation rules

### Runtime
- **Apache Karaf** - OSGi container
- **Apache Felix** - OSGi bundle plugin
- **Pax Exam** - OSGi testing

### Build & Quality
- **Maven** 3.9.4+ with wrapper
- **JaCoCo** - Code coverage
- **JUnit 5** - Testing framework
- **Lombok** - Annotation processing

## Build Commands

```bash
# Standard build
mvn clean install
# or with wrapper
./mvnw clean install

# Build without tests
./mvnw clean install -DskipTests

# Run specific test class
./mvnw test -pl model-test -Dtest="ClassName"
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Includes all submodules (default) |
| `sign-artifacts` | GPG signing for release |
| `release-central` | Maven Central deployment |
| `release-judong` | Internal Judo repository |

## Code Generation Flow

1. **MWE2 Workflow** (`model/src/workflow/generateModel.mwe2`)
   - Generates EMF code from `expression.ecore`
   - Produces GenModel-based Java classes
   - Generates builders and helpers

2. **Model Compilation**
   - Tycho compiles eclipse-plugin modules
   - OSGi bundle compilation with Felix

3. **Feature/Site Building**
   - P2 metadata generation
   - Feature packaging
   - Update site assembly

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM with module definitions and plugin management |
| `.mvn/jvm.config` | JVM arguments for Maven build |
| `model/model/expression.ecore` | Core metamodel definition |
| `model/model/expression.genmodel` | EMF code generation model |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.9.4+
- Eclipse IDE with:
  - m2e (Maven integration)
  - Epsilon plugin
  - EMF/Ecore modeling tools

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** SNAPSHOT-based development (currently 1.0.5-SNAPSHOT)
- **Version Placeholder:** `$VERSION_PLACEHOLDER$` in model metadata
- **Release Process:** CI/CD via GitHub Actions

## Important Notes

1. **Understand EMF/Ecore patterns** before modifying model code
2. **Respect Tycho build constraints** when modifying Eclipse plugins
3. **Validation rules** - Two implementations available:
   - **EVL (Epsilon):** Located in `model/src/main/epsilon/validations/expression/`
   - **Java (Zeta):** Located in `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/`
   - See `docs/validation/java-validation-framework.md` for Java framework documentation
4. **ModelAdapter pattern** - Expression metamodel uses adapters for type resolution
5. **Use OpenSpec for significant changes** - See `openspec/AGENTS.md` for proposal workflow

## Key Classes

| Class | Purpose |
|-------|---------|
| `ModelAdapter` | Interface for type resolution and model navigation |
| `ExpressionZetaValidator` | Main entry point for Java validation |
| `ExpressionValidationContext` | Validation context with ModelAdapter access |
| `ValidationConstants` | Constraint name constants |

## Related Documentation

- `README.adoc` - Project overview
- `AGENTS.md` - Project documentation for AI assistants
- `openspec/AGENTS.md` - OpenSpec workflow for spec-driven development
- `docs/validation/README.md` - Validation rules overview
- `docs/validation/java-validation-framework.md` - Java validation documentation
- `docs/validation/tests.md` - Testing documentation
