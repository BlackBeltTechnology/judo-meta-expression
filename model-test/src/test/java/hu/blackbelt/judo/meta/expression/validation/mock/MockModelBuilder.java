package hu.blackbelt.judo.meta.expression.validation.mock;

import java.util.HashMap;
import java.util.Map;

/**
 * Fluent builder for creating mock models for testing.
 * Provides a convenient API to create complex model structures.
 */
public class MockModelBuilder {

    private final MockModelAdapter adapter;
    private final Map<String, MockPrimitive> primitiveCache = new HashMap<>();
    private final Map<String, MockEnumeration> enumerationCache = new HashMap<>();
    private final Map<String, MockEntityType> entityCache = new HashMap<>();
    private final Map<String, MockTransferObject> transferObjectCache = new HashMap<>();
    private final Map<String, MockMeasure> measureCache = new HashMap<>();
    private final Map<String, MockUnit> unitCache = new HashMap<>();

    private MockModelBuilder() {
        this.adapter = new MockModelAdapter();
    }

    public static MockModelBuilder create() {
        return new MockModelBuilder();
    }

    /**
     * Build and return the configured MockModelAdapter.
     */
    public MockModelAdapter build() {
        return adapter;
    }

    // ========== Primitive Types ==========

    public MockModelBuilder withIntegerType(String name) {
        return withIntegerType(name, null);
    }

    public MockModelBuilder withIntegerType(String name, String namespace) {
        MockPrimitive primitive = new MockPrimitive(name, namespace, PrimitiveType.INTEGER);
        adapter.addPrimitive(primitive);
        primitiveCache.put(primitive.getFqName(), primitive);
        return this;
    }

    public MockModelBuilder withDecimalType(String name) {
        return withDecimalType(name, null);
    }

    public MockModelBuilder withDecimalType(String name, String namespace) {
        MockPrimitive primitive = new MockPrimitive(name, namespace, PrimitiveType.DECIMAL);
        adapter.addPrimitive(primitive);
        primitiveCache.put(primitive.getFqName(), primitive);
        return this;
    }

    public MockModelBuilder withBooleanType(String name) {
        return withBooleanType(name, null);
    }

    public MockModelBuilder withBooleanType(String name, String namespace) {
        MockPrimitive primitive = new MockPrimitive(name, namespace, PrimitiveType.BOOLEAN);
        adapter.addPrimitive(primitive);
        primitiveCache.put(primitive.getFqName(), primitive);
        return this;
    }

    public MockModelBuilder withStringType(String name) {
        return withStringType(name, null);
    }

    public MockModelBuilder withStringType(String name, String namespace) {
        MockPrimitive primitive = new MockPrimitive(name, namespace, PrimitiveType.STRING);
        adapter.addPrimitive(primitive);
        primitiveCache.put(primitive.getFqName(), primitive);
        return this;
    }

    public MockModelBuilder withDateType(String name) {
        return withDateType(name, null);
    }

    public MockModelBuilder withDateType(String name, String namespace) {
        MockPrimitive primitive = new MockPrimitive(name, namespace, PrimitiveType.DATE);
        adapter.addPrimitive(primitive);
        primitiveCache.put(primitive.getFqName(), primitive);
        return this;
    }

    public MockModelBuilder withTimestampType(String name) {
        return withTimestampType(name, null);
    }

    public MockModelBuilder withTimestampType(String name, String namespace) {
        MockPrimitive primitive = new MockPrimitive(name, namespace, PrimitiveType.TIMESTAMP);
        adapter.addPrimitive(primitive);
        primitiveCache.put(primitive.getFqName(), primitive);
        return this;
    }

    public MockModelBuilder withTimeType(String name) {
        return withTimeType(name, null);
    }

    public MockModelBuilder withTimeType(String name, String namespace) {
        MockPrimitive primitive = new MockPrimitive(name, namespace, PrimitiveType.TIME);
        adapter.addPrimitive(primitive);
        primitiveCache.put(primitive.getFqName(), primitive);
        return this;
    }

