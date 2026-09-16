# Design: Zeta Validation Framework Integration

## Architecture Overview

This document describes the architectural design for integrating the Zeta validation framework into judo-meta-expression, following the established patterns from judo-meta-esm.

## Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Expression Model                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────────┐        ┌──────────────────────┐                   │
│  │   EVL Validation     │        │   Java Validation    │                   │
│  │   (Epsilon)          │        │   (Zeta Framework)   │                   │
│  ├──────────────────────┤        ├──────────────────────┤                   │
│  │ expression.evl       │   ≡    │ ExpressionValidator  │                   │
│  │ numeric.evl          │   ≡    │ NumericValidations   │                   │
│  │ object.evl           │   ≡    │ ObjectValidations    │                   │
│  │ collection.evl       │   ≡    │ CollectionValidations│                   │
│  │ logical.evl          │   ≡    │ LogicalValidations   │                   │
│  │ string.evl           │   ≡    │ StringValidations    │                   │
│  │ ...                  │   ≡    │ ...                  │                   │
│  └──────────────────────┘        └──────────────────────┘                   │
│           │                               │                                  │
│           └───────────┬───────────────────┘                                  │
│                       ▼                                                      │
│           ┌──────────────────────┐                                          │
│           │  Parameterized Tests │                                          │
│           │  (EVL + Java parity) │                                          │
│           └──────────────────────┘                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
                        ┌──────────────────────────┐
                        │   Zeta Framework         │
                        │   (judo-zeta)            │
                        ├──────────────────────────┤
                        │ ValidationRegistry       │
                        │ ValidationExecutor       │
                        │ ValidationContext        │
                        │ @Constraint, @Critique   │
                        │ @Guard, @Satisfies       │
                        └──────────────────────────┘
```

## Key Design Decisions

### 1. Dual Validation Mode

Both EVL and Java validation will run in parallel. This ensures:
- Backward compatibility with existing tooling
- Validation of Java implementation against EVL reference
- Gradual migration path if EVL is deprecated in future

### 2. Constraint Name Constants

All constraint names will be defined as constants in `ValidationConstants.java`:

```java
public final class ValidationConstants {
    
    // Numeric constraints
    public static final String RESOLVED = "Resolved";
    public static final String ATTRIBUTE_TYPE_IS_INTEGER = "AttributeTypeIsInteger";
    public static final String ATTRIBUTE_TYPE_IS_DECIMAL = "AttributeTypeIsDecimal";
    public static final String TYPE_OF_DEFAULT_CASE_IS_NUMERIC = "TypeOfDefaultCaseIsNumeric";
    
    // Numeric critiques
    public static final String INTEGER_ARITHMETIC_EXPRESSION_IS_RECOMMENDED = 
        "IntegerArithmeticExpressionIsRecommended";
    
    // Object constraints
    public static final String TARGET_IS_COLLECTION = "TargetIsCollection";
    public static final String OBJECT_SELECTOR_OPERATOR_IS_VALID = "ObjectSelectorOperatorIsValid";
    public static final String CASTED_TYPE_IS_COMPATIBLE = "CastedTypeIsCompatible";
    
    // ... more constants
    
    private ValidationConstants() {} // Prevent instantiation
}
```

### 3. Model Adapter Integration

The Expression model uses a `ModelAdapter` for type resolution and other operations. The Java validation will integrate with this through the ValidationContext:

```java
@ValidationContext(TypeName.class)
public class TypeNameValidations {
    
    @Constraint(name = ValidationConstants.OBJECT_TYPE_IS_VALID)
    public ValidationRule objectTypeIsValid() {
        return (element, ctx) -> {
            TypeName self = (TypeName) element;
            ModelAdapter modelAdapter = ctx.get("modelAdapter", ModelAdapter.class);
            
            if (modelAdapter.get(self).isPresent()) {
                return ValidationResult.pass();
            }
            return ValidationResult.fail(
                "Element named " + self.getName() + 
                " not found in namespace " + self.getNamespace()
            );
        };
    }
}
```

### 4. Guard Implementation Pattern

EVL guards will be implemented using the `@Guard` annotation:

```java
// EVL:
// constraint Resolved {
//     guard: self.objectExpression.satisfiesAll("Resolved")
//     check: ...
// }

// Java:
@Constraint(name = ValidationConstants.RESOLVED)
@Guard(method = "objectExpressionResolved")
public ValidationRule resolved() {
    return (element, ctx) -> {
        // validation logic
    };
}

