# Validation Testing Capability

This capability defines the requirements for comprehensive validation testing infrastructure with mock model adapter support.

## ADDED Requirements

### Requirement: Mock Model Adapter Infrastructure

The system SHALL provide a MockModelAdapter that implements the full ModelAdapter interface with in-memory model structures for testing purposes.

#### Scenario: MockModelAdapter resolves entity types

Given a MockModelAdapter configured with entity type "Customer" in namespace "test"
When a TypeName with namespace "test" and name "Customer" is queried
Then the MockModelAdapter returns the MockEntityType for "Customer"

#### Scenario: MockModelAdapter resolves attributes

Given a MockModelAdapter with entity "Customer" having attribute "age" of type Integer
When getAttribute is called with entity "Customer" and attribute name "age"
Then the MockModelAdapter returns the MockAttribute with Integer type

#### Scenario: MockModelAdapter handles type checking

Given a MockModelAdapter with primitive type "Integer" configured as PrimitiveType.INTEGER
When isInteger is called with the Integer primitive
Then the MockModelAdapter returns true
When isString is called with the Integer primitive
Then the MockModelAdapter returns false

### Requirement: Mock Measure Provider

The system SHALL provide a MockMeasureProvider implementing MeasureProvider interface for testing measure-related validations.

#### Scenario: MockMeasureProvider resolves measures

Given a MockMeasureProvider with measure "Length" having units "m", "cm", "mm"
When getMeasure is called with namespace and name "Length"
Then the MockMeasureProvider returns the MockMeasure for "Length"

#### Scenario: MockMeasureProvider supports duration units

Given a MockMeasureProvider with duration measure having units "ms", "s", "min"
When isDurationSupportingAddition is called with unit "s"
Then the MockMeasureProvider returns true

### Requirement: Fluent Model Builder API

The system SHALL provide a MockModelBuilder with fluent API for constructing test models.

#### Scenario: Build simple entity model

Given a new MockModelBuilder
When withPrimitive, withEntity, and withAttribute are called
And build is called
Then a MockModelAdapter is returned with configured entity and attributes

#### Scenario: Build model with measures

Given a new MockModelBuilder
When withMeasure is called with measure name and units
And build is called
Then a MockModelAdapter is returned with configured measures

### Requirement: Numeric Expression Validation Tests

The system SHALL provide parameterized tests for all numeric expression validation constraints.

#### Scenario: IntegerAttribute with valid integer type passes validation

Given an expression model with IntegerAttribute pointing to an integer-typed attribute
When validation is executed with EVL and Java validators
Then no validation errors are reported by either validator

#### Scenario: IntegerAttribute with string type fails validation

Given an expression model with IntegerAttribute pointing to a string-typed attribute
When validation is executed with EVL and Java validators
Then error "AttributeTypeIsInteger" is reported by both validators

#### Scenario: DecimalAttribute validation

Given an expression model with DecimalAttribute
When validation is executed with both validators
Then results match expected outcomes for decimal type checking

### Requirement: Logical Expression Validation Tests

The system SHALL provide parameterized tests for all logical expression validation constraints.

#### Scenario: BooleanAttribute with valid boolean type passes validation

Given an expression model with LogicalAttribute pointing to a boolean-typed attribute
When validation is executed with both validators
Then no validation errors are reported

#### Scenario: BooleanAttribute with non-boolean type fails validation

Given an expression model with LogicalAttribute pointing to a string-typed attribute
When validation is executed with both validators
Then error "AttributeTypeIsBoolean" is reported

### Requirement: String Expression Validation Tests

The system SHALL provide parameterized tests for all string expression validation constraints.

#### Scenario: StringAttribute with valid string type passes validation

Given an expression model with StringAttribute pointing to a string-typed attribute
When validation is executed with both validators
Then no validation errors are reported

#### Scenario: StringAttribute with non-string type fails validation

Given an expression model with StringAttribute pointing to an integer-typed attribute
When validation is executed with both validators
Then error "AttributeTypeIsString" is reported

### Requirement: Temporal Expression Validation Tests

The system SHALL provide parameterized tests for all temporal expression validation constraints.

#### Scenario: DateAttribute with valid date type passes validation

Given an expression model with DateAttribute pointing to a date-typed attribute
When validation is executed with both validators
Then no validation errors are reported

#### Scenario: TimestampAddition with valid duration passes validation

Given an expression model with TimestampAdditionExpression using measured duration
When validation is executed with both validators
Then no validation errors are reported

### Requirement: Measured Expression Validation Tests

The system SHALL provide parameterized tests for all measured expression validation constraints.

#### Scenario: MeasuredDecimal with valid measure passes validation

Given an expression model with MeasuredDecimal referencing valid measure and unit
When validation is executed with both validators
Then no validation errors are reported

#### Scenario: Addition of different measures fails validation

Given an expression model with DecimalArithmeticExpression adding expressions with different measures
When validation is executed with both validators
Then error "MeasureOfAdditionIsValid" is reported

### Requirement: Binding Validation Tests

The system SHALL provide parameterized tests for all binding validation constraints.

#### Scenario: NumericExpressionMatchesBinding with matching types passes

Given an AttributeBinding with integer attribute and IntegerExpression
When validation is executed with both validators
Then no validation errors are reported

#### Scenario: NumericExpressionMatchesBinding with mismatched types fails

Given an AttributeBinding with integer attribute and StringExpression
When validation is executed with both validators
Then error "NumericExpressionMatchesBinding" is reported

### Requirement: Object and Collection Expression Validation Tests

The system SHALL provide parameterized tests for object and collection expression validation constraints.

#### Scenario: ObjectNavigation with valid reference passes validation

Given an expression model with ObjectNavigationExpression using existing reference
When validation is executed with both validators
Then no validation errors are reported

#### Scenario: CastObject with compatible type passes validation

Given an expression model with CastObject casting to a subtype
When validation is executed with both validators
Then no validation errors are reported

### Requirement: Enumeration and Custom Expression Validation Tests

The system SHALL provide parameterized tests for enumeration and custom expression validation constraints.

#### Scenario: EnumerationAttribute with valid enum type passes validation

Given an expression model with EnumerationAttribute pointing to an enum-typed attribute
When validation is executed with both validators
Then no validation errors are reported

#### Scenario: CustomAttribute with valid custom type passes validation

Given an expression model with CustomAttribute pointing to a custom-typed attribute
When validation is executed with both validators
Then no validation errors are reported

### Requirement: Test Parity Between EVL and Java Validators

The system SHALL ensure that EVL and Java validators produce identical results for all test cases.

#### Scenario: All tests produce identical results with both validators

Given a test suite with all validation test cases
When tests are executed with EVL validator and Java validator
Then both validators report identical constraint violations for each test case