    public MockModelBuilder withCustomType(String name) {
        return withCustomType(name, null);
    }

    public MockModelBuilder withCustomType(String name, String namespace) {
        MockPrimitive primitive = new MockPrimitive(name, namespace, PrimitiveType.CUSTOM);
        adapter.addPrimitive(primitive);
        primitiveCache.put(primitive.getFqName(), primitive);
        return this;
    }

    public MockModelBuilder withMeasuredType(String name, String measureName, String unitName) {
        return withMeasuredType(name, null, measureName, unitName);
    }

    public MockModelBuilder withMeasuredType(String name, String namespace, String measureName, String unitName) {
        MockMeasure measure = measureCache.get(measureName);
        MockUnit unit = unitName != null ? unitCache.get(unitName) : null;
        MockPrimitive primitive = new MockPrimitive(name, namespace, PrimitiveType.DECIMAL, measure, unit);
        adapter.addPrimitive(primitive);
        primitiveCache.put(primitive.getFqName(), primitive);
        return this;
    }

    // ========== Enumeration ==========

    public EnumerationBuilder withEnumeration(String name) {
        return new EnumerationBuilder(this, name, null);
    }

    public EnumerationBuilder withEnumeration(String name, String namespace) {
        return new EnumerationBuilder(this, name, namespace);
    }

    public static class EnumerationBuilder {
        private final MockModelBuilder parent;
        private final MockEnumeration enumeration;

        EnumerationBuilder(MockModelBuilder parent, String name, String namespace) {
            this.parent = parent;
            this.enumeration = new MockEnumeration(name, namespace);
        }

        public EnumerationBuilder withMember(String member) {
            enumeration.addMember(member);
            return this;
        }

        public EnumerationBuilder withMembers(String... members) {
            for (String member : members) {
                enumeration.addMember(member);
            }
            return this;
        }

        public MockModelBuilder end() {
            parent.adapter.addEnumeration(enumeration);
            parent.enumerationCache.put(enumeration.getFqName(), enumeration);
            parent.primitiveCache.put(enumeration.getFqName(), enumeration);
            return parent;
        }
    }

    // ========== Entity Types ==========

    public EntityTypeBuilder withEntityType(String name) {
        return new EntityTypeBuilder(this, name, null);
    }

    public EntityTypeBuilder withEntityType(String name, String namespace) {
        return new EntityTypeBuilder(this, name, namespace);
    }

    public static class EntityTypeBuilder {
        private final MockModelBuilder parent;
        private final MockEntityType entityType;

        EntityTypeBuilder(MockModelBuilder parent, String name, String namespace) {
            this.parent = parent;
            this.entityType = new MockEntityType(name, namespace);
        }

        public EntityTypeBuilder withSuperType(String superTypeName) {
            MockEntityType superType = parent.entityCache.get(superTypeName);
            if (superType != null) {
                entityType.addSuperType(superType);
            }
            return this;
        }

        public EntityTypeBuilder withAttribute(String name, String typeName) {
            MockPrimitive type = parent.primitiveCache.get(typeName);
            if (type != null) {
                MockAttribute attr = new MockAttribute(name, type);
                entityType.addAttribute(attr);
            }
            return this;
        }

        public EntityTypeBuilder withDerivedAttribute(String name, String typeName, String getter) {
            MockPrimitive type = parent.primitiveCache.get(typeName);
            if (type != null) {
                MockAttribute attr = new MockAttribute(name, type, true, getter, null, null);
                entityType.addAttribute(attr);
            }
            return this;
        }

        public EntityTypeBuilder withAttribute(String name, String typeName, boolean derived, 
                                                String getter, String setter, String defaultValue) {
            MockPrimitive type = parent.primitiveCache.get(typeName);
            if (type != null) {
                MockAttribute attr = new MockAttribute(name, type, derived, getter, setter, defaultValue);
                entityType.addAttribute(attr);
            }
            return this;
        }

