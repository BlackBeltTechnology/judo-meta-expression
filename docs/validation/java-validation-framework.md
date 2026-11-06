# Expression Java Validation Framework

The Expression Java Validation Framework provides native Java-based validation for Expression models using the Zeta validation framework. It offers an alternative to EVL validation with better IDE support, debugging capabilities, and improved performance.

## Overview

The framework translates all EVL validation rules to Java, providing:
- Full IDE support (code completion, refactoring, navigation)
- Standard Java debugging
- Expected ~5-10x performance improvement over EVL
- Type-safe validation rule development
- Easy unit testing with JUnit

## Architecture

```
model/src/main/java/hu/blackbelt/judo/meta/expression/validation/
├── ExpressionZetaValidator.java     # Main entry point
├── ExpressionValidationContext.java # Custom validation context
├── constants/
│   └── ValidationConstants.java     # All constraint name constants
└── rules/
    ├── expression/                  # Expression core validations
    │   ├── TypeNameValidations.java
    │   └── ExpressionValidations.java
    ├── numeric/                     # Numeric validations
    │   └── NumericExpressionValidations.java
    ├── object/                      # Object expression validations
    │   └── ObjectExpressionValidations.java
    ├── collection/                  # Collection validations
    │   └── CollectionExpressionValidations.java
    ├── logical/                     # Logical validations
    │   └── LogicalExpressionValidations.java
    ├── string/                      # String validations
    │   └── StringExpressionValidations.java
    ├── attribute/                   # Attribute validations
    │   └── AttributeValidations.java
    ├── temporal/                    # Temporal validations
    │   └── TemporalExpressionValidations.java
    ├── measured/                    # Measured validations
    │   └── MeasuredExpressionValidations.java
    ├── enumeration/                 # Enumeration validations
    │   └── EnumerationExpressionValidations.java
    ├── constant/                    # Constant validations
    │   └── ConstantExpressionValidations.java
    ├── custom/                      # Custom validations
    │   └── CustomExpressionValidations.java
    └── binding/                     # Binding validations
        ├── AttributeBindingValidations.java
        ├── ReferenceBindingValidations.java
        └── FilterBindingValidations.java
```

## Usage

### Basic Validation

```java
import hu.blackbelt.judo.meta.expression.validation.ExpressionZetaValidator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;

// Validate model
ExpressionZetaValidator.validateExpression(log, expressionModel, modelAdapter);
```

### Validation with Expected Results

```java
import hu.blackbelt.judo.meta.expression.validation.ExpressionZetaValidator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionValidationException;

Collection<String> expectedErrors = Arrays.asList(
    "ObjectTypeIsValid"
);
Collection<String> expectedWarnings = Collections.emptyList();

try {
    ExpressionZetaValidator.validateExpression(
        log, 
        expressionModel, 
        modelAdapter,
        expectedErrors, 
        expectedWarnings,
        false  // sequential execution
    );
} catch (ExpressionValidationException e) {
    // Handle unexpected validation results
    log.error("Validation failed: {}", e.getMessage());
}
```

### Parallel Validation

```java
// Enable parallel validation for large models
ExpressionZetaValidator.validateExpression(
    log, 
    expressionModel, 
    modelAdapter,
    expectedErrors, 
    expectedWarnings,
    true  // parallel execution
);
```

## Validation Context

The `ExpressionValidationContext` provides access to Expression-specific resources:

```java
public class ExpressionValidationContext extends ValidationContext {
    
    // Get the model adapter for type resolution
    public ModelAdapter getModelAdapter();
    
    // Get the expression model being validated
    public ExpressionModel getExpressionModel();
    
    // Get the expression evaluator for lambda/scope checks
    public ExpressionEvaluator getEvaluator();
    
    // Check if an element satisfies a constraint
    public boolean satisfies(EObject element, String constraintName);
    
    // Mark a constraint as satisfied for an element
    public void markSatisfied(EObject element, String constraintName);
}
```

## Validation Constants

All constraint and critique names are defined as constants in `ValidationConstants.java`:

```java
import static hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants.*;

// Use in validation rules
@Constraint(name = ValidationConstants.OBJECT_TYPE_IS_VALID)
public ValidationRule objectTypeIsValid() {
    // ...
}
```

### Available Constants

