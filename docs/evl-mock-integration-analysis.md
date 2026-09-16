# EVL Mock Integration Analysis

## Overview

This document analyzes the feasibility of running EVL (Epsilon Validation Language) validation with mock models instead of full EMF models.

## Current Architecture

### EVL Validation Flow
```
EVL Script (expression.evl)
    ↓
ExecutionContext (Epsilon Runtime)
    ↓
ModelContext (WrappedEmfModelContext)
    ↓
IModel (InMemoryEmfModel)
    ↓
EMF Resource / EObjects
```

### Key Components

1. **EVL Scripts** - Declarative validation rules in `.evl` files
2. **ExecutionContext** - Epsilon's script execution engine
3. **ModelContext** - Adapter interface for model access
4. **IModel** - Epsilon's model abstraction (getAllOfKind, getAllOfType, etc.)
5. **EMF Resource** - Actual model storage with EObjects

## EVL Script Dependencies

Analyzing `expression.evl` and related files, EVL scripts depend on:

### 1. Type Navigation
```evl
context EXPR!EnumerationAttribute {
    // EXPR! prefix requires IModel.getAllOfKind("EnumerationAttribute")
}
```

### 2. Property Access
```evl
self.attributeName          // EAttribute access
self.objectExpression       // EReference navigation
self.defaultExpression      // Containment reference
```

### 3. Type Checking
```evl
self.isKindOf(EXPR!EnumerationExpression)  // instanceof check
self.isTypeOf(EXPR!Literal)                // exact type check
```

### 4. Method Invocation
```evl
self.getAttributeType(modelAdapter)        // Call Java method on EMF object
self.satisfiesAll("Resolved")              // Constraint dependency
```

### 5. Injected Services
```evl
modelAdapter.isEnumeration(...)            // Injected Java object
evaluator.evaluate(...)                    // Injected Java object
```

## Options for Mock Integration

### Option 1: Create Mock EMF EObjects (Complex)

**Approach**: Create EObject implementations that wrap MockModelAdapter data.

**Pros**:
- Full compatibility with EVL scripts
- No changes to EVL scripts needed

**Cons**:
- Requires implementing EObject, EClass, EAttribute, EReference
- Must implement EMF notification/adapter framework
- Significant development effort (weeks)
- Essentially reimplementing EMF

**Feasibility**: Low - too much effort for test infrastructure

### Option 2: Custom IModel Implementation (Moderate)

**Approach**: Implement Epsilon's `IModel` interface backed by mock data.

```java
public class MockEpsilonModel implements IModel {
    private MockModelAdapter mockAdapter;
    private ExpressionModel expressionModel;
    
    @Override
    public Collection<?> getAllOfKind(String kind) {
        // Return expression model elements by type
        return expressionModel.getResource().getContents().stream()
            .filter(e -> isKindOf(e, kind))
            .collect(toList());
    }
    
    @Override
    public Object getPropertyValue(Object object, String property) {
        // Use EMF reflection on expression EObjects
        EObject eObj = (EObject) object;
        EStructuralFeature feature = eObj.eClass().getEStructuralFeature(property);
        return eObj.eGet(feature);
    }
}
```

**Pros**:
- Expression model elements ARE real EMF EObjects
- Only need to handle the "adapted" model access
- Moderate development effort

**Cons**:
- EVL scripts still call `self.getAttributeType(modelAdapter)` which returns mock types
- Mock types would need to work with EVL's property access
- Need custom ModelContext implementation

**Feasibility**: Medium - possible but needs careful design

### Option 3: Hybrid Approach - Expression EMF + Mock Adapter (Recommended)

**Approach**: Use real EMF for expressions, mock only the ModelAdapter.

The key insight is that:
1. **Expression elements** (IntegerAttribute, StringConstant, etc.) ARE already EMF EObjects
2. **The ModelAdapter** is already injected as a Java service
3. EVL scripts call `modelAdapter.isString(...)` etc. on the injected service