public boolean objectExpressionResolved(EObject element, ValidationContext ctx) {
    ObjectNavigationExpression self = (ObjectNavigationExpression) element;
    return ctx.satisfiesAll(self.getObjectExpression(), ValidationConstants.RESOLVED);
}
```

### 5. Satisfies Dependencies

The `@Satisfies` annotation handles constraint dependencies:

```java
// EVL:
// constraint AttributeTypeIsInteger {
//     guard: self.satisfiesAll("Resolved")
//     check: modelAdapter.isNumeric(self.getAttributeType(modelAdapter).get())
// }

// Java:
@Constraint(name = ValidationConstants.ATTRIBUTE_TYPE_IS_INTEGER)
@Satisfies(ValidationConstants.RESOLVED)
public ValidationRule attributeTypeIsInteger() {
    return (element, ctx) -> {
        IntegerAttribute self = (IntegerAttribute) element;
        ModelAdapter modelAdapter = ctx.get("modelAdapter", ModelAdapter.class);
        
        Optional<Object> attrType = self.getAttributeType(modelAdapter);
        if (attrType.isPresent() && modelAdapter.isNumeric(attrType.get())) {
            return ValidationResult.pass();
        }
        return ValidationResult.fail(
            "Attribute type of " + self.getAttributeName() + 
            " of object type " + self.getObjectExpression().getObjectType(modelAdapter).getName() + 
            " is not numeric"
        );
    };
}
```

### 6. Critique Implementation

Critiques (warnings) use the `@Critique` annotation:

```java
@Critique(name = ValidationConstants.INTEGER_ARITHMETIC_EXPRESSION_IS_RECOMMENDED)
@Satisfies(ValidationConstants.RESOLVED)
public ValidationRule integerArithmeticExpressionIsRecommended() {
    return (element, ctx) -> {
        DecimalArithmeticExpression self = (DecimalArithmeticExpression) element;
        
        boolean bothInteger = self.getLeft() instanceof IntegerExpression 
            && self.getRight() instanceof IntegerExpression;
        boolean applicableOperator = 
            self.getOperator() == DecimalOperator.ADD 
            || self.getOperator() == DecimalOperator.SUBSTRACT 
            || self.getOperator() == DecimalOperator.MULTIPLY;
        
        if (bothInteger && applicableOperator) {
            return ValidationResult.warn(
                "Both arguments are integer so integer arithmetic expression is recommended in: " + self
            );
        }
        return ValidationResult.pass();
    };
}
```

## Test Architecture

### Parameterized Test Pattern

Following the ESM pattern, tests will be parameterized to run both validators:

```java
public abstract class AbstractExpressionValidationTest {
    
    protected ExpressionModel expressionModel;
    protected ValidatorType validatorType;
    
    protected void runValidation(
        Collection<String> expectedErrors,
        Collection<String> expectedWarnings
    ) throws Exception {
        switch (validatorType) {
            case EVL:
                runEvlValidation(expectedErrors, expectedWarnings);
                break;
            case JAVA:
                ExpressionValidator.validate(
                    log, expressionModel, expectedErrors, expectedWarnings, false
                );
                break;
        }
    }
}
```

### Test Example

```java
public class ExpressionValidationNumericTest extends AbstractExpressionValidationTest {
    
    @ParameterizedTest(name = "testAttributeTypeIsInteger [{0}]")
    @EnumSource(ValidatorType.class)
    void testAttributeTypeIsInteger(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();
        
        // Create model with non-integer attribute used as IntegerAttribute
        // ...
        
        runValidation(
            List.of(ValidationConstants.ATTRIBUTE_TYPE_IS_INTEGER),
            Collections.emptyList()
        );
    }
}
```

### Performance Test Design

```java
@Slf4j
public class ExpressionValidationPerformanceTest {
    
    private static final int ELEMENT_COUNT = 10_000;
    private static final int WARMUP_ITERATIONS = 2;
    private static final int BENCHMARK_ITERATIONS = 5;
    
    @Test
    void benchmarkEvlVsJavaValidation() throws Exception {
        // Generate large model
        ExpressionModel model = generateLargeModel(ELEMENT_COUNT);
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            runEvlValidation(model);
            runJavaValidation(model, false);
            runJavaValidation(model, true);
        }
        