        public EntityTypeBuilder withReference(String name, String targetName, boolean collection) {
            MockEntityType target = parent.entityCache.get(targetName);
            if (target != null) {
                MockReference ref = new MockReference(name, target, collection);
                entityType.addReference(ref);
            }
            return this;
        }

        public EntityTypeBuilder withDerivedReference(String name, String targetName, boolean collection, String getter) {
            MockEntityType target = parent.entityCache.get(targetName);
            if (target != null) {
                MockReference ref = new MockReference(name, target, collection, true, getter, null, null, null);
                entityType.addReference(ref);
            }
            return this;
        }

        public EntityTypeBuilder withReference(String name, String targetName, boolean collection,
                                                boolean derived, String getter, String setter, 
                                                String defaultValue, String range) {
            MockEntityType target = parent.entityCache.get(targetName);
            if (target != null) {
                MockReference ref = new MockReference(name, target, collection, derived, getter, setter, defaultValue, range);
                entityType.addReference(ref);
            }
            return this;
        }

        /**
         * Add a self-reference (reference to self, useful for recursive structures)
         */
        public EntityTypeBuilder withSelfReference(String name, boolean collection) {
            MockReference ref = new MockReference(name, entityType, collection);
            entityType.addReference(ref);
            return this;
        }

        public MockModelBuilder end() {
            parent.adapter.addEntityType(entityType);
            parent.entityCache.put(entityType.getFqName(), entityType);
            return parent;
        }

        /**
         * End and return the created entity type for further reference.
         */
        public MockEntityType endAndGet() {
            parent.adapter.addEntityType(entityType);
            parent.entityCache.put(entityType.getFqName(), entityType);
            return entityType;
        }
    }

    // ========== Transfer Objects ==========

    public TransferObjectBuilder withTransferObject(String name) {
        return new TransferObjectBuilder(this, name, null, null);
    }

    public TransferObjectBuilder withTransferObject(String name, String entityTypeName) {
        MockEntityType entityType = entityCache.get(entityTypeName);
        return new TransferObjectBuilder(this, name, null, entityType);
    }

    public TransferObjectBuilder withTransferObject(String name, String namespace, String entityTypeName) {
        MockEntityType entityType = entityTypeName != null ? entityCache.get(entityTypeName) : null;
        return new TransferObjectBuilder(this, name, namespace, entityType);
    }

    public static class TransferObjectBuilder {
        private final MockModelBuilder parent;
        private final MockTransferObject transferObject;

        TransferObjectBuilder(MockModelBuilder parent, String name, String namespace, MockEntityType entityType) {
            this.parent = parent;
            this.transferObject = new MockTransferObject(name, namespace, entityType);
        }

        public TransferObjectBuilder withAttribute(String name, String typeName) {
            MockPrimitive type = parent.primitiveCache.get(typeName);
            if (type != null) {
                MockTransferAttribute attr = new MockTransferAttribute(name, type);
                transferObject.addAttribute(attr);
            }
            return this;
        }

        public TransferObjectBuilder withRelation(String name, String targetName, boolean collection) {
            MockTransferObject target = parent.transferObjectCache.get(targetName);
            if (target != null) {
                MockTransferRelation rel = new MockTransferRelation(name, target, collection);
                transferObject.addRelation(rel);
            }
            return this;
        }

        public TransferObjectBuilder withSuperType(String superTypeName) {
            MockTransferObject superType = parent.transferObjectCache.get(superTypeName);
            if (superType != null) {
                transferObject.addSuperType(superType);
            }
            return this;
        }

        public MockModelBuilder end() {
            parent.adapter.addTransferObject(transferObject);
            parent.transferObjectCache.put(transferObject.getFqName(), transferObject);
            return parent;
        }
    }

    // ========== Measures and Units ==========

    public MeasureBuilder withMeasure(String name) {
        return new MeasureBuilder(this, name, null);
    }

