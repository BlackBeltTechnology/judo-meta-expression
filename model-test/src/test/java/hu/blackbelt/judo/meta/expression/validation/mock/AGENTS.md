# AGENTS.md — `model-test/src/test/java/hu/blackbelt/judo/meta/expression/validation/mock`

Mock domain model fixtures and parameterized validation tests executing EVL/Zeta rules against synthetic types.

| File | Purpose |
| --- | --- |
| `AbstractMockExpressionValidationTest.java` | Base test harness executing EVL or Zeta expression validation against mock model contexts. Exports `AbstractMockExpressionValidationTest`, `initModel()`, `setMockModelAdapter()`, `runValidation()`, `createStandardMockAdapter()`, `createStandardMockBuilder()`. Requires configured `ValidatorType`. |
| `ConstantExpressionValidationTest.java` | Parameterized validation tests for constant expressions across EVL and Zeta validators. Verifies type compatibility for integer, decimal, string, boolean, literal, null, and measured decimal constants. |
| `EnumerationExpressionValidationTest.java` | Parameterized validation tests for enumeration attributes, literals, and switch expressions across EVL and Zeta validators. Verifies member resolution and branch completeness. |
| `LogicalExpressionValidationTest.java` | Parameterized validation tests for logical comparisons, boolean attributes, and Kleene logic across EVL and Zeta validators. Verifies numeric and decimal comparisons. |
| `MockAttribute.java` | Mutable test attribute descriptor representing entity type attributes. Exports `MockAttribute`, `getName()`, `getType()`, `isDerived()`, `getGetter()`, `getSetter()`, `getDefaultValue()`. |
| `MockEntityType.java` | Mock entity type containing attributes, references, supertypes, and container types. Exports `MockEntityType`, `addAttribute()`, `addReference()`, `addSuperType()`, `isSubTypeOf()`. |
| `MockEnumeration.java` | Mock enumeration type holding literal member names. Exports `MockEnumeration`, `getMembers()`, `addMember()`, `containsMember()`. Extends `MockPrimitive` under `PrimitiveType.ENUMERATION`. |
| `MockEvlModelContext.java` | EMF resource context provider for EVL script execution during unit tests. Exports `MockEvlModelContext`, `createEmptyResource()`. Configures `ResourceSet` with XMI resource factory. |
| `MockMeasure.java` | Mock measurement unit descriptor holding units and base measure relationships. Exports `MockMeasure`, `addUnit()`, `addBaseMeasure()`, `getUnits()`, `isBaseMeasure()`, `getUnitByNameOrSymbol()`. |
| `MockMeasureProvider.java` | Mock implementation of `MeasureProvider` supplying measures and units to expression evaluators. Exports `MockMeasureProvider`, `addMeasure()`, `getMeasure()`, `getUnits()`, `isDurationSupportingAddition()`. |
| `MockModelAdapter.java` | Test implementation of `ModelAdapter` resolving mock types, attributes, references, and measures for expression validation. Exports `MockModelAdapter`, `addEntityType()`, `addPrimitive()`, `buildTypeName()`. |
| `MockModelBuilder.java` | Fluent builder constructing connected `MockModelAdapter` instances with primitives, entities, transfer objects, and measures. Exports `MockModelBuilder`, `create()`, `build()`, `withEntityType()`. |
| `MockNamespaceElement.java` | Common interface for namespace-qualified mock domain model elements. Exports `MockNamespaceElement`, `getName()`, `getNamespace()`, default `getFqName()`. |
| `MockPrimitive.java` | Mock primitive and scalar type descriptor supporting measured, temporal, and custom types. Exports `MockPrimitive`, `getPrimitiveType()`, `getMeasure()`, `getUnit()`, `isNumeric()`, `isMeasured()`. |
| `MockReference.java` | Mock entity reference descriptor representing associations with cardinality and derivation status. Exports `MockReference`, `getTarget()`, `isCollection()`, `isDerived()`, `getRange()`. |
| `MockSequence.java` | Mock sequence generator representation. Exports `MockSequence`, `getName()`, `getNamespace()`. Implements `MockNamespaceElement`. |
| `MockTransferAttribute.java` | Mock transfer object attribute carrying binding to entity attributes. Exports `MockTransferAttribute`, `getType()`, `getBinding()`, `hasBinding()`. |
| `MockTransferObject.java` | Mock transfer object type containing transfer attributes, relations, and mapped entity types. Exports `MockTransferObject`, `getEntityType()`, `addAttribute()`, `addRelation()`, `getAllAttributes()`. |
| `MockTransferRelation.java` | Mock transfer relation linking transfer objects with cardinality and entity reference bindings. Exports `MockTransferRelation`, `getTarget()`, `getBinding()`, `isCollection()`. |
| `MockUnit.java` | Mock unit of measurement carrying symbol and duration flags. Exports `MockUnit`, `getName()`, `getNamespace()`, `getSymbol()`, `isDurationSupportingAddition()`. |
| `NumericExpressionValidationTest.java` | Parameterized validation tests for integer and decimal arithmetic, attributes, and switch expressions across EVL and Zeta validators. Verifies type alignment in arithmetic operations. |
| `PrimitiveType.java` | Enumeration defining supported mock primitive types. Exports `PrimitiveType` enum constants `INTEGER`, `DECIMAL`, `BOOLEAN`, `STRING`, `DATE`, `TIMESTAMP`, `TIME`, `CUSTOM`, `ENUMERATION`. |
| `StringExpressionValidationTest.java` | Parameterized validation tests for string concatenation, case transformation, trimming, attributes, and switches across EVL and Zeta validators. |
| `TemporalExpressionValidationTest.java` | Parameterized validation tests for date, time, and timestamp attributes and switch expressions across EVL and Zeta validators. |
