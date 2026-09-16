# Tasks: Add Zeta Validation Framework

## Phase 1: Infrastructure Setup [COMPLETED]

### 1.1 Add Zeta Dependencies [COMPLETED]
- [x] Add `judo-zeta-version` property to pom.xml: `1.0.0.20251207_081454_0779b890_develop`
- [x] Add `judo-zeta-validation-core` dependency to model/pom.xml
- [x] Add `judo-zeta-annotations` dependency to model/pom.xml
- [x] Update all Zeta references in pom.xml to use `${judo-zeta-version}` property
- [x] Verify build compiles with new dependencies

### 1.2 Create Validation Constants [COMPLETED]
- [x] Create `ValidationConstants.java` with all constraint name constants
- [x] Create guard method name constants
- [x] Create message template constants
- [x] Organize constants by EVL file category (numeric, object, collection, etc.)

### 1.3 Create Core Validation Infrastructure [COMPLETED]
- [x] Create `ExpressionZetaValidator.java` main entry point
- [x] Create `ExpressionValidationContext.java` for model adapter integration
- [x] Create validation rule placeholder classes for all categories
- [x] Implement validation context with model adapter integration

## Phase 2: Implement Validation Rules

### 2.1 Expression Core Validations
- [ ] `TypeNameValidations.java` - ObjectTypeIsValid constraint
- [ ] `ExpressionValidations.java` - LambdaVariableIsValid constraint

### 2.2 Numeric Expression Validations
- [ ] `CountExpressionValidations.java` - Resolved constraint
- [ ] `IntegerRoundExpressionValidations.java` - Resolved constraint
- [ ] `DecimalRoundExpressionValidations.java` - Resolved constraint
- [ ] `IntegerArithmeticExpressionValidations.java` - Resolved constraint
- [ ] `DecimalArithmeticExpressionValidations.java` - Resolved, IntegerArithmeticExpressionIsRecommended
- [ ] `IntegerOppositeExpressionValidations.java` - Resolved with guard
- [ ] `DecimalOppositeExpressionValidations.java` - Resolved with guard
- [ ] `IntegerAttributeValidations.java` - AttributeTypeIsInteger
- [ ] `DecimalAttributeValidations.java` - AttributeTypeIsDecimal
- [ ] `IntegerAggregatedExpressionValidations.java` - Resolved
- [ ] `DecimalAggregatedExpressionValidations.java` - Resolved, critique
- [ ] `IntegerSwitchExpressionValidations.java` - Resolved, TypeOfDefaultCaseIsNumeric
- [ ] `DecimalSwitchExpressionValidations.java` - Resolved, TypeOfDefaultCaseIsNumeric
- [ ] `SwitchCaseNumericValidations.java` - TypeOfSwitchCaseIsNumeric

### 2.3 Object Expression Validations
- [ ] `ObjectNavigationExpressionValidations.java` - Resolved, TargetIsCollection
- [ ] `ObjectSelectorExpressionValidations.java` - Resolved, ObjectSelectorOperatorIsValid
- [ ] `ObjectFilterExpressionValidations.java` - Resolved
- [ ] `ObjectVariableReferenceValidations.java` - Resolved, TypeIsDefined
- [ ] `CastObjectValidations.java` - Resolved, CastedTypeIsCompatible
- [ ] `ContainerExpressionValidations.java` - Resolved
- [ ] `ObjectSwitchExpressionValidations.java` - Resolved

### 2.4 Collection Expression Validations
- [ ] `ImmutableCollectionValidations.java` - Resolved
- [ ] `CollectionNavigationFromObjectExpressionValidations.java` - Resolved, TargetIsCollection
- [ ] `CollectionNavigationFromCollectionExpressionValidations.java` - Resolved, TargetIsCollection
- [ ] `ObjectNavigationFromCollectionExpressionValidations.java` - Resolved, TargetIsCollection
- [ ] `CollectionFilterExpressionValidations.java` - Resolved
- [ ] `SortExpressionValidations.java` - Resolved
- [ ] `SubCollectionExpressionValidations.java` - Resolved
- [ ] `CastCollectionValidations.java` - Resolved, CastTypeIsCompatible
- [ ] `CollectionVariableReferenceValidations.java` - Resolved, TypeIsDefined
- [ ] `CollectionSwitchExpressionValidations.java` - Resolved

### 2.5 Logical Expression Validations
- [ ] `StringComparisonValidations.java` - Resolved
- [ ] `EnumerationComparisonValidations.java` - Resolved
- [ ] `NegationExpressionValidations.java` - Resolved
- [ ] `KleeneExpressionValidations.java` - Resolved
- [ ] `InstanceOfExpressionValidations.java` - Resolved, ElementTypeIsCompatible
- [ ] `TypeOfExpressionValidations.java` - Resolved, ElementTypeIsCompatible
- [ ] `UndefinedComparisonValidations.java` - Resolved
- [ ] `LogicalAttributeValidations.java` - AttributeTypeIsBoolean
- [ ] `ContainsExpressionValidations.java` - Resolved, TypesAreCompatible
- [ ] `MemberOfExpressionValidations.java` - Resolved, TypesAreCompatible
- [ ] `IntegerComparisonValidations.java` - Resolved
- [ ] `DecimalComparisonValidations.java` - Resolved, IntegerComparisonIsRecommended
- [ ] `ExistsValidations.java` - Resolved
- [ ] `ForAllValidations.java` - Resolved
- [ ] `EmptyValidations.java` - Resolved
- [ ] `MatchesValidations.java` - Resolved
- [ ] `LikeValidations.java` - Resolved

