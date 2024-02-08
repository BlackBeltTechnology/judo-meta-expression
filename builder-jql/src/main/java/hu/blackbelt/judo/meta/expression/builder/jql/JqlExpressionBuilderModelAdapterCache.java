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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.support.ExpressionModelResourceSupport;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;

public class JqlExpressionBuilderModelAdapterCache<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());

    public static final String NAMESPACE_SEPARATOR = "::";
    public static final String SELF_NAME = "self";

    private final Resource expressionResource;
    private final ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> modelAdapter;

    private final EMap<C, Instance> entityInstances = ECollections.asEMap(new HashMap<>());
    private final Map<String, MeasureName> measureNames = new HashMap<>();
    private final Map<String, MeasureName> durationMeasures = new HashMap<>();
    private final Map<String, TypeName> enumTypes = new HashMap<>();
    private final Map<String, TypeName> primitiveTypes = new HashMap<>();
    private final EMap<TO, TypeName> transferObjectTypes = ECollections.asEMap(new HashMap<>());

    private final static CacheLoader<ModelAdapter, JqlExpressionBuilderModelAdapterCache> cacheLoader = new CacheLoader<>() {
        @Override
        public JqlExpressionBuilderModelAdapterCache load(ModelAdapter modelAdapter) {

            final ExpressionModelResourceSupport expressionModelResourceSupport = ExpressionModelResourceSupport.expressionModelResourceSupportBuilder()
                    .uri(URI.createURI("expression:filter-" + UUID.randomUUID()))
                    .build();

            JqlExpressionBuilderModelAdapterCache cache = new JqlExpressionBuilderModelAdapterCache(modelAdapter, expressionModelResourceSupport.getResource());
            return cache;
        }
    };

    private final static LoadingCache<ModelAdapter, JqlExpressionBuilderModelAdapterCache> cacheProvider = CacheBuilder
            .newBuilder()
            .expireAfterAccess(Long.parseLong(System.getProperty("JqlExpressionBuilderModelAdapterCacheExpiration", "86400")), TimeUnit.SECONDS)
            .build(cacheLoader);

    public static JqlExpressionBuilderModelAdapterCache getCache(ModelAdapter modelAdapter) {
        JqlExpressionBuilderModelAdapterCache cache = null;
        try {
            cache = cacheProvider.get(modelAdapter);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return cache;
    }

    private JqlExpressionBuilderModelAdapterCache(ModelAdapter modelAdapter, Resource expressionResource) {
        this.modelAdapter = modelAdapter;
        this.expressionResource = expressionResource;
        addModelElements();
    }

    private void addModelElements() {
        addMeasures();
        addEntityTypes();
        addEnums();
        addSequences();
        addTransferObjectTypes();
        addActors();
        addPrimitiveTypes();
    }
    public Resource getTemplateExpressionModel() {
        return expressionResource;
    }

    public EMap<C, Instance> getEntityInstances() {
        return entityInstances;
    }

    public Map<String, MeasureName> getMeasureNames() {
        return measureNames;
    }

    public Map<String, MeasureName> getDurationMeasures() {
        return durationMeasures;
    }

    public Map<String, TypeName> getEnumTypes() {
        return enumTypes;
    }

    public Map<String, TypeName> getPrimitiveTypes() {
        return primitiveTypes;
    }

    public EMap<TO, TypeName> getTransferObjectTypes() {
        return transferObjectTypes;
    }

    @SuppressWarnings("unchecked")
    private static <T> Stream<T> all(ResourceSet resourceSet, Class<T> clazz) {
        final Iterable<Notifier> resourceContents = resourceSet::getAllContents;
        return StreamSupport.stream(resourceContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    private void addSequences() {
        modelAdapter.getAllStaticSequences().forEach(e -> {
            TypeName typeName = modelAdapter.buildTypeName(e).get();
            storeTypeName(expressionResource, e, typeName);
        });
    }

    private void addTransferObjectTypes() {
        modelAdapter.getAllTransferObjectTypes().forEach(t -> {
            TypeName typeName = modelAdapter.buildTypeName(t).get();
            storeTypeName(expressionResource, t, typeName);
            transferObjectTypes.put(t, typeName);
        });
    }

    private void addActors() {
        modelAdapter.getAllActorTypes().forEach(t -> {
            TypeName typeName = modelAdapter.buildTypeName(t).get();
            storeTypeName(expressionResource, t, typeName);
        });
    }


    private void addEnums() {
        modelAdapter.getAllEnums().forEach(e -> {
            TypeName enumTypeName = modelAdapter.buildTypeName(e).get();
            storeTypeName(expressionResource, e, enumTypeName);
            enumTypes.put(enumTypeName.getNamespace() + NAMESPACE_SEPARATOR + enumTypeName.getName(), enumTypeName);
        });
    }

    private void addPrimitiveTypes() {
        modelAdapter.getAllPrimitiveTypes().forEach(e -> {
            TypeName primitiveTypeName = modelAdapter.buildTypeName(e).get();
            storeTypeName(expressionResource, e, primitiveTypeName);
            primitiveTypes.put(primitiveTypeName.getNamespace() + NAMESPACE_SEPARATOR + primitiveTypeName.getName(), primitiveTypeName);
        });
    }

    private void addEntityTypes() {
        modelAdapter.getAllEntityTypes().forEach(clazz -> {
            final TypeName typeName = modelAdapter.buildTypeName(clazz).get();
            storeTypeName(expressionResource, clazz, typeName);

            Optional<Instance> foundSelf = all(expressionResource.getResourceSet(), Instance.class)
                    .filter(i -> Objects.equals(i.getName(), SELF_NAME) && EcoreUtil.equals(i.getElementName(), typeName))
                    .findAny();

            final Instance self;
            if (!foundSelf.isPresent()) {
                self = newInstanceBuilder()
                        .withElementName(typeName.eResource() != null ? typeName : buildTypeName(typeName.getNamespace(), typeName.getName()))
                        .withName(SELF_NAME)
                        .build();
                expressionResource.getContents().add(self);
            } else {
                LOGGER.trace("  - self instance is already added to resource set: {}", clazz);
                self = foundSelf.get();
            }

            entityInstances.put(clazz, self);
        });
    }

    private void addMeasures() {
        modelAdapter.getAllMeasures().forEach(measure -> {
            MeasureName measureName = modelAdapter.buildMeasureName(measure).get();
            String measureNameString = String.join(NAMESPACE_SEPARATOR, measureName.getNamespace(), measureName.getName());
            boolean alreadyAdded = measureNames.containsKey(measureNameString);
            if (!alreadyAdded) {
                expressionResource.getContents().add(measureName);
                measureNames.put(measureNameString, measureName);
                modelAdapter.getUnits(measure).stream().filter(modelAdapter::isDurationSupportingAddition).findAny().ifPresent(u -> durationMeasures.put(measureNameString, measureName));
            }

        });
    }

    private Optional<TypeName> buildTypeName(final NE clazz) {
        return modelAdapter.buildTypeName(clazz);
    }

    private TypeName buildTypeName(String namespace, String name) {
        TypeName typeName = newTypeNameBuilder().withName(name).withNamespace(namespace).build();
        return modelAdapter.get(typeName).flatMap(modelAdapter::buildTypeName).orElse(null);
    }

    private void storeTypeName(Resource expressionResource, NE namespaceElement, TypeName typeName) {
        if (all(expressionResource.getResourceSet(), TypeName.class)
                .noneMatch(tn -> Objects.equals(tn.getName(), typeName.getName()) && Objects.equals(tn.getNamespace(), typeName.getNamespace()))) {
            expressionResource.getContents().add(typeName);
        } else {
            LOGGER.trace("  - type name is already added to resource set: {}", namespaceElement);
        }
    }

}