| Category | Constant | Description |
|----------|----------|-------------|
| Core | `RESOLVED` | Generic resolved constraint |
| Core | `OBJECT_TYPE_IS_VALID` | Type name references valid type |
| Core | `LAMBDA_VARIABLE_IS_VALID` | Lambda variable reference is valid |
| Numeric | `ATTRIBUTE_TYPE_IS_INTEGER` | Attribute type is integer |
| Numeric | `ATTRIBUTE_TYPE_IS_DECIMAL` | Attribute type is decimal |
| Logical | `ATTRIBUTE_TYPE_IS_BOOLEAN` | Attribute type is boolean |
| String | `ATTRIBUTE_TYPE_IS_STRING` | Attribute type is string |
| Temporal | `ATTRIBUTE_TYPE_IS_DATE` | Attribute type is date |
| Temporal | `ATTRIBUTE_TYPE_IS_TIMESTAMP` | Attribute type is timestamp |
| Temporal | `ATTRIBUTE_TYPE_IS_TIME` | Attribute type is time |
| Object | `OBJECT_SELECTOR_OPERATOR_IS_VALID` | Only 'any' selector supported |
| Object | `CASTED_TYPE_IS_COMPATIBLE` | Cast type is compatible |
| Collection | `TARGET_IS_COLLECTION` | Navigation target is collection |
| Measured | `MEASURE_IS_VALID` | Measure is valid |
| Binding | `ATTRIBUTE_BINDING_IS_VALID` | Attribute binding is valid |
| Binding | `REFERENCE_BINDING_IS_VALID` | Reference binding is valid |
| Binding | `FILTER_BINDING_IS_VALID` | Filter binding is valid |

See `ValidationConstants.java` for the complete list.

## Zeta Annotations

Validation rules use the following Zeta annotations:

### @ValidationContext

Specifies the model element type the rules apply to:

```java
@ValidationContext(TypeName.class)
public class TypeNameValidations {
    // Rules for TypeName elements
}
```

### @Constraint

Defines a validation constraint (error on failure):

```java
@Constraint(name = ValidationConstants.OBJECT_TYPE_IS_VALID)
public ValidationRule objectTypeIsValid() {
    return (element, ctx) -> {
        TypeName self = (TypeName) element;
        ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
        ModelAdapter modelAdapter = exprCtx.getModelAdapter();
        
        if (self.get(modelAdapter) != null) {
            return ValidationResult.pass();
        }
        
        return ValidationResult.fail(
            "Element named " + self.getName() + 
            " not found in namespace " + self.getNamespace()
        );
    };
}
```

### @Critique

Defines a validation critique (warning on failure):

```java
@Critique(name = ValidationConstants.INTEGER_ARITHMETIC_EXPRESSION_IS_RECOMMENDED)
public ValidationRule integerArithmeticRecommended() {
    return (element, ctx) -> {
        DecimalArithmeticExpression self = (DecimalArithmeticExpression) element;
        // Check if both operands are integers
        if (bothOperandsAreIntegers(self)) {
            return ValidationResult.warn(
                "Consider using IntegerArithmeticExpression for better performance"
            );
        }
        return ValidationResult.pass();
    };
}
```

### @Guard

Defines a guard condition for a constraint:

```java
@Constraint(name = ValidationConstants.RESOLVED)
@Guard("isLambdaFunction")
public ValidationRule lambdaResolved() {
    return (element, ctx) -> {
        // Only runs if isLambdaFunction guard returns true
    };
}
```

### @Satisfies

Specifies dependencies on other constraints:

```java
@Constraint(name = "DetailedValidation")
@Satisfies(ValidationConstants.OBJECT_TYPE_IS_VALID)
public ValidationRule detailedValidation() {
    // This rule only runs if OBJECT_TYPE_IS_VALID passes
}
```

## Validation Classes

### Expression Core Validations

| Class | Constraints |
|-------|-------------|
| `TypeNameValidations` | ObjectTypeIsValid |
| `ExpressionValidations` | LambdaVariableIsValid |

### Numeric Validations

| Class | Constraints |
|-------|-------------|
| `NumericExpressionValidations` | Resolved, AttributeTypeIsInteger, AttributeTypeIsDecimal, TypeOfDefaultCaseIsNumeric, TypeOfSwitchCaseIsNumeric, IntegerArithmeticExpressionIsRecommended (critique) |

### Object Validations

| Class | Constraints |
|-------|-------------|
| `ObjectExpressionValidations` | Resolved, TargetIsCollection, ObjectSelectorOperatorIsValid, CastedTypeIsCompatible |

### Collection Validations

| Class | Constraints |
|-------|-------------|
| `CollectionExpressionValidations` | Resolved, TargetIsCollection, CastTypeIsCompatible, TypeIsDefined |

### Logical Validations

| Class | Constraints |
|-------|-------------|
| `LogicalExpressionValidations` | Resolved, ElementTypeIsCompatible, AttributeTypeIsBoolean, TypesAreCompatible, IntegerComparisonIsRecommended (critique) |

### String Validations

| Class | Constraints |
|-------|-------------|
| `StringExpressionValidations` | Resolved, AttributeTypeIsString, TypeOfDefaultCaseIsString, TypeOfSwitchCaseIsString |

### Attribute Validations

| Class | Constraints |
|-------|-------------|
| `AttributeValidations` | Resolved |