        // Benchmark EVL
        List<Long> evlTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            runEvlValidation(model);
            evlTimes.add(System.currentTimeMillis() - start);
        }
        
        // Benchmark Java Sequential
        List<Long> javaSeqTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            runJavaValidation(model, false);
            javaSeqTimes.add(System.currentTimeMillis() - start);
        }
        
        // Benchmark Java Parallel
        List<Long> javaParTimes = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.currentTimeMillis();
            runJavaValidation(model, true);
            javaParTimes.add(System.currentTimeMillis() - start);
        }
        
        // Report results
        log.info("EVL avg: {} ms", average(evlTimes));
        log.info("Java Sequential avg: {} ms", average(javaSeqTimes));
        log.info("Java Parallel avg: {} ms", average(javaParTimes));
        log.info("Speedup (Java Seq vs EVL): {}x", average(evlTimes) / average(javaSeqTimes));
        log.info("Speedup (Java Par vs EVL): {}x", average(evlTimes) / average(javaParTimes));
    }
}
```

## File Organization

### model/pom.xml Changes

```xml
<properties>
    <judo-zeta-version>1.0.0.20251207_081454_0779b890_develop</judo-zeta-version>
</properties>

<dependencies>
    <dependency>
        <groupId>hu.blackbelt.judo.zeta</groupId>
        <artifactId>judo-zeta-validation-core</artifactId>
        <version>${judo-zeta-version}</version>
    </dependency>
    <dependency>
        <groupId>hu.blackbelt.judo.zeta</groupId>
        <artifactId>judo-zeta-annotations</artifactId>
        <version>${judo-zeta-version}</version>
    </dependency>
</dependencies>
```

### Package Structure

```
hu.blackbelt.judo.meta.expression.validation
├── ExpressionValidator.java
├── ExpressionValidationException.java
├── constants
│   └── ValidationConstants.java
└── rules
    ├── expression
    │   ├── TypeNameValidations.java
    │   └── ExpressionValidations.java
    ├── numeric
    │   ├── CountExpressionValidations.java
    │   ├── IntegerArithmeticExpressionValidations.java
    │   ├── DecimalArithmeticExpressionValidations.java
    │   └── ...
    ├── object
    │   ├── ObjectNavigationExpressionValidations.java
    │   ├── CastObjectValidations.java
    │   └── ...
    ├── collection
    │   ├── CollectionNavigationValidations.java
    │   ├── CastCollectionValidations.java
    │   └── ...
    ├── logical
    │   ├── ComparisonValidations.java
    │   ├── InstanceOfExpressionValidations.java
    │   └── ...
    ├── string
    │   ├── StringAttributeValidations.java
    │   ├── StringOperationValidations.java
    │   └── ...
    ├── temporal
    │   └── TemporalExpressionValidations.java
    ├── measured
    │   └── MeasuredExpressionValidations.java
    ├── enumeration
    │   └── EnumerationExpressionValidations.java
    ├── constant
    │   └── ConstantExpressionValidations.java
    ├── custom
    │   └── CustomExpressionValidations.java
    └── binding
        ├── AttributeBindingValidations.java
        ├── ReferenceBindingValidations.java
        └── FilterBindingValidations.java
```

## Error Message Consistency

All error messages must match the EVL messages exactly for test parity:

| EVL Message Pattern | Java Implementation |
|---------------------|---------------------|
| `"Element named " + self.name + " not found in namespace " + self.namespace` | Same string concatenation |
| `"Attribute type of " + self.attributeName + " of object type " + self.objectExpression.getObjectType(modelAdapter).getName() + " is not numeric"` | Same pattern |

## Extension Methods

Complex EVL operations like `satisfiesAll`, `getObjectType`, etc. will be implemented as extension methods:

```java
public class ExpressionExtensions {
    
    @ExtensionMethod
    public static Optional<Object> getObjectType(ObjectExpression expr, ModelAdapter adapter) {
        // Implementation matching EVL behavior
    }
    
    @ExtensionMethod
    public static Optional<Object> getAttributeType(AttributeSelector expr, ModelAdapter adapter) {
        // Implementation matching EVL behavior
    }
}
```

## Migration Considerations

1. **No breaking changes** - EVL validation continues to work unchanged
2. **Gradual adoption** - Java validation can be enabled/disabled per use case
3. **Test coverage** - Parameterized tests ensure parity between implementations
4. **Documentation** - Clear docs on when to use each approach
