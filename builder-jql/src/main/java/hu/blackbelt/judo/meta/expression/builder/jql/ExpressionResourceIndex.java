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
import hu.blackbelt.judo.meta.expression.binding.ReferenceBinding;
import hu.blackbelt.judo.meta.expression.binding.ReferenceBindingRole;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Index of an expression resource set answering, in constant time, the lookups JQL extraction does per feature and
 * per type: "is there a binding for (type, feature, role)" and "which {@link TypeName} has (name, namespace)". It
 * replaces a walk over the whole resource set per question, which made extraction O(features x model size).
 * <p>
 * One index per resource set, shared by the extractor, the expression builder and the model adapter cache:
 * {@link #of(ResourceSet)} finds it among the resource set's adapters or attaches a new one.
 * <p>
 * The index is built by one full scan on first use (resource order, so {@link #typeName} returns the first match in
 * {@code getAllContents()} order). Objects appended as top-level contents of a resource (how bindings, expressions and
 * type names are stored) are added incrementally, with their contents, through an adapter on each resource; any other
 * change of the top-level contents (remove, move, set) or a resource not seen yet makes the next query rebuild the
 * index. Indexed objects are expected not to change their type name, feature name, role, name or namespace after they
 * were added.
 */
final class ExpressionResourceIndex extends AdapterImpl {

    private final ResourceSet resourceSet;
    private final Set<List<Object>> attributeBindings = new HashSet<>();
    private final Set<List<Object>> referenceBindings = new HashSet<>();
    private final Map<List<Object>, TypeName> typeNames = new HashMap<>();
    private final Set<Resource> watched = new HashSet<>();
    private boolean dirty = true;

    private final AdapterImpl contentsListener = new AdapterImpl() {
        @Override
        public void notifyChanged(Notification notification) {
            if (notification.isTouch() || notification.getFeatureID(Resource.class) != Resource.RESOURCE__CONTENTS) {
                return;
            }
            synchronized (ExpressionResourceIndex.this) {
                switch (notification.getEventType()) {
                    case Notification.ADD:
                        indexTree((EObject) notification.getNewValue());
                        break;
                    case Notification.ADD_MANY:
                        for (Object added : (Collection<?>) notification.getNewValue()) {
                            indexTree((EObject) added);
                        }
                        break;
                    default:
                        dirty = true;
                }
            }
        }
    };

    private ExpressionResourceIndex(ResourceSet resourceSet) {
        this.resourceSet = resourceSet;
    }

    /** @return the index of {@code resourceSet}, attached to it on first use */
    static ExpressionResourceIndex of(ResourceSet resourceSet) {
        synchronized (resourceSet) {
            for (Object adapter : resourceSet.eAdapters()) {
                if (adapter instanceof ExpressionResourceIndex) {
                    return (ExpressionResourceIndex) adapter;
                }
            }
            ExpressionResourceIndex index = new ExpressionResourceIndex(resourceSet);
            resourceSet.eAdapters().add(index);
            return index;
        }
    }

    @Override
    public boolean isAdapterForType(Object type) {
        return type == ExpressionResourceIndex.class;
    }

    synchronized boolean hasAttributeBinding(TypeName typeName, String attributeName, AttributeBindingRole role) {
        refresh();
        return attributeBindings.contains(key(typeName.getName(), typeName.getNamespace(), role, attributeName));
    }

    synchronized boolean hasReferenceBinding(TypeName typeName, String referenceName, ReferenceBindingRole role) {
        refresh();
        return referenceBindings.contains(key(typeName.getName(), typeName.getNamespace(), role, referenceName));
    }

    /**
     * @return the first {@link TypeName} (resource order) with exactly this name and namespace ({@code null} matches
     * {@code null}); {@code null} when there is none
     */
    synchronized TypeName typeName(String name, String namespace) {
        refresh();
        return typeNames.get(Arrays.asList(name, namespace));
    }

    /** Rebuilds the index when dirty or when the resource set has a resource that is not watched yet. */
    private void refresh() {
        if (!dirty && watched.containsAll(resourceSet.getResources())) {
            return;
        }
        attributeBindings.clear();
        referenceBindings.clear();
        typeNames.clear();
        for (Resource resource : new ArrayList<>(resourceSet.getResources())) {
            if (watched.add(resource)) {
                resource.eAdapters().add(contentsListener);
            }
            resource.getAllContents().forEachRemaining(this::index);
        }
        dirty = false;
    }

    private void indexTree(EObject root) {
        if (dirty) {
            return; // rebuilt on the next query anyway
        }
        index(root);
        root.eAllContents().forEachRemaining(this::index);
    }

    private void index(EObject object) {
        if (object instanceof AttributeBinding) {
            AttributeBinding b = (AttributeBinding) object;
            if (b.getTypeName() != null) {
                attributeBindings.add(key(b.getTypeName().getName(), b.getTypeName().getNamespace(), b.getRole(), b.getAttributeName()));
            }
        } else if (object instanceof ReferenceBinding) {
            ReferenceBinding b = (ReferenceBinding) object;
            if (b.getTypeName() != null) {
                referenceBindings.add(key(b.getTypeName().getName(), b.getTypeName().getNamespace(), b.getRole(), b.getReferenceName()));
            }
        } else if (object instanceof TypeName) {
            TypeName t = (TypeName) object;
            typeNames.putIfAbsent(Arrays.asList(t.getName(), t.getNamespace()), t);
        }
    }

    private static List<Object> key(String name, String namespace, Object role, String featureName) {
        return Arrays.asList(name, namespace, role, featureName);
    }
}