### Temporal Validations

| Class | Constraints |
|-------|-------------|
| `TemporalExpressionValidations` | DurationIsSystemUnit, AttributeTypeIsDate, AttributeTypeIsTimestamp, AttributeTypeIsTime, TypeOfDefaultCaseIsDate/Timestamp/Time, TypeOfSwitchCaseIsDate/Timestamp/Time |

### Measured Validations

| Class | Constraints |
|-------|-------------|
| `MeasuredExpressionValidations` | MeasureIsValid, MeasureOfAdditionIsValid, MeasureOfCompleteExpressionIsDefined, ComparisonIsMeasured, MeasuresAreMatching |

### Enumeration Validations

| Class | Constraints |
|-------|-------------|
| `EnumerationExpressionValidations` | AttributeTypeIsEnumeration, TypeOfDefaultCaseIsEnumeration, TypeOfSwitchCaseIsEnumeration |

### Binding Validations

| Class | Constraints |
|-------|-------------|
| `AttributeBindingValidations` | AttributeBindingIsValid, AttributeBindingExpressionIsValid, AttributeBindingTypeIsValid |
| `ReferenceBindingValidations` | ReferenceBindingIsValid, ReferenceBindingExpressionIsValid, ReferenceBindingTypeIsValid |
| `FilterBindingValidations` | FilterBindingIsValid, FilterBindingExpressionIsValid |

## Adding New Validation Rules

### Step 1: Create Validation Class

```java
package hu.blackbelt.judo.meta.expression.validation.rules.mypackage;

import hu.blackbelt.judo.meta.expression.MyElement;
import hu.blackbelt.judo.meta.expression.validation.ExpressionValidationContext;
import hu.blackbelt.judo.meta.expression.validation.constants.ValidationConstants;
import hu.blackbelt.judo.zeta.annotations.Constraint;
import hu.blackbelt.judo.zeta.annotations.ValidationContext;
import hu.blackbelt.judo.zeta.validation.api.ValidationResult;
import hu.blackbelt.judo.zeta.validation.api.ValidationRule;

@ValidationContext(MyElement.class)
public class MyElementValidations {

    @Constraint(name = ValidationConstants.MY_CONSTRAINT)
    public ValidationRule myConstraint() {
        return (element, ctx) -> {
            MyElement self = (MyElement) element;
            ExpressionValidationContext exprCtx = (ExpressionValidationContext) ctx;
            
            // Validation logic
            if (isValid(self, exprCtx.getModelAdapter())) {
                return ValidationResult.pass();
            }
            
            return ValidationResult.fail("Error message: " + self);
        };
    }
}
```

### Step 2: Add Constant

Add the constraint name to `ValidationConstants.java`:

```java
public static final String MY_CONSTRAINT = "MyConstraint";
```

### Step 3: Register in ExpressionZetaValidator

Add the class to `VALIDATOR_CLASSES` in `ExpressionZetaValidator.java`:

```java
private static final List<Class<?>> VALIDATOR_CLASSES = List.of(
    // ... existing classes ...
    MyElementValidations.class
);
```

### Step 4: Add Tests

Create parameterized test cases:

```java
@ParameterizedTest(name = "testMyConstraint [{0}]")
@EnumSource(ValidatorType.class)
void testMyConstraint(ValidatorType type) throws Exception {
    this.validatorType = type;
    initModel();
    
    // Create model that violates the constraint
    MyElement element = createInvalidElement();
    expressionModel.addContent(element);
    
    runValidation(
        ImmutableList.of(ValidationConstants.MY_CONSTRAINT),
        ImmutableList.of()
    );
}
```

## Related Documentation

### Expression-Specific
- [Validation Overview](README.md) - Overview of Expression validation
- [Validation Tests](tests.md) - Complete test documentation

### Zeta Framework Documentation
Complete Zeta framework documentation is available in the [judo-zeta repository](https://github.com/BlackBeltTechnology/judo-zeta):

- **[Getting Started](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/getting-started.md)** - Install and write your first validation rule
- **[Core Concepts](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/user-guide/core-concepts.md)** - Validation fundamentals
- **[Validation Rules](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/user-guide/validation-rules.md)** - Writing validation rules
- **[Guards and Dependencies](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/user-guide/guards-and-dependencies.md)** - Conditional validation
- **[Caching](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/user-guide/caching.md)** - Performance optimization
- **[EVL Migration Guide](https://github.com/BlackBeltTechnology/judo-zeta/blob/develop/docs/validation/evl-comparison/migration-guide.md)** - Migrating from EVL

### External References
- [Epsilon EVL Reference](https://eclipse.dev/epsilon/doc/evl/) - Epsilon Validation Language reference
- [Zeta Framework Repository](https://github.com/BlackBeltTechnology/judo-zeta) - Source code and issues
