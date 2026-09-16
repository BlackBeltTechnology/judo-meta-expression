package hu.blackbelt.judo.meta.expression.validation.mock;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.NumericExpression;
import hu.blackbelt.judo.meta.expression.ReferenceSelector;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock implementation of ModelAdapter for testing validation constraints.
 * This adapter uses in-memory mock model structures instead of EMF-based models.
 */
public class MockModelAdapter implements ModelAdapter<
        MockNamespaceElement,  // NE
        MockPrimitive,         // P
        MockEnumeration,       // E
        MockEntityType,        // C
        MockAttribute,         // PTE
        MockReference,         // RTE
        MockTransferObject,    // TO
        MockTransferAttribute, // TA
        MockTransferRelation,  // TR
        MockSequence,          // S
        MockMeasure,           // M
        MockUnit               // U
        > {

    private final Map<String, MockNamespaceElement> namespaceElements = new LinkedHashMap<>();
    private final Map<String, MockPrimitive> primitives = new LinkedHashMap<>();
    private final Map<String, MockEnumeration> enumerations = new LinkedHashMap<>();
    private final Map<String, MockEntityType> entityTypes = new LinkedHashMap<>();
    private final Map<String, MockTransferObject> transferObjects = new LinkedHashMap<>();
    private final Map<String, MockSequence> sequences = new LinkedHashMap<>();
    private final MockMeasureProvider measureProvider;
    private final Map<MockEntityType, List<MockEntityType>> containerTypes = new LinkedHashMap<>();
    private final Map<MockNamespaceElement, MockTransferObject> actorPrincipals = new LinkedHashMap<>();

    public MockModelAdapter() {
        this.measureProvider = new MockMeasureProvider();
    }

    public MockModelAdapter(MockMeasureProvider measureProvider) {
        this.measureProvider = measureProvider != null ? measureProvider : new MockMeasureProvider();
    }

    // Registration methods for building mock models

    public MockModelAdapter addPrimitive(MockPrimitive primitive) {
        String fqName = primitive.getFqName();
        primitives.put(fqName, primitive);
        namespaceElements.put(fqName, primitive);
        if (primitive instanceof MockEnumeration) {
            enumerations.put(fqName, (MockEnumeration) primitive);
        }
        return this;
    }

    public MockModelAdapter addEnumeration(MockEnumeration enumeration) {
        String fqName = enumeration.getFqName();
        enumerations.put(fqName, enumeration);
        primitives.put(fqName, enumeration);
        namespaceElements.put(fqName, enumeration);
        return this;
    }

    public MockModelAdapter addEntityType(MockEntityType entityType) {
        String fqName = entityType.getFqName();
        entityTypes.put(fqName, entityType);
        namespaceElements.put(fqName, entityType);
        return this;
    }

    public MockModelAdapter addTransferObject(MockTransferObject transferObject) {
        String fqName = transferObject.getFqName();
        transferObjects.put(fqName, transferObject);
        namespaceElements.put(fqName, transferObject);
        return this;
    }

    public MockModelAdapter addSequence(MockSequence sequence) {
        String fqName = sequence.getFqName();
        sequences.put(fqName, sequence);
        namespaceElements.put(fqName, sequence);
        return this;
    }

    public MockModelAdapter addMeasure(MockMeasure measure) {
        measureProvider.addMeasure(measure);
        return this;
    }

    public MockModelAdapter addContainerType(MockEntityType contained, MockEntityType container) {
        containerTypes.computeIfAbsent(contained, k -> new ArrayList<>()).add(container);
        return this;
    }

    public MockModelAdapter addActorPrincipal(MockNamespaceElement actorType, MockTransferObject principal) {
        actorPrincipals.put(actorType, principal);
        return this;
    }

    public MockMeasureProvider getMeasureProvider() {
        return measureProvider;
    }

    // ModelAdapter interface implementation

    @Override
    public Optional<TypeName> buildTypeName(MockNamespaceElement namespaceElement) {
        // Return empty as we don't create TypeName instances in mock
        return Optional.empty();
    }

    @Override
    public Optional<? extends MockNamespaceElement> get(TypeName elementName) {
        if (elementName == null) {
            return Optional.empty();
        }
        String namespace = elementName.getNamespace();
        String name = elementName.getName();
        String fqName = (namespace == null || namespace.isEmpty()) ? name : namespace + "::" + name;
        return Optional.ofNullable(namespaceElements.get(fqName));
    }

    @Override
    public Optional<? extends MockMeasure> get(MeasureName measureName) {
        if (measureName == null) {
            return Optional.empty();
        }
        return measureProvider.getMeasure(measureName.getNamespace(), measureName.getName());
    }

    @Override
    public boolean isObjectType(MockNamespaceElement namespaceElement) {
        return namespaceElement instanceof MockEntityType;
    }

    @Override
    public boolean isPrimitiveType(MockNamespaceElement namespaceElement) {
        return namespaceElement instanceof MockPrimitive;
    }

    @Override
    public boolean isMeasuredType(MockPrimitive primitiveType) {
        return primitiveType.isMeasured();
    }

    @Override
    public Optional<? extends MockMeasure> getMeasureOfType(MockPrimitive primitiveType) {
        return Optional.ofNullable(primitiveType.getMeasure());
    }

    @Override
    public Optional<MockUnit> getUnitOfType(MockPrimitive primitiveType) {
        return Optional.ofNullable(primitiveType.getUnit());
    }

    @Override
    public String getUnitName(MockUnit unit) {
        return unit.getName();
    }

    @Override
    public UnitFraction getUnitRates(MockUnit unit) {
        // Default unit rate of 1/1
        return new UnitFraction(BigDecimal.ONE, BigDecimal.ONE);
    }

    @Override
    public UnitFraction getBaseDurationRatio(MockUnit unit, DurationType targetType) {
        if (targetType == DurationType.SECOND) {
            return targetType.getSecondUnitFraction();
        } else if (targetType == DurationType.DAY) {
            return targetType.getDayUnitFraction();
        }
        return new UnitFraction(BigDecimal.ONE, BigDecimal.ONE);
    }

    @Override
    public Optional<? extends MockReference> getReference(MockEntityType clazz, String referenceName) {
        return clazz.getReference(referenceName);
    }

    @Override
    public Optional<? extends MockTransferRelation> getTransferRelation(MockTransferObject transferObject, String relationName) {
        return transferObject.getRelation(relationName);
    }

    @Override
    public boolean isCollection(ReferenceSelector referenceSelector) {
        // This would need expression model access - return false by default
        return false;
    }

    @Override
    public boolean isCollectionReference(MockReference reference) {
        return reference.isCollection();
    }

    @Override
    public Optional<MockTransferObject> getAttributeParameterType(MockAttribute attribute) {
        return Optional.empty();
    }

    @Override
    public Optional<MockTransferObject> getReferenceParameterType(MockReference reference) {
        return Optional.empty();
    }

    @Override
    public Optional<MockTransferObject> getTransferAttributeParameterType(MockTransferAttribute attribute) {
        return Optional.empty();
    }

    @Override
    public Optional<MockTransferObject> getTransferRelationParameterType(MockTransferRelation reference) {
        return Optional.empty();
    }

    @Override
    public MockEntityType getTarget(MockReference reference) {
        return reference.getTarget();
    }

    @Override
    public MockTransferObject getTransferRelationTarget(MockTransferRelation relation) {
        return relation.getTarget();
    }

    @Override
    public Optional<? extends MockAttribute> getAttribute(MockEntityType clazz, String attributeName) {
        return clazz.getAttribute(attributeName);
    }

    @Override
    public Optional<? extends MockTransferAttribute> getTransferAttribute(MockTransferObject transferObject, String attributeName) {
        return transferObject.getAttribute(attributeName);
    }

    @Override
    public Optional<? extends MockPrimitive> getAttributeType(MockAttribute attribute) {
        return Optional.ofNullable(attribute.getType());
    }

    @Override
    public Optional<? extends MockPrimitive> getAttributeType(MockEntityType clazz, String attributeName) {
        return clazz.getAttribute(attributeName).map(MockAttribute::getType);
    }

    @Override
    public Collection<? extends MockEntityType> getSuperTypes(MockEntityType clazz) {
        return clazz.getSuperTypes();
    }

    @Override
    public boolean isMixin(MockTransferObject included, MockTransferObject mixin) {
        // Check if mixin includes all attributes and relations from included
        return mixin.getAllAttributes().keySet().containsAll(included.getAllAttributes().keySet()) &&
               mixin.getAllRelations().keySet().containsAll(included.getAllRelations().keySet());
    }

    @Override
    public boolean isNumeric(MockPrimitive primitive) {
        return primitive.isInteger() || primitive.isDecimal();
    }

    @Override
    public boolean isInteger(MockPrimitive primitive) {
        return primitive.isInteger();
    }

    @Override
    public boolean isDecimal(MockPrimitive primitive) {
        return primitive.isDecimal();
    }

    @Override
    public boolean isBoolean(MockPrimitive primitive) {
        return primitive.isBoolean();
    }

    @Override
    public boolean isString(MockPrimitive primitive) {
        return primitive.isString();
    }

    @Override
    public boolean isEnumeration(MockPrimitive primitive) {
        return primitive.isEnumeration();
    }

    @Override
    public boolean isDate(MockPrimitive primitive) {
        return primitive.isDate();
    }

    @Override
    public boolean isTimestamp(MockPrimitive primitive) {
        return primitive.isTimestamp();
    }

    @Override
    public boolean isTime(MockPrimitive primitive) {
        return primitive.isTime();
    }

    @Override
    public boolean isCustom(MockPrimitive primitive) {
        return primitive.isCustom();
    }

    @Override
    public boolean isMeasured(NumericExpression numericExpression) {
        // Would need expression analysis - return false by default
        return false;
    }

    @Override
    public boolean contains(MockEnumeration enumeration, String memberName) {
        return enumeration.containsMember(memberName);
    }

    @Override
    public boolean isDurationSupportingAddition(MockUnit unit) {
        return unit.isDurationSupportingAddition();
    }

    @Override
    public Optional<MockMeasure> getMeasure(NumericExpression numericExpression) {
        // Would need expression analysis
        return Optional.empty();
    }

    @Override
    public Optional<MockUnit> getUnit(NumericExpression numericExpression) {
        // Would need expression analysis
        return Optional.empty();
    }

    @Override
    public EList<MockUnit> getUnits(MockMeasure measure) {
        EList<MockUnit> result = new BasicEList<>();
        result.addAll(measure.getUnits());
        return result;
    }

    @Override
    public Optional<Map<MockMeasure, Integer>> getDimension(NumericExpression numericExpression) {
        // Would need expression analysis
        return Optional.empty();
    }

    @Override
    public EList<MockEntityType> getContainerTypesOf(MockEntityType clazz) {
        EList<MockEntityType> result = new BasicEList<>();
        List<MockEntityType> containers = containerTypes.get(clazz);
        if (containers != null) {
            result.addAll(containers);
        }
        return result;
    }

    @Override
    public EList<MockEntityType> getAllEntityTypes() {
        EList<MockEntityType> result = new BasicEList<>();
        result.addAll(entityTypes.values());
        return result;
    }

    @Override
    public EList<MockMeasure> getAllMeasures() {
        EList<MockMeasure> result = new BasicEList<>();
        result.addAll(measureProvider.getAllMeasures());
        return result;
    }

    @Override
    public Optional<MeasureName> buildMeasureName(MockMeasure measure) {
        return Optional.empty();
    }

    @Override
    public EList<MockEnumeration> getAllEnums() {
        EList<MockEnumeration> result = new BasicEList<>();
        result.addAll(enumerations.values());
        return result;
    }

    @Override
    public EList<MockPrimitive> getAllPrimitiveTypes() {
        EList<MockPrimitive> result = new BasicEList<>();
        result.addAll(primitives.values());
        return result;
    }

    @Override
    public EList<MockNamespaceElement> getAllStaticSequences() {
        EList<MockNamespaceElement> result = new BasicEList<>();
        result.addAll(sequences.values());
        return result;
    }

    @Override
    public EList<MockTransferObject> getAllTransferObjectTypes() {
        EList<MockTransferObject> result = new BasicEList<>();
        result.addAll(transferObjects.values());
        return result;
    }

    @Override
    public EList<MockTransferObject> getAllMappedTransferObjectTypes() {
        EList<MockTransferObject> result = new BasicEList<>();
        result.addAll(transferObjects.values().stream()
                .filter(MockTransferObject::hasEntityType)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public EList<MockTransferObject> getAllUnmappedTransferObjectTypes() {
        EList<MockTransferObject> result = new BasicEList<>();
        result.addAll(transferObjects.values().stream()
                .filter(to -> !to.hasEntityType())
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public Optional<? extends MockSequence> getSequence(MockEntityType clazz, String sequenceName) {
        // Sequences are not typically associated with entity types in mock
        return Optional.empty();
    }

    @Override
    public boolean isSequence(MockNamespaceElement namespaceElement) {
        return namespaceElement instanceof MockSequence;
    }

    @Override
    public boolean isDerivedAttribute(MockAttribute attribute) {
        return attribute.isDerived();
    }

    @Override
    public boolean isDerivedTransferAttribute(MockTransferAttribute attribute) {
        return false; // Mock doesn't track derived status for transfer attributes
    }

    @Override
    public Optional<String> getAttributeGetter(MockAttribute attribute) {
        return Optional.ofNullable(attribute.getGetter());
    }

    @Override
    public Optional<String> getTransferAttributeGetter(MockTransferAttribute attribute) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getAttributeSetter(MockAttribute attribute) {
        return Optional.ofNullable(attribute.getSetter());
    }

    @Override
    public Optional<String> getAttributeDefault(MockAttribute attribute) {
        return Optional.ofNullable(attribute.getDefaultValue());
    }

    @Override
    public boolean isDerivedReference(MockReference reference) {
        return reference.isDerived();
    }

    @Override
    public boolean isDerivedTransferRelation(MockTransferRelation relation) {
        return false; // Mock doesn't track derived status for transfer relations
    }

    @Override
    public Optional<String> getReferenceGetter(MockReference reference) {
        return Optional.ofNullable(reference.getGetter());
    }

    @Override
    public Optional<String> getTransferRelationGetter(MockTransferRelation relation) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getReferenceDefault(MockReference reference) {
        return Optional.ofNullable(reference.getDefaultValue());
    }

    @Override
    public Optional<String> getReferenceRange(MockReference reference) {
        return Optional.ofNullable(reference.getRange());
    }

    @Override
    public Optional<String> getReferenceSetter(MockReference reference) {
        return Optional.ofNullable(reference.getSetter());
    }

    @Override
    public Optional<String> getTransferRelationSetter(MockTransferRelation relation) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getFilter(MockTransferObject transferObjectType) {
        return Optional.empty();
    }

    @Override
    public Optional<MockEntityType> getEntityTypeOfTransferObjectRelationTarget(TypeName elementName, String referenceName) {
        return get(elementName)
                .filter(ne -> ne instanceof MockTransferObject)
                .map(ne -> (MockTransferObject) ne)
                .flatMap(to -> to.getRelation(referenceName))
                .map(MockTransferRelation::getTarget)
                .filter(MockTransferObject::hasEntityType)
                .map(MockTransferObject::getEntityType);
    }

    @Override
    public boolean isCollectionReference(TypeName elementName, String referenceName) {
        Optional<? extends MockNamespaceElement> element = get(elementName);
        if (element.isPresent()) {
            MockNamespaceElement ne = element.get();
            if (ne instanceof MockEntityType) {
                return ((MockEntityType) ne).getReference(referenceName)
                        .map(MockReference::isCollection)
                        .orElse(false);
            } else if (ne instanceof MockTransferObject) {
                return ((MockTransferObject) ne).getRelation(referenceName)
                        .map(MockTransferRelation::isCollection)
                        .orElse(false);
            }
        }
        return false;
    }

    @Override
    public Optional<MockEntityType> getMappedEntityType(MockTransferObject mappedTransferObjectType) {
        return Optional.ofNullable(mappedTransferObjectType.getEntityType());
    }

    @Override
    public String getFqName(Object object) {
        if (object instanceof MockNamespaceElement) {
            return ((MockNamespaceElement) object).getFqName();
        }
        return object.toString();
    }

    @Override
    public Optional<String> getName(Object object) {
        if (object instanceof MockNamespaceElement) {
            return Optional.ofNullable(((MockNamespaceElement) object).getName());
        }
        return Optional.empty();
    }

    @Override
    public Collection<? extends MockAttribute> getAttributes(MockEntityType clazz) {
        return clazz.getAttributes().values();
    }

    @Override
    public Collection<? extends MockReference> getReferences(MockEntityType clazz) {
        return clazz.getReferences().values();
    }

    @Override
    public Collection<? extends MockTransferAttribute> getTransferAttributes(MockTransferObject transferObjectType) {
        return transferObjectType.getAttributes().values();
    }

    @Override
    public Collection<? extends MockTransferRelation> getTransferRelations(MockTransferObject transferObjectType) {
        return transferObjectType.getRelations().values();
    }

    @Override
    public MockPrimitive getTransferAttributeType(MockTransferAttribute transferAttribute) {
        return transferAttribute.getType();
    }

    @Override
    public List<MockNamespaceElement> getAllActorTypes() {
        return new ArrayList<>(actorPrincipals.keySet());
    }

    @Override
    public MockTransferObject getPrincipal(MockNamespaceElement actorType) {
        return actorPrincipals.get(actorType);
    }

    /**
     * Clear all registered model elements.
     */
    public void clear() {
        namespaceElements.clear();
        primitives.clear();
        enumerations.clear();
        entityTypes.clear();
        transferObjects.clear();
        sequences.clear();
        measureProvider.clear();
        containerTypes.clear();
        actorPrincipals.clear();
    }
}
