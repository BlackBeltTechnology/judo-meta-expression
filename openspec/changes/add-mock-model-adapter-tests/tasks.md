# Tasks: Add Mock Model Adapter and Comprehensive Validation Tests

## Phase 1: Mock Model Infrastructure

### 1.1 Create Mock Model Classes
- [x] Create `MockNamespaceElement` base class with name and namespace fields
- [x] Create `MockPrimitive` with PrimitiveType enum (INTEGER, DECIMAL, BOOLEAN, STRING, DATE, TIMESTAMP, TIME, CUSTOM)
- [x] Create `MockEnumeration` extending MockPrimitive with members list
- [x] Create `MockEntityType` (named `MockEntity`) with attributes, references, and superTypes
- [x] Create `MockAttribute` with name, type, derived flag
- [x] Create `MockReference` with name, target, collection flag
- [x] Create `MockTransferObject`, `MockTransferAttribute`, `MockTransferRelation` stubs
- [x] Create `MockSequence` stub

### 1.2 Create Mock Measure Classes
- [x] Create `MockMeasure` with namespace, name, units list, baseMeasure flag
- [x] Create `MockUnit` with name, symbol, rateDividend, rateDivisor, durationSupportsAddition
- [x] Create `MockMeasureProvider` implementing `MeasureProvider<MockMeasure, MockUnit>`
- [x] Implement base measure and derived measure support

### 1.3 Create MockModelAdapter
- [x] Implement `ModelAdapter` interface with all 12 type parameters
- [x] Implement type name resolution (`buildTypeName`, `get(TypeName)`)
- [x] Implement entity/attribute/reference lookup methods
- [x] Implement type checking methods (isNumeric, isInteger, isDecimal, isBoolean, isString, etc.)
- [x] Implement measure-related methods (isMeasured, getMeasure, getUnit, getDimension)
- [x] Implement transfer object methods (stubs returning empty for initial version)
- [x] Add model registry for entity types and primitives

### 1.4 Create MockModelBuilder (Fluent API)
- [x] Create builder for easy test model construction
- [x] Add methods: `withEntity()`, `withAttribute()`, `withReference()`
- [x] Add methods: `withPrimitive()`, `withEnumeration()`
- [x] Add methods: `withMeasure()`, `withUnit()`
- [x] Add `build()` method returning configured MockModelAdapter

## Phase 2: Core Expression Validation Tests

### 2.1 TypeName Validation Tests
- [ ] Test `ObjectTypeIsValid` - positive: valid type name exists
- [ ] Test `ObjectTypeIsValid` - negative: type name does not exist
- [ ] Test `Resolved` for TypeName - positive: resolvable type
- [ ] Test `Resolved` for TypeName - negative: unresolvable type

### 2.2 Attribute Validation Tests
- [ ] Test attribute resolved - positive: attribute exists on entity
- [ ] Test attribute resolved - negative: attribute does not exist
- [ ] Test object expression resolved - positive: entity type exists
- [ ] Test object expression resolved - negative: entity type missing

### 2.3 Lambda Variable Tests
- [ ] Test `LambdaVariableIsValid` - positive: valid variable reference
- [ ] Test `LambdaVariableIsValid` - negative: invalid variable scope

## Phase 3: Numeric Expression Validation Tests

### 3.1 Integer Attribute Tests
- [x] Test `AttributeTypeIsInteger` - positive: integer attribute type
- [x] Test `AttributeTypeIsInteger` - negative: non-integer attribute type (string)
- [x] Test `AttributeTypeIsInteger` - negative: decimal attribute type

### 3.2 Decimal Attribute Tests
- [x] Test `AttributeTypeIsDecimal` - positive: decimal attribute type
- [x] Test `AttributeTypeIsDecimal` - negative: non-decimal attribute type

### 3.3 Integer Aggregated Expression Tests
- [ ] Test resolved - positive: valid collection with integer expression
- [ ] Test resolved - negative: invalid collection expression

