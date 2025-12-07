# Design: Mock Model Adapter and Validation Test Infrastructure

## Overview

This document describes the architectural design for the mock model adapter and comprehensive validation test infrastructure.

## Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Test Infrastructure                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────┐    ┌────────────────────────────────────────┐  │
│  │  Test Classes          │    │  Mock Model Infrastructure              │  │
│  │  ─────────────────     │    │  ────────────────────────               │  │
│  │                        │    │                                         │  │
│  │  - NumericExprTests    │───>│  MockModelBuilder                       │  │
│  │  - LogicalExprTests    │    │    │                                    │  │
│  │  - StringExprTests     │    │    ├── withEntity(name)                 │  │
│  │  - TemporalExprTests   │    │    ├── withAttribute(name, type)        │  │
│  │  - MeasuredExprTests   │    │    ├── withReference(name, target)      │  │
│  │  - BindingTests        │    │    ├── withMeasure(ns, name, units)     │  │
│  │  - ...                 │    │    └── build() → MockModelAdapter       │  │
│  │                        │    │                                         │  │
│  └──────────┬─────────────┘    └──────────────────┬─────────────────────┘  │
│             │                                      │                        │
│             │ extends                              │ creates                │
│             ▼                                      ▼                        │
│  ┌────────────────────────┐    ┌────────────────────────────────────────┐  │
│  │ AbstractExprValidTest  │    │  MockModelAdapter                       │  │
│  │ ──────────────────     │    │  ────────────────                       │  │
│  │                        │    │                                         │  │
│  │  - initModel()         │───>│  implements ModelAdapter<               │  │
│  │  - setModelAdapter()   │    │    MockNE, MockP, MockE, MockC,         │  │
│  │  - runValidation()     │    │    MockPTE, MockRTE, MockTO,            │  │
│  │  - runEvlValidation()  │    │    MockTA, MockTR, MockS, MockM, MockU> │  │
│  │                        │    │                                         │  │
│  └──────────┬─────────────┘    │  - type registry                        │  │
│             │                   │  - entity registry                      │  │
│             │                   │  - measure provider                     │  │
│             ▼                   │                                         │  │
│  ┌────────────────────────────────────────────────────────────────────┐   │
│  │                      Validation Execution                          │   │
│  │                      ────────────────────                          │   │
│  │                                                                    │   │
│  │   ┌─────────────────┐           ┌─────────────────────────────┐   │   │
│  │   │  EVL Validator  │           │  Java (Zeta) Validator      │   │   │
│  │   │  ─────────────  │           │  ─────────────────────      │   │   │
│  │   │                 │           │                             │   │   │
│  │   │  expression.evl │           │  ExpressionZetaValidator    │   │   │
│  │   │  + imports      │           │  + ValidationRegistry       │   │   │
│  │   │                 │           │  + ValidationExecutor       │   │   │
│  │   └────────┬────────┘           └──────────────┬──────────────┘   │   │
│  │            │                                    │                  │   │
│  │            └────────────┬───────────────────────┘                  │   │
│  │                         │                                          │   │
│  │                         ▼                                          │   │
│  │              ┌──────────────────────────┐                          │   │
│  │              │  Validation Results      │                          │   │
│  │              │  ─────────────────       │                          │   │
│  │              │  - errors: List<String>  │                          │   │
│  │              │  - warnings: List<String>│                          │   │
│  │              └──────────────────────────┘                          │   │
│  └────────────────────────────────────────────────────────────────────┘   │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
```

## Mock Model Classes

### Type Hierarchy

```
MockNamespaceElement (interface)
├── name: String
├── namespace: String
└── fqName(): String

MockPrimitive implements MockNamespaceElement
├── primitiveType: PrimitiveType
├── measuredType: MockMeasure (optional)
└── isX() methods delegating to primitiveType

MockEnumeration extends MockPrimitive
└── members: Set<String>

MockEntityType implements MockNamespaceElement
├── attributes: Map<String, MockAttribute>
├── references: Map<String, MockReference>
├── superTypes: List<MockEntityType>
└── getAllAttributes(): includes inherited

MockAttribute
├── name: String
├── type: MockPrimitive
├── derived: boolean
└── getter/setter/default: String (optional)

MockReference
├── name: String
├── target: MockEntityType
├── collection: boolean
├── derived: boolean
└── getter/setter/range: String (optional)
```

### Measure Classes

```
MockMeasure
├── namespace: String
├── name: String
├── units: List<MockUnit>
├── baseMeasure: boolean
└── baseMeasures: Map<MockMeasure, Integer> (for derived)

MockUnit
├── name: String
├── symbol: String
├── rateDividend: BigDecimal
├── rateDivisor: BigDecimal
└── durationSupportsAddition: boolean
```

## MockModelAdapter Implementation

### Key Methods and Their Implementation Strategy

#### Type Resolution

```java
@Override
public Optional<TypeName> buildTypeName(MockNamespaceElement element) {
    // Create TypeName from element's namespace and name
    TypeName typeName = ExpressionBuilders.newTypeNameBuilder()
        .withNamespace(element.getNamespace())
        .withName(element.getName())
        .build();
    return Optional.of(typeName);
}

