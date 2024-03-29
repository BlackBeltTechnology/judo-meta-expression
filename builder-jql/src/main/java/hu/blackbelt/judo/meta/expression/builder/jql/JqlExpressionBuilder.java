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

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.*;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.QualifiedName;
import hu.blackbelt.judo.meta.jql.runtime.JqlParser;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.*;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.binding.util.builder.BindingBuilders.*;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameBuilder;

/**
 * Expression builder from JQL.
 *
 */
public class JqlExpressionBuilder<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> {

    public static final String SELF_NAME = "self";
    private static final Logger LOGGER = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());
    public static final String NAMESPACE_SEPARATOR = "::";
    private final Resource expressionResource;
    private final ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> modelAdapter;

    private final EMap<C, Instance> entityInstances = ECollections.asEMap(new HashMap<>());
    private final Map<String, MeasureName> measureNames = new HashMap<>();
    private final Map<String, MeasureName> durationMeasures = new HashMap<>();
    private final Map<String, TypeName> enumTypes = new HashMap<>();
    private final Map<String, TypeName> primitiveTypes = new HashMap<>();
    private final EMap<TO, TypeName> transferObjectTypes = ECollections.asEMap(new HashMap<>());

    private final JqlParser jqlParser = new JqlParser();
    private final JqlTransformers jqlTransformers;
    private JqlExpressionBuilderConfig config;

    private JqlExpressionBuilderModelAdapterCache<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> cache;
    public JqlExpressionBuilder(ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> modelAdapter, Resource expressionResource, JqlExpressionBuilderConfig config) {
        this.modelAdapter = modelAdapter;
        this.expressionResource = expressionResource;
        this.jqlTransformers = new JqlTransformers<>(this);
        this.config = config;
        this.cache = JqlExpressionBuilderModelAdapterCache.getCache(modelAdapter);

        Map<EObject, EObject> map = cloneExpression();
        remapCache(map);

    }


    private Map<EObject, EObject> cloneExpression() {
        EcoreUtil.Copier copier = new EcoreUtil.Copier();
        synchronized (cache.getTemplateExpressionModel()) {
            Collection<EObject> toCopy = copier.copyAll(cache.getTemplateExpressionModel().getContents());
            copier.copyReferences();
            this.expressionResource.getContents().addAll(toCopy);
        }
        return copier;
    }

    private void remapCache(Map<EObject, EObject> map) {
        for (String key : cache.getMeasureNames().keySet()) {
            measureNames.put(key, (MeasureName) map.get(cache.getMeasureNames().get(key)));
        }

        for (String key : cache.getDurationMeasures().keySet()) {
            durationMeasures.put(key, (MeasureName) map.get(cache.getDurationMeasures().get(key)));
        }

        for (C key : ((EMap<C, Instance>) cache.getEntityInstances()).keySet()) {
            entityInstances.put(key, (Instance) map.get(cache.getEntityInstances().get(key)));
        }

        for (String key : cache.getEnumTypes().keySet()) {
            enumTypes.put(key, (TypeName) map.get(cache.getEnumTypes().get(key)));
        }

        for (TO key : ((EMap<TO, TypeName>) cache.getTransferObjectTypes()).keySet()) {
            transferObjectTypes.put(key, (TypeName) map.get(cache.getTransferObjectTypes().get(key)));
        }

        for (String key : cache.getPrimitiveTypes().keySet()) {
            primitiveTypes.put(key, (TypeName) map.get(cache.getPrimitiveTypes().get(key)));
        }
    }

    public JqlExpressionBuilder(ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> modelAdapter, Resource expressionResource) {
        this(modelAdapter, expressionResource, new JqlExpressionBuilderConfig());
    }

    @SuppressWarnings("unchecked")
    private static <T> Stream<T> all(ResourceSet resourceSet, Class<T> clazz) {
        final Iterable<Notifier> resourceContents = resourceSet::getAllContents;
        return StreamSupport.stream(resourceContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    public static String createQNamespaceString(QualifiedName qName) {
        return getFqString(qName.getNamespaceElements());
    }

    public static String getFqString(List<String> namespaceElements, String... additionalElements) {
        List<String> allElements = new ArrayList<>();
        if (namespaceElements != null) {
            allElements.addAll(namespaceElements);
        }
        if (additionalElements != null) {
            allElements.addAll(Arrays.asList(additionalElements));
        }
        return String.join(NAMESPACE_SEPARATOR, allElements);
    }


    /**
     * Get type name of a given class (ASM/PSM/ESM model element).
     *
     * @param clazz entity type of data model
     * @return type name (expression metamodel element)
     */
    public Optional<TypeName> buildTypeName(final NE clazz) {
        return modelAdapter.buildTypeName(clazz);
    }

    public JqlExpression parseJqlString(String jqlString) {
        return jqlParser.parseString(jqlString);
    }

    /**
     * Create and return expression of a given JQL expression.
     */
    public Expression createExpression(CreateExpressionArguments<C, TO, NE> arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException("CreateExpressionArguments (arguments) cannot be null");
        }

        JqlExpression jqlExpression;
        if (arguments.getJqlExpression() == null && arguments.getJqlExpressionAsString() == null) {
            throw new IllegalArgumentException("Neither jqlExpression or jqlExpressionAsString of CreateExpressionArguments can be null");
        } else {
            jqlExpression = Objects.requireNonNullElseGet(arguments.getJqlExpression(),
                                                          () -> parseJqlString(arguments.getJqlExpressionAsString()));
        }

        ExpressionBuildingVariableResolver context =
                Objects.requireNonNullElseGet(arguments.getContext(),
                                              () -> new JqlExpressionBuildingContext<TO>(config));

        context.pushBase(arguments.getClazz());
        if (arguments.getInputParameterType() != null) {
            context.setInputParameterType(arguments.getInputParameterType());
        }

        if (context.getContextNamespace().isEmpty()) {
            if (arguments.getContextNamespacePreset() != null || arguments.getClazz() != null) {
                context.setContextNamespace(
                        buildTypeName(Objects.requireNonNullElse(arguments.getContextNamespacePreset(), arguments.getClazz()))
                                .map(TypeName::getNamespace)
                                .orElse(null)
                );
            }
        }

        // ARGUMENTS PREP
        /////////////////////////////////////////
        // EXPRESSION PREP

        Object base = context.peekBase();
        final Instance instance = base != null ? entityInstances.get(base) : null;
        if (instance != null) {
            context.pushVariable(instance);
        }
        final Expression expression = transformJqlToExpression(jqlExpression, context);
        if (instance != null) {
            context.popVariable();
        }
        return expression;
    }

    public void storeExpression(Expression expression) {
        if (expression != null) {
            LOGGER.trace("Expression created: {}", expression);
            expressionResource.getContents().add(expression);
        } else {
            LOGGER.warn("No expression created");
        }
    }

    /**
     * Create binding of a given expression.
     *
     * @param bindingContext binding context
     * @param expression     expression
     * @return expression binding
     */
    public Binding createBinding(final BindingContext bindingContext, final C entityType, TO transferObjectType, final Expression expression) {
        final TypeName typeName;
        if (entityType != null) {
            typeName = entityInstances.get(entityType).getElementName();
        } else if (transferObjectType != null) {
            typeName = transferObjectTypes.get(transferObjectType);
        } else {
            throw new IllegalArgumentException("Entity type or transfer object type must be specified");
        }

        final Binding binding;
        switch (bindingContext.getType()) {
            case ATTRIBUTE:
                final AttributeBindingRole attributeBindingRole;
                switch (bindingContext.getRole()) {
                    case GETTER:
                        attributeBindingRole = AttributeBindingRole.GETTER;
                        break;
                    case SETTER:
                        attributeBindingRole = AttributeBindingRole.SETTER;
                        break;
                    default:
                        throw new IllegalStateException("Unsupported binding role");
                }

                binding = newAttributeBindingBuilder()
                        .withExpression(expression)
                        .withTypeName(typeName)
                        .withAttributeName(bindingContext.getFeatureName())
                        .withRole(attributeBindingRole)
                        .build();
                break;
            case RELATION:
                final ReferenceBindingRole referenceBindingRole;
                switch (bindingContext.getRole()) {
                    case GETTER:
                        referenceBindingRole = ReferenceBindingRole.GETTER;
                        break;
                    case SETTER:
                        referenceBindingRole = ReferenceBindingRole.SETTER;
                        break;
                    default:
                        throw new IllegalStateException("Unsupported binding role");
                }

                binding = newReferenceBindingBuilder()
                        .withExpression(expression)
                        .withTypeName(typeName)
                        .withReferenceName(bindingContext.getFeatureName())
                        .withRole(referenceBindingRole)
                        .build();
                break;
            case FILTER:
                binding = newFilterBindingBuilder()
                        .withExpression(expression)
                        .withTypeName(typeName)
                        .build();
                break;
            default:
                throw new IllegalStateException("Unsupported feature");
        }

        LOGGER.debug("Binding created for expression: {}", expression);
        expressionResource.getContents().add(binding);

        return binding;
    }

    public Binding createGetterBinding(C base, Expression expression, String feature, BindingType bindingType) {
        Binding binding = createBinding(new BindingContext(feature, bindingType, BindingRole.GETTER), base, null, expression);
        return binding;
    }

    private Expression transformJqlToExpression(JqlExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        return jqlTransformers.transform(jqlExpression, context);
    }

    public ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> getModelAdapter() {
        return modelAdapter;
    }

    public Resource getExpressionResource() {
        return expressionResource;
    }

    public MeasureName getMeasureName(String qualifiedName) {
        return measureNames.get(qualifiedName);
    }

    public MeasureName getMeasureName(String namespace, String name) {
        return measureNames.get(namespace.replace(".", NAMESPACE_SEPARATOR)  + NAMESPACE_SEPARATOR + name);
    }

    public MeasureName getDurationMeasureName(String qualifiedName) {
        return durationMeasures.get(qualifiedName);
    }

    public Collection<MeasureName> getDurationMeasures() {
        return durationMeasures.values();
    }

    public TypeName getEnumTypeName(String enumType) {
        return enumTypes.get(enumType);
    }

    public TypeName getTypeNameFromResource(QualifiedName qName) {
        if (qName == null) return null;
        return getTypeNameFromResource(createQNamespaceString(qName), qName.getName());
    }

    public TypeName getTypeNameFromResource(String namespace, String name) {
        return Optional.ofNullable(buildTypeName(namespace, name))
                .flatMap(resolvedTypeName ->
                                 all(expressionResource.getResourceSet(), TypeName.class)
                                         .filter(tn -> tn.getName() != null && tn.getName().equals(resolvedTypeName.getName()) &&
                                                       tn.getNamespace() != null && tn.getNamespace().equals(resolvedTypeName.getNamespace()))
                                         .findAny())
                .orElse(null);
    }

    public TypeName buildTypeName(QualifiedName qName) {
        if (qName == null) return null;
        return buildTypeName(createQNamespaceString(qName), qName.getName());
    }

    public TypeName buildTypeName(String namespace, String name) {
        TypeName typeName = newTypeNameBuilder().withName(name).withNamespace(namespace).build();
        return modelAdapter.get(typeName).flatMap(modelAdapter::buildTypeName).orElse(null);
    }

    /**
     * Simplified definition of {@link #getTypeNameOf(QualifiedName, Supplier, Supplier)} where namespace is not calculated
     *
     * @see #getTypeNameOf(QualifiedName, Supplier, Supplier)
     */
    public static TypeName getTypeNameOf(QualifiedName qualifiedName, Supplier<TypeName> getIfNamespaceDefined) {
        return getTypeNameOf(qualifiedName, null, getIfNamespaceDefined);
    }

    /**
     * <p>Returns {@link TypeName} with namespace and name defined.</p>
     * <p>If namespace is not defined it is calculated.</p>
     * <p>Beware that in case type name is not found, exception is thrown.</p>
     * <p>if namespace is not defined and typename is not found, symbol is unknown.</p>
     * <p>if namespace is defined and typename is not found, type is unknown.</p>
     *
     * @param qualifiedName         {@link QualifiedName} that contains information about given namespace and name
     * @param getIfNamespaceDefined {@link Supplier} that returns {@link TypeName} if namespace is defined
     *                              in <i>qualifiedName</i>. Note that defined could mean here that namespace is
     *                              intentionally left empty meaning a symbol is used.
     * @param getIfNamespaceEmpty   {@link Supplier} that returns {@link TypeName} if namespace is not defined
     *                              in <i>qualifiedName</i>.
     * @return {@link TypeName} with namespace and name defined
     * @throws IllegalArgumentException if <i>qualifiedName</i> is null
     * @throws NoSuchElementException   if {@link TypeName} cannot be found
     */
    public static TypeName getTypeNameOf(QualifiedName qualifiedName, Supplier<TypeName> getIfNamespaceEmpty, Supplier<TypeName> getIfNamespaceDefined) {
        if (qualifiedName == null) {
            throw new IllegalArgumentException("QualifiedName cannot be null");
        }

        TypeName typeName = null;
        try {
            try {
                typeName = getIfNamespaceDefined.get();
            } catch (Exception ignored) { }
            if (typeName == null) {
                if (qualifiedName.getNamespaceElements().isEmpty()) {
                    throw new IllegalArgumentException("Symbol not found or type is used in short form");
                } else {
                    String nameSpace = String.join(NAMESPACE_SEPARATOR, qualifiedName.getNamespaceElements()) + NAMESPACE_SEPARATOR;
                    throw new NoSuchElementException(String.format("Type not found: %s%s", nameSpace, qualifiedName.getName()));
                }
            }
        } catch (NoSuchElementException nse) {
            throw nse;
        } catch (Exception e) {
            try {
                typeName = getIfNamespaceEmpty.get();
            } catch (Exception ignored) { }
        }

        if (typeName == null) {
            throw new NoSuchElementException("Unknown symbol: " + qualifiedName.getName());
        }

        return typeName;
    }

    public enum BindingRole {
        GETTER, SETTER
    }

    public enum BindingType {
        ATTRIBUTE, RELATION, FILTER
    }

    public static class BindingContext {

        private final String featureName;
        private final BindingType type;
        private final BindingRole role;

        public BindingContext(final String featureName, final BindingType bindingType, final BindingRole bindingRole) {
            this.featureName = featureName;
            type = bindingType;
            role = bindingRole;
        }

        public String getFeatureName() {
            return featureName;
        }

        public BindingType getType() {
            return type;
        }

        public BindingRole getRole() {
            return role;
        }
    }

    public void overrideTransformer(Class<? extends JqlExpression> jqlType, Function<JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA,TR, S, M, U>, ? extends JqlExpressionTransformerFunction> transformer) {
        jqlTransformers.overrideTransformer(jqlType, transformer);
    }

    public void addFunctionTransformer(String functionName, Function<JqlTransformers, JqlFunctionTransformer<? extends Expression>> transformer) {
        jqlTransformers.addFunctionTransformer(functionName, transformer);
    }

    public void setResolveDerived(boolean resolveDerived) {
        jqlTransformers.setResolveDerived(resolveDerived);
    }

    /**
     * Returns true if c2 equals c1 or, c2 is a supertype of c1
     */
    public boolean isKindOf(C c1, C c2) {
        if (c1.equals(c2)) {
            return true;
        }
        Set<C> checkedSuperTypes = new LinkedHashSet<>();
        Collection<? extends C> superTypes = getModelAdapter().getSuperTypes(c1);
        checkedSuperTypes.addAll(superTypes);
        for (C c : checkedSuperTypes) {
            if (c.equals(c2)) {
                return true;
            } else {
                checkedSuperTypes.addAll(getModelAdapter().getSuperTypes(c));
            }
        }
        return false;
    }

    public MappedTransferObjectCompatibility getMappedTransferObjectTypeCompatibility(TO from, TO to) {
        MappedTransferObjectCompatibility result = MappedTransferObjectCompatibility.NONE;
        if (from.equals(to)) {
            result = MappedTransferObjectCompatibility.KINDOF;
        }
        if (result == MappedTransferObjectCompatibility.NONE) {
            EList<TO> allMappedTransferObjectTypes = getModelAdapter().getAllMappedTransferObjectTypes();
            if (allMappedTransferObjectTypes.contains(from) && allMappedTransferObjectTypes.contains(to)) {
                C mappedEntityFrom = getModelAdapter().getMappedEntityType(from).get();
                C mappedEntityTo = getModelAdapter().getMappedEntityType(to).get();
                if (isKindOf(mappedEntityFrom, mappedEntityTo)) {
                    result = MappedTransferObjectCompatibility.KINDOF;
                } else if (isKindOf(mappedEntityTo, mappedEntityFrom)) {
                    result = MappedTransferObjectCompatibility.SUPERTYPE;
                }
            }
        }
        return result;
    }

    public enum MappedTransferObjectCompatibility {
        NONE, KINDOF, SUPERTYPE
    }

    public void setBuilderConfig(JqlExpressionBuilderConfig builderConfig) {
        this.config = builderConfig;
    }

}