**What's needed**:
1. A mock "adapted" resource that EVL can load (even if empty)
2. The MockModelAdapter already works as injected service
3. The expression model works as-is

**Implementation**:
```java
// Create empty adapted resource for EVL context
Resource emptyAdaptedResource = new ResourceSetImpl()
    .createResource(URI.createURI("mock://adapted"));

// Use existing expression model (has real EObjects)
Resource expressionResource = expressionModel.getResource();

// Inject mock adapter
injections.put("modelAdapter", mockModelAdapter);
```

**Challenge**: When EVL calls `self.getAttributeType(modelAdapter)`, this:
1. Calls Java method on expression EObject (works)
2. Returns `Optional<MockPrimitive>` from MockModelAdapter (works)
3. EVL then tries to access properties on the result

The issue is step 3 - EVL would try `result.get().getName()` but MockPrimitive 
isn't an EObject, so Epsilon's property access fails.

### Option 4: EVL Script Wrapper Functions (Simplest)

**Approach**: Add wrapper operations in EVL that handle mock types.

```evl
// In expression.evl header
operation Any asMockPrimitive() : Any {
    if (self.isKindOf(Native("hu.blackbelt.judo.meta.expression.validation.mock.MockPrimitive"))) {
        return self;
    }
    return self;
}

operation Any getMockName() : String {
    if (self.isKindOf(Native("hu.blackbelt.judo.meta.expression.validation.mock.MockPrimitive"))) {
        return self.asNative().getName();
    }
    return self.name;
}
```

**Pros**:
- Minimal code changes
- EVL scripts mostly unchanged

**Cons**:
- Pollutes EVL scripts with test concerns
- Maintenance overhead

**Feasibility**: Medium - works but not clean

## Recommended Solution: Option 3 with Method Wrapper

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    EVL Execution                         │
├─────────────────────────────────────────────────────────┤
│  ModelContext: WrappedEmfModelContext                   │
│    - EXPR: expressionModel.getResource() (real EMF)     │
│    - ADAPTED: emptyMockResource                         │
├─────────────────────────────────────────────────────────┤
│  Injections:                                            │
│    - modelAdapter: MockModelAdapter                      │
│    - evaluator: ExpressionEvaluator                     │
├─────────────────────────────────────────────────────────┤
│  EVL Script Calls:                                      │
│    self.getAttributeType(modelAdapter)                  │
│         ↓                                               │
│    Calls Java method on Expression EObject               │
│         ↓                                               │
│    Returns Optional<MockPrimitive>                       │
│         ↓                                               │
│    modelAdapter.isEnumeration(result.get())             │
│         ↓                                               │
│    MockModelAdapter.isEnumeration(MockPrimitive)        │
│         ↓                                               │
│    Returns boolean (works!)                             │
└─────────────────────────────────────────────────────────┘
```

### Key Insight

Looking at the EVL constraints more carefully:

```evl
check: modelAdapter.isEnumeration(self.getAttributeType(modelAdapter).get())
```

This works because:
1. `self` is a real EMF EObject (EnumerationAttribute)
2. `self.getAttributeType(modelAdapter)` calls a Java method that returns `Optional<?>`
3. `.get()` returns the mock primitive
4. `modelAdapter.isEnumeration(...)` handles the mock primitive

The EVL script never tries to access properties ON the mock primitive directly!

### Implementation Plan

1. Create `MockEvlModelContext` that provides an empty "adapted" model
2. Use expression model's real EMF resource
3. Inject MockModelAdapter as the `modelAdapter` service
4. Test with actual EVL execution

## Proof of Concept

See `MockEvlValidationTest.java` for implementation.

## Conclusion

EVL mock integration IS feasible because:
1. Expression elements are real EMF EObjects (generated code)
2. MockModelAdapter methods are called via Java injection
3. EVL scripts don't navigate INTO mock objects, they pass them to modelAdapter

The main work is creating the proper `ModelContext` setup for EVL execution with mock adapters.
