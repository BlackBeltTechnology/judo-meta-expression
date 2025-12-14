package hu.blackbelt.judo.meta.expression.validation.mock;

/*-
 * #%L
 * Judo :: Expression :: Model :: Test
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.*;

/**
 * Mock entity type representing a class/entity with attributes and references.
 */
public class MockEntityType implements MockNamespaceElement {

    private final String name;
    private final String namespace;
    private final Map<String, MockAttribute> attributes = new LinkedHashMap<>();
    private final Map<String, MockReference> references = new LinkedHashMap<>();
    private final List<MockEntityType> superTypes = new ArrayList<>();
    private final List<MockEntityType> containerTypes = new ArrayList<>();

    public MockEntityType(String name) {
        this(name, null);
    }

    public MockEntityType(String name, String namespace) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.namespace = namespace;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    // Attribute management

    public void addAttribute(MockAttribute attribute) {
        attributes.put(attribute.getName(), attribute);
    }

    public Optional<MockAttribute> getAttribute(String name) {
        MockAttribute attr = attributes.get(name);
        if (attr != null) {
            return Optional.of(attr);
        }
        // Check inherited attributes
        for (MockEntityType superType : superTypes) {
            Optional<MockAttribute> inherited = superType.getAttribute(name);
            if (inherited.isPresent()) {
                return inherited;
            }
        }
        return Optional.empty();
    }

    public Map<String, MockAttribute> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public Collection<MockAttribute> getAllAttributes() {
        Map<String, MockAttribute> allAttrs = new LinkedHashMap<>();
        // Add inherited attributes first
        for (MockEntityType superType : superTypes) {
            for (MockAttribute attr : superType.getAllAttributes()) {
                allAttrs.put(attr.getName(), attr);
            }
        }
        // Add local attributes (override inherited)
        allAttrs.putAll(attributes);
        return allAttrs.values();
    }

    // Reference management

    public void addReference(MockReference reference) {
        references.put(reference.getName(), reference);
    }

    public Optional<MockReference> getReference(String name) {
        MockReference ref = references.get(name);
        if (ref != null) {
            return Optional.of(ref);
        }
        // Check inherited references
        for (MockEntityType superType : superTypes) {
            Optional<MockReference> inherited = superType.getReference(name);
            if (inherited.isPresent()) {
                return inherited;
            }
        }
        return Optional.empty();
    }

    public Map<String, MockReference> getReferences() {
        return Collections.unmodifiableMap(references);
    }

    public Collection<MockReference> getAllReferences() {
        Map<String, MockReference> allRefs = new LinkedHashMap<>();
        // Add inherited references first
        for (MockEntityType superType : superTypes) {
            for (MockReference ref : superType.getAllReferences()) {
                allRefs.put(ref.getName(), ref);
            }
        }
        // Add local references (override inherited)
        allRefs.putAll(references);
        return allRefs.values();
    }

    // Inheritance management

    public void addSuperType(MockEntityType superType) {
        superTypes.add(superType);
    }

    public List<MockEntityType> getSuperTypes() {
        return Collections.unmodifiableList(superTypes);
    }

    public boolean isSubTypeOf(MockEntityType other) {
        if (this.equals(other)) {
            return true;
        }
        for (MockEntityType superType : superTypes) {
            if (superType.isSubTypeOf(other)) {
                return true;
            }
        }
        return false;
    }

    // Container types

    public void addContainerType(MockEntityType containerType) {
        containerTypes.add(containerType);
    }

    public List<MockEntityType> getContainerTypes() {
        return Collections.unmodifiableList(containerTypes);
    }

    @Override
    public String toString() {
        return "MockEntityType{" + getFqName() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MockEntityType that = (MockEntityType) o;
        return Objects.equals(name, that.name) && Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, namespace);
    }
}
