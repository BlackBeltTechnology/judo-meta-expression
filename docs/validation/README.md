# Expression Validation

Expression models are validated using two complementary validation frameworks:

## Validation Frameworks

### 1. EVL (Epsilon Validation Language)

The original validation implementation using Epsilon's EVL language.

- **Location:** `model/src/main/epsilon/validations/expression/`
- **Documentation:** See [Epsilon EVL documentation](https://eclipse.dev/epsilon/doc/evl/) for EVL syntax and usage

### 2. Java Validation Framework (Zeta)

A native Java validation implementation providing better IDE support, debugging, and performance.

- **Location:** `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/`
- **Documentation:** See [java-validation-framework.md](java-validation-framework.md)
- **Zeta Framework:** See [judo-zeta repository](https://github.com/BlackBeltTechnology/judo-zeta)

## Comparison

| Feature | EVL | Java (Zeta) |
|---------|-----|-------------|
| IDE Support | Limited | Full (code completion, refactoring) |
| Debugging | EVL debugger | Standard Java debugger |
| Performance | Baseline | Expected ~5-10x faster |
| Type Safety | Runtime | Compile-time |
| Test Integration | Epsilon runtime | Standard JUnit |

## Validation Categories

Both frameworks implement validations for all Expression domains:

| Category | EVL Files | Java Package |
|----------|-----------|--------------|
| Expression | `expression.evl` | `rules.expression` |
| Numeric | `numeric.evl` | `rules.numeric` |
| Object | `object.evl` | `rules.object` |
| Collection | `collection.evl` | `rules.collection` |
| Logical | `logical.evl` | `rules.logical` |
| String | `string.evl` | `rules.string` |
| Attribute | `attribute.evl` | `rules.attribute` |
| Temporal | `temporal.evl` | `rules.temporal` |
| Measured | `measured.evl` | `rules.measured` |
| Enumeration | `enumeration.evl` | `rules.enumeration` |
| Constant | - | `rules.constant` |
| Custom | - | `rules.custom` |
| Binding | `Binding.evl` | `rules.binding` |

## Usage

### EVL Validation

```java
import hu.blackbelt.judo.meta.expression.runtime.ExpressionValidator;

ExpressionValidator.validateExpression(log, expressionModel, modelAdapter,
    adaptedName, adapted, measureName, measure,
    expectedErrors, expectedWarnings);
```

### Java (Zeta) Validation

```java
import hu.blackbelt.judo.meta.expression.validation.ExpressionZetaValidator;

// Simple validation
ExpressionZetaValidator.validateExpression(log, expressionModel, modelAdapter);

// Validation with expected errors/warnings
ExpressionZetaValidator.validateExpression(log, expressionModel, modelAdapter,
    expectedErrors, expectedWarnings, parallel);
```

## Testing

Both validation frameworks have comprehensive test coverage. See [tests.md](tests.md) for complete test documentation.

### Test Infrastructure

| Class | Description |
|-------|-------------|
| `ValidatorType` | Enum for selecting EVL or JAVA validation |
| `AbstractExpressionValidationTest` | Base class for parameterized tests |
| `ExpressionValidationPerformanceTest` | Performance benchmark test |

### Running Tests

```bash
# Run all validation tests
mvn test -pl model-test

# Run parameterized tests (both EVL and Java)
mvn test -pl model-test -Dtest=*ValidationTest

# Run performance tests
mvn test -pl model-test -Dtest=ExpressionValidationPerformanceTest
```

## Related Documentation

### Expression-Specific
- [Java Validation Framework](java-validation-framework.md) - Expression-specific Java validation documentation
- [Validation Tests](tests.md) - Complete test documentation

### Zeta Framework Documentation
The Java validation framework is built on the [Judo Zeta Framework](https://github.com/BlackBeltTechnology/judo-zeta). Complete documentation is available in the Zeta repository:

- **[Getting Started](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/getting-started.md)** - Install and write your first validation rule
- **[User Guide](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/user-guide/)** - Core concepts, rules, guards, caching
- **[EVL Comparison](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/evl-comparison/)** - Migration from Epsilon EVL
- **[Best Practices](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/best-practices/)** - Production patterns
- **[Examples](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/examples/)** - Working examples
- **[Reference](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/reference/)** - API reference

### External References
- [Epsilon EVL Reference](https://eclipse.dev/epsilon/doc/evl/) - Epsilon Validation Language reference
- [Zeta Framework Repository](https://github.com/BlackBeltTechnology/judo-zeta) - Source code and issues
