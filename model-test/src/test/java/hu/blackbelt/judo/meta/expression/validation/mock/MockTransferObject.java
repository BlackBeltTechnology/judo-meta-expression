package hu.blackbelt.judo.meta.expression.validation.mock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mock transfer object for testing purposes.
 * This is a stub class as transfer object validation is not the primary focus.
 */
public class MockTransferObject implements MockNamespaceElement {

    private final String name;
    private final String namespace;
    private final MockEntityType entityType;
    private final Map<String, MockTransferAttribute> attributes = new LinkedHashMap<>();
    private final Map<String, MockTransferRelation> relations = new LinkedHashMap<>();
    private final List<MockTransferObject> superTypes = new ArrayList<>();

    public MockTransferObject(String name, String namespace, MockEntityType entityType) {
        this.name = name;
        this.namespace = namespace;
        this.entityType = entityType;
    }

    public MockTransferObject(String name, MockEntityType entityType) {
        this(name, null, entityType);
    }

    public MockTransferObject(String name) {
        this(name, null, null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public MockEntityType getEntityType() {
        return entityType;
    }

    public boolean hasEntityType() {
        return entityType != null;
    }

    public MockTransferObject addAttribute(MockTransferAttribute attribute) {
        attributes.put(attribute.getName(), attribute);
        return this;
    }

    public MockTransferObject addRelation(MockTransferRelation relation) {
        relations.put(relation.getName(), relation);
        return this;
    }

    public MockTransferObject addSuperType(MockTransferObject superType) {
        superTypes.add(superType);
        return this;
    }

    public Optional<MockTransferAttribute> getAttribute(String name) {
        return Optional.ofNullable(attributes.get(name));
    }

    public Optional<MockTransferRelation> getRelation(String name) {
        return Optional.ofNullable(relations.get(name));
    }

    public Map<String, MockTransferAttribute> getAttributes() {
        return new LinkedHashMap<>(attributes);
    }

    public Map<String, MockTransferRelation> getRelations() {
        return new LinkedHashMap<>(relations);
    }

    public List<MockTransferObject> getSuperTypes() {
        return new ArrayList<>(superTypes);
    }

    /**
     * Gets all attributes including inherited ones.
     */
    public Map<String, MockTransferAttribute> getAllAttributes() {
        Map<String, MockTransferAttribute> allAttributes = new LinkedHashMap<>();
        for (MockTransferObject superType : superTypes) {
            allAttributes.putAll(superType.getAllAttributes());
        }
        allAttributes.putAll(attributes);
        return allAttributes;
    }

    /**
     * Gets all relations including inherited ones.
     */
    public Map<String, MockTransferRelation> getAllRelations() {
        Map<String, MockTransferRelation> allRelations = new LinkedHashMap<>();
        for (MockTransferObject superType : superTypes) {
            allRelations.putAll(superType.getAllRelations());
        }
        allRelations.putAll(relations);
        return allRelations;
    }
}