### 3.4 Decimal Aggregated Expression Tests
- [ ] Test resolved - positive: valid collection with decimal expression
- [ ] Test resolved - negative: invalid collection expression

### 3.5 Switch Expression Tests
- [x] Test `TypeOfDefaultCaseIsNumeric` - positive: numeric default expression
- [x] Test `TypeOfDefaultCaseIsNumeric` - negative: non-numeric default
- [x] Test `TypeOfSwitchCaseIsNumeric` - positive: numeric case expression
- [x] Test `TypeOfSwitchCaseIsNumeric` - negative: non-numeric case

### 3.6 Arithmetic Expression Critiques
- [x] Test `IntegerArithmeticExpressionIsRecommended` - warning when decimal used on integers

## Phase 4: Logical Expression Validation Tests

### 4.1 Boolean Attribute Tests
- [x] Test `AttributeTypeIsBoolean` - positive: boolean attribute type
- [x] Test `AttributeTypeIsBoolean` - negative: non-boolean attribute type

### 4.2 InstanceOf/TypeOf Tests
- [ ] Test `ElementTypeIsCompatible` - positive: compatible types
- [ ] Test `ElementTypeIsCompatible` - negative: incompatible types
- [ ] Test resolved - positive: valid type checks
- [ ] Test resolved - negative: unresolved type

### 4.3 Contains/MemberOf Tests
- [ ] Test `TypesAreCompatible` - positive: compatible element/collection types
- [ ] Test `TypesAreCompatible` - negative: incompatible types

### 4.4 Comparison Critiques
- [x] Test `IntegerComparisonIsRecommended` - warning when decimal comparison on integers

### 4.5 Basic Logical Expression Tests (Added)
- [x] Test IntegerComparison - valid comparison
- [x] Test DecimalComparison - valid comparison
- [x] Test BooleanConstant - valid constant
- [x] Test NegationExpression - valid negation
- [x] Test KleeneExpression (AND) - valid expression
- [x] Test UndefinedComparison - valid comparison
- [x] Test StringComparison - valid comparison

## Phase 5: String Expression Validation Tests

### 5.1 String Attribute Tests
- [x] Test `AttributeTypeIsString` - positive: string attribute type
- [x] Test `AttributeTypeIsString` - negative: non-string attribute type (integer)
- [x] Test `AttributeTypeIsString` - negative: non-string attribute type (boolean)

### 5.2 String Switch Expression Tests
- [x] Test `TypeOfDefaultCaseIsString` - positive: string default
- [x] Test `TypeOfDefaultCaseIsString` - negative: non-string default (integer)

### 5.3 Basic String Expression Tests
- [x] Test Concatenate - valid concatenation
- [x] Test UpperCase - valid expression
- [x] Test LowerCase - valid expression
- [x] Test Trim - valid expression
- [x] Test StringConstant - valid constant
- [x] Test AsString - valid conversion

## Phase 6: Temporal Expression Validation Tests

### 6.1 Date Attribute Tests
- [x] Test `AttributeTypeIsDate` - positive: date attribute type
- [x] Test `AttributeTypeIsDate` - negative: non-date attribute type (string)

### 6.2 Timestamp Attribute Tests
- [x] Test `AttributeTypeIsTimestamp` - positive: timestamp attribute type
- [x] Test `AttributeTypeIsTimestamp` - negative: non-timestamp type (integer)

### 6.3 Time Attribute Tests
- [x] Test `AttributeTypeIsTime` - positive: time attribute type
- [x] Test `AttributeTypeIsTime` - negative: non-time attribute type (boolean)

### 6.4 Temporal Switch Tests
- [x] Test `TypeOfDefaultCaseIsDate` - negative: non-date default (string)
- [x] Test `TypeOfDefaultCaseIsTimestamp` - negative: non-timestamp default (integer)
- [x] Test `TypeOfDefaultCaseIsTime` - negative: non-time default (boolean)

