# Add Zeta Validation Framework to Expression Model

## Summary

Integrate the Judo Zeta validation framework into judo-meta-expression to provide native Java-based validation alongside the existing EVL (Epsilon Validation Language) validation. This enables dual validation mode where both EVL and Java validators run in parallel, ensuring consistency while providing better IDE support, debugging capabilities, and improved performance.

## Motivation

The current Expression model uses EVL for validation, which has several limitations:
- Limited IDE support (no code completion, refactoring)
- Difficult debugging (requires EVL debugger)
- Slower execution compared to native Java
- Runtime type checking only

The Zeta framework, already successfully integrated in judo-meta-esm, provides:
- Full IDE support with code completion and refactoring
- Standard Java debugging
- ~7.5x performance improvement over EVL (based on ESM benchmarks)
- Compile-time type safety
- Easy unit testing with JUnit

## Scope

### In Scope
1. Add Zeta validation framework dependency to pom.xml
2. Implement Java validators for all existing EVL validation rules
3. Create parameterized tests that run both EVL and Java validation
4. Add performance benchmark tests (10,000 elements)
5. Update documentation to reference Zeta framework
6. Use constants for all constraint names, guard methods, and message templates

### Out of Scope
- Removing EVL validation (dual mode will be maintained)
- Changing the metamodel structure
- Adding new validation rules beyond existing EVL rules

## EVL Rules to Convert

Based on analysis of `model/src/main/epsilon/validations/expression/`:

| EVL File | Constraints | Critiques |
|----------|-------------|-----------|
| expression.evl | ObjectTypeIsValid, LambdaVariableIsValid | - |
| numeric.evl | Resolved (multiple), AttributeTypeIsInteger, AttributeTypeIsDecimal, TypeOfDefaultCaseIsNumeric | IntegerArithmeticExpressionIsRecommended, AttributeTypeIsDecimalOrOperationIsNotSupportedOnIntegers |
| object.evl | Resolved (multiple), TargetIsCollection, ObjectSelectorOperatorIsValid, TypeIsDefined, CastedTypeIsCompatible | - |
| collection.evl | Resolved (multiple), TargetIsCollection, CastTypeIsCompatible, TypeIsDefined | - |
| logical.evl | Resolved (multiple), AttributeTypeIsBoolean, ElementTypeIsCompatible, TypesAreCompatible | IntegerComparisonIsRecommended |
| string.evl | Resolved (multiple), AttributeTypeIsString, TypeOfDefaultCaseIsString, TypeOfSwitchCaseIsString | - |
| attribute.evl | Resolved | - |
| temporal.evl | Resolved (multiple), AttributeTypeIsTemporal | - |
| measured.evl | Resolved (multiple), AttributeTypeIsMeasured, DimensionMatches | - |
| enumeration.evl | Resolved (multiple), AttributeTypeIsEnumeration | - |
| constant.evl | Resolved (multiple) | - |
| custom.evl | Resolved (multiple) | - |
| attributeBinding.evl | Resolved (multiple), BindingIsCompatible | - |
| referenceBinding.evl | Resolved (multiple), BindingIsCompatible | - |
| filterBinding.evl | Resolved (multiple), FilterTypeIsCompatible | - |

## Technical Approach

### 1. Project Structure

```
model/src/main/java/hu/blackbelt/judo/meta/expression/validation/
├── ExpressionValidator.java           # Main entry point
├── ExpressionModelProvider.java       # Model element provider
├── constants/
│   └── ValidationConstants.java       # All constraint names as constants
└── rules/
    ├── expression/
    │   └── TypeNameValidations.java
    │   └── ExpressionValidations.java
    ├── numeric/
    │   └── NumericExpressionValidations.java
    ├── object/
    │   └── ObjectExpressionValidations.java
    ├── collection/
    │   └── CollectionExpressionValidations.java
    ├── logical/
    │   └── LogicalExpressionValidations.java
    ├── string/
    │   └── StringExpressionValidations.java
    ├── temporal/
    │   └── TemporalExpressionValidations.java
    ├── measured/
    │   └── MeasuredExpressionValidations.java
    ├── enumeration/
    │   └── EnumerationExpressionValidations.java
    ├── constant/
    │   └── ConstantExpressionValidations.java
    ├── custom/
    │   └── CustomExpressionValidations.java
    └── binding/
        ├── AttributeBindingValidations.java
        ├── ReferenceBindingValidations.java
        └── FilterBindingValidations.java
```

### 2. Test Structure

```
model-test/src/test/java/hu/blackbelt/judo/meta/expression/
├── AbstractExpressionValidationTest.java  # Base class for parameterized tests
├── ValidatorType.java                      # EVL/JAVA enum
├── ExpressionValidationNumericTest.java    # Parameterized tests
├── ExpressionValidationObjectTest.java
├── ExpressionValidationCollectionTest.java
├── ExpressionValidationLogicalTest.java
├── ExpressionValidationStringTest.java
├── ExpressionValidationPerformanceTest.java  # 10,000 element benchmark
└── validation/
    └── ZetaValidatorEngineTest.java        # Zeta framework unit tests
```

### 3. Dependencies

Add to pom.xml properties:
```xml
<judo-zeta-version>1.0.0.20251207_081454_0779b890_develop</judo-zeta-version>
```

Add dependencies:
```xml
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
```

## Success Criteria

1. All existing EVL validation rules have equivalent Java implementations
2. Parameterized tests pass for both EVL and Java validators
3. Java validation produces identical results to EVL validation
4. Performance benchmark shows Java validation is faster than EVL
5. Documentation is updated with Zeta references
6. All constraint names use constants

## Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| EVL/Java parity issues | Parameterized tests ensure identical results |
| Complex EVL expressions | Use Zeta extension methods for complex logic |
| Model adapter differences | Implement equivalent model adapter in Java |

## References

- [Judo Zeta Repository](https://github.com/BlackBeltTechnology/judo-zeta)
- [ESM Zeta Integration](../../../judo-meta-esm) - Reference implementation
- [Zeta Validation Documentation](https://github.com/BlackBeltTechnology/judo-zeta/tree/develop/docs/validation)
