# OpenSpec Project Configuration: judo-meta-expression

## Project Overview

**Repository:** BlackBeltTechnology/judo-meta-expression  
**License:** Eclipse Public License 2.0 (EPL-2.0)  
**Java Version:** 21  
**Build System:** Maven 3.9.4+ with Tycho

This is an Eclipse/Tycho-based metamodel project that defines the Expression metamodel for JUDO. It provides expression language support for data transformations, queries, and computed values.

## Directory Structure

```
judo-meta-expression/
├── model/                          # Core Expression metamodel (Ecore)
├── model-test/                     # Unit tests for metamodel
├── adapter-measure/                # Measure adapter
├── builder-jql/                    # JQL builder
├── osgi/                           # OSGi bundle repackaging
├── osgi-itest/                     # OSGi integration tests
├── feature-model/                  # Eclipse feature (model)
├── feature-builder-jql/            # Eclipse feature (JQL builder)
├── feature-adapter-measure/        # Eclipse feature (measure adapter)
├── site/                           # Eclipse P2 update site
├── docs/                           # Documentation
│   └── validation/                 # Validation documentation
└── openspec/                       # OpenSpec change management
```

## Technology Stack

- **Eclipse Modeling Framework (EMF)** - Metamodel foundation
- **Ecore** - Model definition language
- **Epsilon** - Model validation (EVL)
- **Tycho** - Eclipse plugin build
- **Zeta** - Java validation framework (to be added)

## Build Commands

```bash
# Standard build
mvn clean install

# Run tests
mvn test -pl model-test
```

## Validation

### Current State (EVL Only)
- EVL validation rules in `model/src/main/epsilon/validations/`
- Main entry: `expression.evl` and `expression-plugin-validation.evl`

### Target State (Dual Validation)
- EVL validation (unchanged)
- Java validation using Zeta framework
- Parameterized tests for parity

## Coding Conventions

### Validation Rules
1. All constraint names must be defined as constants
2. Guard methods should be named descriptively
3. Error messages must match EVL messages exactly
4. Use `@Satisfies` for constraint dependencies

### Testing
1. Use parameterized tests for dual validation
2. Test models should be minimal but complete
3. Include both positive and negative test cases

## OpenSpec Conventions

### Change IDs
- Use verb-led IDs: `add-zeta-validation`, `fix-type-check`, etc.
- Keep IDs concise but descriptive

### Spec Organization
- One spec per major capability
- Group related requirements together
- Include scenarios for each requirement

## Related Projects

- **judo-meta-esm** - Reference for Zeta integration patterns
- **judo-zeta** - Zeta validation framework source
- **judo-meta-measure** - Measure model dependency
- **judo-meta-jql** - JQL model dependency