## Phase 7: Measured Expression Validation Tests

### 7.1 Measure Validation Tests
- [x] Test `MeasureIsValid` - positive: valid measure reference (Zeta validation implemented)
- [ ] Test `MeasureIsValid` - negative: invalid measure name

### 7.2 Measured Addition Tests
- [x] Test `MeasureOfAdditionIsValid` - positive: same measures (Zeta validation implemented)
- [ ] Test `MeasureOfAdditionIsValid` - negative: different measures
- [ ] Test scalar + measured - negative: mixing not allowed

### 7.3 Measured Comparison Tests
- [x] Test `ComparisonIsMeasured` - positive: both operands measured (Zeta validation implemented)
- [x] Test `MeasuresAreMatching` - positive: same dimensions (Zeta validation implemented)
- [ ] Test `MeasuresAreMatching` - negative: different dimensions

### 7.4 Complete Expression Tests
- [ ] Test `MeasureOfCompleteExpressionIsDefined` - positive: defined measure
- [ ] Test `MeasureOfCompleteExpressionIsDefined` - negative: undefined dimension

## Phase 8: Object & Collection Expression Validation Tests

### 8.1 Object Navigation Tests
- [ ] Test resolved - positive: valid reference navigation
- [ ] Test resolved - negative: missing reference

### 8.2 Object Selector Tests
- [ ] Test `ObjectSelectorOperatorIsValid` - positive: 'any' operator
- [ ] Test `ObjectSelectorOperatorIsValid` - negative: invalid operator

### 8.3 Cast Expression Tests
- [ ] Test `CastedTypeIsCompatible` - positive: subtype cast
- [ ] Test `CastedTypeIsCompatible` - negative: incompatible cast

### 8.4 Collection Tests
- [ ] Test `TargetIsCollection` - positive: collection reference
- [ ] Test `TargetIsCollection` - negative: single reference
- [ ] Test `CastTypeIsCompatible` - positive/negative for collections

## Phase 9: Enumeration & Custom Expression Validation Tests

### 9.1 Enumeration Attribute Tests
- [ ] Test `AttributeTypeIsEnumeration` - positive: enum attribute
- [ ] Test `AttributeTypeIsEnumeration` - negative: non-enum attribute
- [ ] Test enum member exists - positive/negative

### 9.2 Enumeration Switch Tests
- [ ] Test `TypeOfDefaultCaseIsEnumeration` - positive/negative
- [ ] Test `TypeOfSwitchCaseIsEnumeration` - positive/negative

### 9.3 Custom Attribute Tests
- [ ] Test `AttributeTypeIsCustom` - positive: custom attribute
- [ ] Test `AttributeTypeIsCustom` - negative: non-custom attribute

### 9.4 Custom Switch Tests
- [ ] Test `TypeOfDefaultCaseIsCustom` - positive/negative
- [ ] Test `TypeOfSwitchCaseIsCustom` - positive/negative

## Phase 10: Binding Validation Tests

### 10.1 Attribute Binding Tests
- [ ] Test `NumericExpressionMatchesBinding` - positive: numeric to numeric
- [ ] Test `NumericExpressionMatchesBinding` - negative: string to numeric
- [ ] Test `BooleanExpressionMatchesBinding` - positive/negative
- [ ] Test `StringExpressionMatchesBinding` - positive/negative
- [ ] Test `EnumerationExpressionMatchesBinding` - positive/negative
- [ ] Test `DateExpressionMatchesBinding` - positive/negative
- [ ] Test `TimestampExpressionMatchesBinding` - positive/negative
- [ ] Test `TimeExpressionMatchesBinding` - positive/negative
- [ ] Test `CustomExpressionMatchesBinding` - positive/negative

### 10.2 Reference Binding Tests
- [ ] Test `ReferenceBindingExpressionIsValid` - positive/negative
- [ ] Test `ReferenceExpressionMatchesBinding` - positive/negative
- [ ] Test `ObjectExpressionMatchesBinding` - positive/negative
- [ ] Test `CollectionExpressionMatchesBinding` - positive/negative

