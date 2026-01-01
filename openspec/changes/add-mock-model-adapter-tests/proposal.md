# Add Mock Model Adapter and Comprehensive Validation Tests

## Summary

Create a mock model adapter with an in-memory model structure to enable comprehensive testing of all Expression validation constraints. This includes positive and negative test cases for every constraint and guard method in both EVL and Java (Zeta) validators, as well as implementing the missing measure-related ModelAdapter methods.

## Motivation

The current validation testing infrastructure has several gaps:

1. **No Mock Model Adapter**: Tests use Mockito mocks with minimal stubbing, which cannot properly exercise validations that depend on type resolution, attribute lookup, and measure calculations.

2. **Incomplete Test Coverage**: The `ExpressionValidationPerformanceTest` generates expressions but doesn't validate actual constraint behavior. There are no systematic tests for each of the 50+ validation constraints.

3. **Missing Measure Support**: The ModelAdapter interface methods for measures are not implemented in a testable way, making measure-related validations untestable.

4. **No Positive/Negative Test Cases**: Without proper test infrastructure, we cannot verify that constraints correctly pass for valid models and fail for invalid ones.

## Scope

### In Scope

- Create `MockModelAdapter` implementing the full `ModelAdapter` interface with in-memory model structures
- Create `MockModel` classes representing entities, attributes, references, primitives, enumerations, measures, and units
- Create `MockMeasureProvider` implementing `MeasureProvider` interface for measure testing
- Implement comprehensive parameterized tests for all validation constraints:
  - Expression core constraints (ObjectTypeIsValid, LambdaVariableIsValid, Resolved)
  - Numeric constraints (AttributeTypeIsInteger, AttributeTypeIsDecimal, etc.)
  - Logical constraints (AttributeTypeIsBoolean, ElementTypeIsCompatible, etc.)
  - String constraints (AttributeTypeIsString, etc.)
  - Temporal constraints (AttributeTypeIsDate, AttributeTypeIsTimestamp, etc.)
  - Measured constraints (MeasureIsValid, MeasureOfAdditionIsValid, etc.)
  - Enumeration constraints (AttributeTypeIsEnumeration, etc.)
  - Custom constraints (AttributeTypeIsCustom, etc.)
  - Binding constraints (AttributeBindingIsValid, NumericExpressionMatchesBinding, etc.)
- Each constraint test includes both positive (passes validation) and negative (fails validation) scenarios
- Tests run with both EVL and Java validators via parameterized tests

### Out of Scope

- Changes to the existing validation rule implementations
- Integration with real ASM/PSM/ESM models (those are separate integration tests)
- UI or tooling changes

## Technical Approach

### 1. Mock Model Structure

Create a simple in-memory model hierarchy:

```
MockModel
├── MockNamespaceElement (base)
├── MockPrimitive extends MockNamespaceElement
│   ├── type: PrimitiveType (INTEGER, DECIMAL, BOOLEAN, STRING, DATE, TIMESTAMP, TIME, CUSTOM)
│   └── measuredType: MockMeasure (optional)
├── MockEnumeration extends MockPrimitive
│   └── members: List<String>
├── MockEntityType extends MockNamespaceElement
│   ├── attributes: Map<String, MockAttribute>
│   ├── references: Map<String, MockReference>
│   └── superTypes: List<MockEntityType>
├── MockAttribute
│   ├── name: String
│   ├── type: MockPrimitive
│   └── derived: boolean
├── MockReference
│   ├── name: String
│   ├── target: MockEntityType
│   └── collection: boolean
├── MockMeasure
│   ├── namespace: String
│   ├── name: String
│   ├── units: List<MockUnit>
│   └── baseMeasure: boolean
└── MockUnit
    ├── name: String
    ├── symbol: String
    └── durationSupportsAddition: boolean
```

### 2. MockModelAdapter Implementation

Implement `ModelAdapter<MockNamespaceElement, MockPrimitive, MockEnumeration, MockEntityType, MockAttribute, MockReference, MockTransferObject, MockTransferAttribute, MockTransferRelation, MockSequence, MockMeasure, MockUnit>` with:

- Type name resolution from a registry
- Attribute/reference lookup on entity types
- Type checking methods (isNumeric, isBoolean, isString, etc.)
- Measure resolution and dimension calculation
- All methods return proper values based on the mock model state

### 3. Test Structure

Create test classes organized by validation category:

```
model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/
├── mock/
│   ├── MockModel.java
│   ├── MockModelAdapter.java
│   ├── MockMeasureProvider.java
│   └── MockModelBuilder.java (fluent API for test setup)
└── tests/
    ├── TypeNameValidationTest.java
    ├── NumericExpressionValidationTest.java
    ├── LogicalExpressionValidationTest.java
    ├── StringExpressionValidationTest.java
    ├── TemporalExpressionValidationTest.java
    ├── MeasuredExpressionValidationTest.java
    ├── EnumerationExpressionValidationTest.java
    ├── CustomExpressionValidationTest.java
    ├── ObjectExpressionValidationTest.java
    ├── CollectionExpressionValidationTest.java
    └── BindingValidationTest.java
```

### 4. Test Pattern

Each test class follows this pattern:

```java
@ParameterizedTest(name = "{0}: {1}")
@MethodSource("constraintTestCases")
void testConstraint(ValidatorType validatorType, String testName, 
                    Runnable modelSetup, List<String> expectedErrors) {
    this.validatorType = validatorType;
    initModel();
    modelSetup.run();
    runValidation(expectedErrors, Collections.emptyList());
}

static Stream<Arguments> constraintTestCases() {
    return Stream.of(
        // Positive case - should pass
        Arguments.of(ValidatorType.EVL, "IntegerAttribute_ValidType_Passes", 
            (Runnable) () -> setupValidIntegerAttribute(), 
            Collections.emptyList()),
        Arguments.of(ValidatorType.JAVA, "IntegerAttribute_ValidType_Passes", 
            (Runnable) () -> setupValidIntegerAttribute(), 
            Collections.emptyList()),
        // Negative case - should fail
        Arguments.of(ValidatorType.EVL, "IntegerAttribute_StringType_Fails", 
            (Runnable) () -> setupInvalidIntegerAttribute(), 
            List.of("AttributeTypeIsInteger")),
        Arguments.of(ValidatorType.JAVA, "IntegerAttribute_StringType_Fails", 
            (Runnable) () -> setupInvalidIntegerAttribute(), 
            List.of("AttributeTypeIsInteger"))
    );
}
```

## Success Criteria

1. All 50+ validation constraints have at least one positive and one negative test case
2. Tests pass for both EVL and Java (Zeta) validators
3. MockModelAdapter correctly implements all ModelAdapter methods
4. Measure-related validations are fully testable
5. Test execution time remains reasonable (< 60 seconds for all tests)
6. Code coverage for validation rules reaches > 90%

## Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| Mock model diverges from real model behavior | Document mock assumptions; add integration tests with real models |
| EVL and Java validators have different error formats | Use constraint name extraction (already implemented in AbstractExpressionValidationTest) |
| Large number of test cases slow down CI | Use parallel test execution; consider test categories |
| Missing edge cases in mock implementation | Review EVL validation logic to identify all code paths |

## Dependencies

- Existing `AbstractExpressionValidationTest` base class
- `ValidatorType` enum for parameterized testing
- `ExpressionZetaValidator` for Java validation
- EVL scripts for EVL validation
