package hu.blackbelt.judo.meta.expression.builder.jql;

/*-
 * #%L
 * JUDO :: Expression :: Model
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

import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.binding.AttributeBinding;
import hu.blackbelt.judo.meta.expression.binding.AttributeBindingRole;
import hu.blackbelt.judo.meta.expression.binding.ReferenceBindingRole;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.binding.util.builder.BindingBuilders.newAttributeBindingBuilder;
import static hu.blackbelt.judo.meta.expression.binding.util.builder.BindingBuilders.newReferenceBindingBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link ExpressionResourceIndex}: bindings and type names; initial scan, incremental adds, rebuild after other changes, new resources, sharing. */
class ExpressionResourceIndexTest {

    private ResourceSet resourceSet;
    private Resource resource;
    private ExpressionResourceIndex index;

    @BeforeEach
    void setUp() {
        resourceSet = new ResourceSetImpl();
        resource = new XMIResourceImpl(URI.createURI("urn:expressions"));
        resourceSet.getResources().add(resource);
        index = ExpressionResourceIndex.of(resourceSet);
    }

    private static TypeName type(String name) {
        return newTypeNameBuilder().withNamespace("demo::entities").withName(name).build();
    }

    private static AttributeBinding attribute(String type, String attribute, AttributeBindingRole role) {
        return newAttributeBindingBuilder().withTypeName(type(type)).withAttributeName(attribute).withRole(role).build();
    }

    @Test
    void findsBindingsPresentBeforeFirstUse() {
        resource.getContents().add(attribute("Order", "total", AttributeBindingRole.GETTER));
        resource.getContents().add(newReferenceBindingBuilder().withTypeName(type("Order")).withReferenceName("items")
                .withRole(ReferenceBindingRole.SETTER).build());

        assertTrue(index.hasAttributeBinding(type("Order"), "total", AttributeBindingRole.GETTER));
        assertFalse(index.hasAttributeBinding(type("Order"), "total", AttributeBindingRole.SETTER), "role differs");
        assertFalse(index.hasAttributeBinding(type("Customer"), "total", AttributeBindingRole.GETTER), "type differs");
        assertTrue(index.hasReferenceBinding(type("Order"), "items", ReferenceBindingRole.SETTER));
        assertFalse(index.hasReferenceBinding(type("Order"), "items", ReferenceBindingRole.GETTER));
    }

    @Test
    void seesBindingsAddedAfterFirstUse() {
        assertFalse(index.hasAttributeBinding(type("Order"), "total", AttributeBindingRole.GETTER));

        resource.getContents().add(attribute("Order", "total", AttributeBindingRole.GETTER));
        assertTrue(index.hasAttributeBinding(type("Order"), "total", AttributeBindingRole.GETTER), "single add");

        resource.getContents().addAll(List.of(attribute("Order", "a", AttributeBindingRole.GETTER),
                attribute("Order", "b", AttributeBindingRole.SETTER)));
        assertTrue(index.hasAttributeBinding(type("Order"), "a", AttributeBindingRole.GETTER), "add many");
        assertTrue(index.hasAttributeBinding(type("Order"), "b", AttributeBindingRole.SETTER), "add many");
    }

    @Test
    void forgetsRemovedBindings() {
        AttributeBinding binding = attribute("Order", "total", AttributeBindingRole.GETTER);
        resource.getContents().add(binding);
        assertTrue(index.hasAttributeBinding(type("Order"), "total", AttributeBindingRole.GETTER));

        resource.getContents().remove(binding);
        assertFalse(index.hasAttributeBinding(type("Order"), "total", AttributeBindingRole.GETTER));
    }

    @Test
    void seesNewResources() {
        assertFalse(index.hasAttributeBinding(type("Order"), "total", AttributeBindingRole.GETTER));

        Resource other = new XMIResourceImpl(URI.createURI("urn:other"));
        other.getContents().add(attribute("Order", "total", AttributeBindingRole.GETTER));
        resourceSet.getResources().add(other);
        assertTrue(index.hasAttributeBinding(type("Order"), "total", AttributeBindingRole.GETTER));
    }

    @Test
    void sharedPerResourceSet() {
        assertSame(index, ExpressionResourceIndex.of(resourceSet));
    }

    @Test
    void typeNamesFirstInResourceOrderAndIncremental() {
        TypeName first = type("Order");
        TypeName second = type("Order");
        resource.getContents().add(first);
        resource.getContents().add(second);
        assertSame(first, index.typeName("Order", "demo::entities"), "first in resource order");
        assertNull(index.typeName("Order", "other::ns"), "namespace differs");

        // Binding.typeName is not containment: a type name only referenced by a binding is not in the contents, so
        // (like the former getAllContents scan) it is not found
        resource.getContents().add(attributeWith(type("Customer")));
        assertNull(index.typeName("Customer", "demo::entities"), "referenced, not contained");

        TypeName added = type("Customer");
        resource.getContents().add(added);
        assertSame(added, index.typeName("Customer", "demo::entities"), "added after first use");

        resource.getContents().remove(first);
        assertSame(second, index.typeName("Order", "demo::entities"), "rebuilt after removal");
    }

    private static AttributeBinding attributeWith(TypeName typeName) {
        return newAttributeBindingBuilder().withTypeName(typeName).withAttributeName("a").withRole(AttributeBindingRole.GETTER).build();
    }
}