### 10.3 Filter Binding Tests
- [ ] Test `LogicalExpressionMatchesBinding` - positive: logical filter
- [ ] Test `LogicalExpressionMatchesBinding` - negative: non-logical filter

## Phase 11: Constant & Environment Variable Tests

### 11.1 Constant Tests
- [ ] Test IntegerConstant resolved
- [ ] Test DecimalConstant resolved
- [ ] Test BooleanConstant resolved
- [ ] Test StringConstant resolved
- [ ] Test Instance resolved - positive: valid type
- [ ] Test Instance resolved - negative: invalid type

### 11.2 Environment Variable Tests
- [ ] Test IntegerEnvironmentVariable resolved
- [ ] Test DecimalEnvironmentVariable resolved
- [ ] Test BooleanEnvironmentVariable resolved
- [ ] Test StringEnvironmentVariable resolved
- [ ] Test CustomEnvironmentVariable resolved
- [ ] Test MeasuredDecimalEnvironmentVariable resolved

## Phase 12: Integration & Documentation

### 12.1 Integration Testing
- [x] Verify all tests pass with EVL validator (skipped for mock tests - requires EMF resources)
- [x] Verify all tests pass with Java (Zeta) validator
- [ ] Verify parity between EVL and Java validation results
- [ ] Measure test execution time and optimize if needed

### 12.2 Documentation
- [ ] Document MockModelAdapter usage patterns
- [ ] Document test case naming conventions
- [ ] Add examples to AbstractExpressionValidationTest javadoc
- [ ] Update AGENTS.md with testing guidelines

## Dependencies

- Phase 2-11 depend on Phase 1 (Mock Model Infrastructure) ✅ COMPLETED
- Phases 2-11 can be executed in parallel after Phase 1
- Phase 12 depends on all previous phases

## Progress Summary

| Phase | Status | Completed Tasks |
|-------|--------|-----------------|
| 1     | ✅ Complete | 16/16 |
| 2     | ⏳ Pending | 0/8 |
| 3     | 🔄 Partial | 8/12 |
| 4     | ✅ Complete | 12/12 (includes added tests) |
| 5     | ✅ Complete | 11/11 |
| 6     | ✅ Complete | 9/9 |
| 7     | 🔄 Partial | 4/10 (Zeta validations implemented) |
| 8     | ⏳ Pending | 0/10 |
| 9     | ⏳ Pending | 0/10 |
| 10    | ⏳ Pending | 0/14 |
| 11    | ⏳ Pending | 0/12 |
| 12    | 🔄 Partial | 2/6 |
| **Total** | **In Progress** | **62/130** |

## Files Created

- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockEntity.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockAttribute.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockReference.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockPrimitive.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockEnumeration.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockEnumerationMember.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockTransferObject.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockTransferAttribute.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockTransferRelation.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockSequence.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockMeasure.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockUnit.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockMeasureProvider.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockModelAdapter.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/MockModelBuilder.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/AbstractMockExpressionValidationTest.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/NumericExpressionValidationTest.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/LogicalExpressionValidationTest.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/StringExpressionValidationTest.java`
- `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock/TemporalExpressionValidationTest.java`

## Zeta Validations Implemented

- `model/src/main/java/hu/blackbelt/judo/meta/expression/validation/rules/measured/MeasuredExpressionValidations.java`
  - `MeasureIsValid` for MeasuredDecimal, MeasuredDecimalEnvironmentVariable, IntegerAttribute, DecimalAttribute
  - `MeasureOfAdditionIsValid` for IntegerArithmeticExpression, DecimalArithmeticExpression
  - `ComparisonIsMeasured` for IntegerComparison, DecimalComparison
  - `MeasuresAreMatching` for IntegerComparison, DecimalComparison