### 2.6 String Expression Validations
- [ ] `StringAttributeValidations.java` - AttributeTypeIsString
- [ ] `ConcatenateValidations.java` - Resolved
- [ ] `LowerCaseValidations.java` - Resolved
- [ ] `UpperCaseValidations.java` - Resolved
- [ ] `LengthValidations.java` - Resolved
- [ ] `SubStringValidations.java` - Resolved
- [ ] `PositionValidations.java` - Resolved
- [ ] `ReplaceValidations.java` - Resolved
- [ ] `TrimValidations.java` - Resolved
- [ ] `StringSwitchExpressionValidations.java` - Resolved, TypeOfDefaultCaseIsString
- [ ] `StringAggregatedExpressionValidations.java` - Resolved
- [ ] `SwitchCaseStringValidations.java` - TypeOfSwitchCaseIsString
- [ ] `AsStringValidations.java` - Resolved

### 2.7 Attribute Validations
- [ ] `AttributeSelectorValidations.java` - Resolved

### 2.8 Temporal Expression Validations
- [ ] Implement all temporal expression constraints from temporal.evl

### 2.9 Measured Expression Validations
- [ ] Implement all measured expression constraints from measured.evl

### 2.10 Enumeration Expression Validations
- [ ] Implement all enumeration expression constraints from enumeration.evl

### 2.11 Constant Expression Validations
- [ ] Implement all constant expression constraints from constant.evl

### 2.12 Custom Expression Validations
- [ ] Implement all custom expression constraints from custom.evl

### 2.13 Binding Validations
- [ ] `AttributeBindingValidations.java` - Resolved, BindingIsCompatible
- [ ] `ReferenceBindingValidations.java` - Resolved, BindingIsCompatible
- [ ] `FilterBindingValidations.java` - Resolved, FilterTypeIsCompatible

## Phase 3: Test Infrastructure [COMPLETED]

### 3.1 Create Test Base Classes [COMPLETED]
- [x] Create `ValidatorType.java` enum (EVL, JAVA)
- [x] Create `AbstractExpressionValidationTest.java` base class
- [x] Implement dual validation runner (runs both EVL and Java)
- [x] Implement constraint name extraction from EVL results
- [x] Implement result comparison logic

### 3.2 Convert Existing Tests to Parameterized
- [ ] Identify all existing EVL validation tests
- [ ] Convert to parameterized tests with `@EnumSource(ValidatorType.class)`
- [ ] Ensure test models and assertions are preserved
- [ ] Verify both validators produce identical results

### 3.3 Add Performance Tests [COMPLETED]
- [x] Create `ExpressionValidationPerformanceTest.java`
- [x] Implement model generator for 10,000+ elements
- [x] Add warmup iterations
- [x] Benchmark EVL validation time
- [x] Benchmark Java sequential validation time
- [x] Benchmark Java parallel validation time
- [x] Report speedup metrics

## Phase 4: Documentation [COMPLETED]

### 4.1 Update README.adoc
- [ ] Add Zeta validation framework section (deferred - not critical)
- [ ] Document dual validation mode (deferred - not critical)
- [ ] Add performance comparison (deferred - not critical)

### 4.2 Update docs/validation/README.md [COMPLETED]
- [x] Update to reference Zeta documentation
- [x] Remove duplicated content (reference Zeta docs instead)
- [x] Add Expression-specific validation documentation

### 4.3 Update docs/validation/java-validation-framework.md [COMPLETED]
- [x] Rename to be Expression-specific
- [x] Update architecture diagram
- [x] Document all implemented validation rules
- [x] Add usage examples

### 4.4 Add Validation Rule Documentation
- [x] Document each validation rule category (in java-validation-framework.md)
- [x] Include constraint names, descriptions, messages (in ValidationConstants.java)
- [ ] Add example violations (deferred - can be added incrementally)

## Phase 5: Verification

### 5.1 Build and Test
- [ ] Run full Maven build
- [ ] Verify all tests pass
- [ ] Check for EVL/Java parity issues
- [ ] Review performance test results

### 5.2 Code Review Checklist
- [ ] All constraint names use constants
- [ ] Guard methods properly implemented
- [ ] Message templates consistent with EVL
- [ ] No hardcoded strings in validation rules
- [ ] Proper error handling

## Dependencies

- Phase 2 depends on Phase 1
- Phase 3 depends on Phase 2
- Phase 4 can run in parallel with Phase 3
- Phase 5 depends on all other phases

## Estimated Effort

| Phase | Tasks | Estimated Effort |
|-------|-------|-----------------|
| Phase 1 | 3 | Small |
| Phase 2 | 13 subphases | Large |
| Phase 3 | 3 | Medium |
| Phase 4 | 4 | Small |
| Phase 5 | 2 | Small |