    public MeasureBuilder withMeasure(String name, String namespace) {
        return new MeasureBuilder(this, name, namespace);
    }

    public static class MeasureBuilder {
        private final MockModelBuilder parent;
        private final MockMeasure measure;

        MeasureBuilder(MockModelBuilder parent, String name, String namespace) {
            this.parent = parent;
            this.measure = new MockMeasure(name, namespace);
        }

        public MeasureBuilder withUnit(String name, String symbol) {
            MockUnit unit = new MockUnit(name, null, symbol, false);
            measure.addUnit(unit);
            parent.unitCache.put(name, unit);
            return this;
        }

        public MeasureBuilder withDurationUnit(String name, String symbol) {
            MockUnit unit = MockUnit.durationUnit(name, symbol);
            measure.addUnit(unit);
            parent.unitCache.put(name, unit);
            return this;
        }

        public MeasureBuilder withBaseMeasure(String baseMeasureName, int exponent) {
            MockMeasure baseMeasure = parent.measureCache.get(baseMeasureName);
            if (baseMeasure != null) {
                measure.addBaseMeasure(baseMeasure, exponent);
            }
            return this;
        }

        public MockModelBuilder end() {
            parent.adapter.addMeasure(measure);
            parent.measureCache.put(measure.getFqName(), measure);
            return parent;
        }
    }

    // ========== Sequences ==========

    public MockModelBuilder withSequence(String name) {
        return withSequence(name, null);
    }

    public MockModelBuilder withSequence(String name, String namespace) {
        MockSequence sequence = new MockSequence(name, namespace);
        adapter.addSequence(sequence);
        return this;
    }

    // ========== Container Types ==========

    public MockModelBuilder withContainerRelation(String containedName, String containerName) {
        MockEntityType contained = entityCache.get(containedName);
        MockEntityType container = entityCache.get(containerName);
        if (contained != null && container != null) {
            adapter.addContainerType(contained, container);
        }
        return this;
    }

    // ========== Utility Methods ==========

    /**
     * Get a primitive type by name from the cache.
     */
    public MockPrimitive getPrimitive(String name) {
        return primitiveCache.get(name);
    }

    /**
     * Get an enumeration by name from the cache.
     */
    public MockEnumeration getEnumeration(String name) {
        return enumerationCache.get(name);
    }

    /**
     * Get an entity type by name from the cache.
     */
    public MockEntityType getEntityType(String name) {
        return entityCache.get(name);
    }

    /**
     * Get a transfer object by name from the cache.
     */
    public MockTransferObject getTransferObject(String name) {
        return transferObjectCache.get(name);
    }

    /**
     * Get a measure by name from the cache.
     */
    public MockMeasure getMeasure(String name) {
        return measureCache.get(name);
    }

    /**
     * Get a unit by name from the cache.
     */
    public MockUnit getUnit(String name) {
        return unitCache.get(name);
    }

    // ========== Convenience Methods for Common Patterns ==========

    /**
     * Add standard primitive types (Integer, Decimal, Boolean, String, Date, Timestamp, Time).
     */
    public MockModelBuilder withStandardPrimitives() {
        return withIntegerType("Integer")
                .withDecimalType("Decimal")
                .withBooleanType("Boolean")
                .withStringType("String")
                .withDateType("Date")
                .withTimestampType("Timestamp")
                .withTimeType("Time");
    }

    /**
     * Add standard duration measure with common time units.
     */
    public MockModelBuilder withStandardDurationMeasure() {
        return withMeasure("Duration")
                .withDurationUnit("millisecond", "ms")
                .withDurationUnit("second", "s")
                .withDurationUnit("minute", "min")
                .withDurationUnit("hour", "h")
                .withDurationUnit("day", "d")
                .end();
    }

    /**
     * Add a simple entity with id and name attributes.
     */
    public MockModelBuilder withSimpleEntity(String name) {
        return withEntityType(name)
                .withAttribute("id", "Integer")
                .withAttribute("name", "String")
                .end();
    }
}