@Override
public Optional<? extends MockNamespaceElement> get(TypeName typeName) {
    // Look up in type registry by namespace::name
    String key = typeName.getNamespace() + "::" + typeName.getName();
    return Optional.ofNullable(typeRegistry.get(key));
}
```

#### Type Checking

```java
@Override
public boolean isNumeric(MockPrimitive primitive) {
    return primitive.getPrimitiveType() == PrimitiveType.INTEGER 
        || primitive.getPrimitiveType() == PrimitiveType.DECIMAL;
}

@Override
public boolean isInteger(MockPrimitive primitive) {
    return primitive.getPrimitiveType() == PrimitiveType.INTEGER;
}

// Similar for isDecimal, isBoolean, isString, isDate, isTimestamp, isTime, isCustom
```

#### Attribute/Reference Lookup

```java
@Override
public Optional<? extends MockAttribute> getAttribute(MockEntityType clazz, String name) {
    // Check local attributes first, then inherited
    MockAttribute attr = clazz.getAttributes().get(name);
    if (attr != null) return Optional.of(attr);
    
    for (MockEntityType superType : clazz.getSuperTypes()) {
        Optional<? extends MockAttribute> inherited = getAttribute(superType, name);
        if (inherited.isPresent()) return inherited;
    }
    return Optional.empty();
}
```

#### Measure Methods

```java
@Override
public boolean isMeasured(NumericExpression expr) {
    // Delegate to MeasureAdapter
    return measureAdapter.isMeasured(expr);
}

@Override
public Optional<MockMeasure> getMeasure(NumericExpression expr) {
    return measureAdapter.getMeasure(expr);
}

@Override
public Optional<MockUnit> getUnit(NumericExpression expr) {
    // Extract unit from expression (MeasuredDecimal, NumericAttribute, etc.)
    if (expr instanceof MeasuredDecimal) {
        MeasuredDecimal md = (MeasuredDecimal) expr;
        return measureProvider.getUnitByNameOrSymbol(
            Optional.empty(), 
            md.getUnitName()
        );
    }
    // Handle other expression types...
}
```

## MockModelBuilder Fluent API

```java
public class MockModelBuilder {
    private Map<String, MockNamespaceElement> types = new HashMap<>();
    private List<MockMeasure> measures = new ArrayList<>();
    
    public MockModelBuilder withPrimitive(String name, PrimitiveType type) {
        MockPrimitive primitive = new MockPrimitive(name, type);
        types.put(name, primitive);
        return this;
    }
    
    public MockModelBuilder withEntity(String name) {
        MockEntityType entity = new MockEntityType(name);
        types.put(name, entity);
        return this;
    }
    
    public MockModelBuilder withAttribute(String entityName, String attrName, 
                                          String typeName) {
        MockEntityType entity = (MockEntityType) types.get(entityName);
        MockPrimitive type = (MockPrimitive) types.get(typeName);
        entity.addAttribute(new MockAttribute(attrName, type));
        return this;
    }
    
    public MockModelBuilder withMeasure(String namespace, String name, 
                                        boolean isBase, MockUnit... units) {
        MockMeasure measure = new MockMeasure(namespace, name, isBase, 
                                              Arrays.asList(units));
        measures.add(measure);
        return this;
    }
    
    public MockModelAdapter build() {
        MockMeasureProvider measureProvider = new MockMeasureProvider(measures);
        return new MockModelAdapter(types, measureProvider);
    }
}
```

## Test Pattern Design

### Parameterized Test Structure

```java
public class NumericExpressionValidationTest extends AbstractExpressionValidationTest {

    private MockModelBuilder modelBuilder;
    
    @BeforeEach
    void setup() {
        modelBuilder = new MockModelBuilder()
            .withPrimitive("Integer", PrimitiveType.INTEGER)
            .withPrimitive("Decimal", PrimitiveType.DECIMAL)
            .withPrimitive("String", PrimitiveType.STRING)
            .withEntity("Customer")
            .withAttribute("Customer", "age", "Integer")
            .withAttribute("Customer", "name", "String");
    }

    @ParameterizedTest(name = "[{0}] {1}")
    @MethodSource("integerAttributeTestCases")
    void testIntegerAttribute(ValidatorType type, String scenario, 
                              Consumer<ExpressionModel> modelSetup,
                              List<String> expectedErrors) throws Exception {
        this.validatorType = type;
        initModel();
        
        MockModelAdapter adapter = modelBuilder.build();
        setModelAdapter(adapter);
        
        modelSetup.accept(expressionModel);
        
        runValidation(expectedErrors, Collections.emptyList());
    }
    
    static Stream<Arguments> integerAttributeTestCases() {
        return Stream.of(
            // Positive: integer attribute with integer type passes
            Arguments.of(ValidatorType.EVL, 
                "IntegerAttribute_ValidIntegerType_Passes",
                (Consumer<ExpressionModel>) model -> {
                    TypeName customerType = newTypeNameBuilder()
                        .withNamespace("test")
                        .withName("Customer")
                        .build();
                    ObjectVariableReference objRef = newObjectVariableReferenceBuilder()
                        .withVariableName(newInstanceBuilder()
                            .withElementName(customerType)
                            .build())
                        .build();
                    IntegerAttribute attr = newIntegerAttributeBuilder()
                        .withObjectExpression(objRef)
                        .withAttributeName("age")
                        .build();
                    model.addContent(customerType);
                    model.addContent(attr);
                },
                Collections.emptyList()),
                
            Arguments.of(ValidatorType.JAVA, 
                "IntegerAttribute_ValidIntegerType_Passes",
                /* same setup */,
                Collections.emptyList()),
                
            // Negative: integer attribute with string type fails
            Arguments.of(ValidatorType.EVL,
                "IntegerAttribute_StringType_Fails",
                (Consumer<ExpressionModel>) model -> {
                    // Setup IntegerAttribute pointing to "name" (String type)
                    // ...
                },
                List.of("AttributeTypeIsInteger")),
                
            Arguments.of(ValidatorType.JAVA,
                "IntegerAttribute_StringType_Fails",
                /* same setup */,
                List.of("AttributeTypeIsInteger"))
        );
    }
}
```

### Test Data Organization

For maintainability, test data can be externalized:

```java
public class ValidationTestData {
    
    public static class IntegerAttributeTests {
        public static final TestCase VALID_INTEGER_TYPE = TestCase.builder()
            .name("ValidIntegerType")
            .setup(model -> { /* setup valid integer attribute */ })
            .expectedErrors(Collections.emptyList())
            .build();
            
        public static final TestCase INVALID_STRING_TYPE = TestCase.builder()
            .name("InvalidStringType")
            .setup(model -> { /* setup integer attr pointing to string */ })
            .expectedErrors(List.of("AttributeTypeIsInteger"))
            .build();
    }
}
```

## Trade-offs and Decisions

### 1. Mock vs Real Model

**Decision**: Use mock models instead of real ASM/PSM models.

**Rationale**:
- Faster test execution (no model loading overhead)
- Easier to construct specific test scenarios
- No dependency on external model files
- Clear documentation of what's being tested

**Trade-off**: May miss integration issues with real models. Mitigate by keeping separate integration tests.

### 2. Parameterized Tests with Both Validators

**Decision**: Run every test case with both EVL and Java validators.

**Rationale**:
- Ensures parity between implementations
- Catches regression in either validator
- Single source of truth for expected behavior

**Trade-off**: Doubles test execution time. Mitigate by parallel execution.

### 3. Constraint Name-Based Matching

**Decision**: Match validation results by constraint name only, not full error message.

**Rationale**:
- Error message formats differ between EVL and Java
- Constraint names are stable identifiers
- Easier to maintain as messages evolve

**Trade-off**: Cannot verify exact error messages. Add separate message verification if needed.

### 4. MockMeasureProvider Integration

**Decision**: Create separate MockMeasureProvider rather than mocking MeasureAdapter.

**Rationale**:
- MeasureAdapter has complex dimension calculation logic
- MockMeasureProvider allows testing measure-related validations
- Reuses existing MeasureAdapter implementation

**Trade-off**: More code to maintain. Worth it for proper measure testing.

## File Organization

```
model-test/src/test/java/hu/blackbelt/judo/meta/expression/
├── AbstractExpressionValidationTest.java  (existing)
├── ValidatorType.java                     (existing)
├── validation/
│   ├── mock/
│   │   ├── MockNamespaceElement.java
│   │   ├── MockPrimitive.java
│   │   ├── MockEnumeration.java
│   │   ├── MockEntityType.java
│   │   ├── MockAttribute.java
│   │   ├── MockReference.java
│   │   ├── MockTransferObject.java
│   │   ├── MockTransferAttribute.java
│   │   ├── MockTransferRelation.java
│   │   ├── MockSequence.java
│   │   ├── MockMeasure.java
│   │   ├── MockUnit.java
│   │   ├── MockMeasureProvider.java
│   │   ├── MockModelAdapter.java
│   │   ├── MockModelBuilder.java
│   │   └── PrimitiveType.java
│   └── tests/
│       ├── TypeNameValidationTest.java
│       ├── NumericExpressionValidationTest.java
│       ├── LogicalExpressionValidationTest.java
│       ├── StringExpressionValidationTest.java
│       ├── TemporalExpressionValidationTest.java
│       ├── MeasuredExpressionValidationTest.java
│       ├── EnumerationExpressionValidationTest.java
│       ├── CustomExpressionValidationTest.java
│       ├── ObjectExpressionValidationTest.java
│       ├── CollectionExpressionValidationTest.java
│       ├── BindingValidationTest.java
│       └── ConstantValidationTest.java
```
