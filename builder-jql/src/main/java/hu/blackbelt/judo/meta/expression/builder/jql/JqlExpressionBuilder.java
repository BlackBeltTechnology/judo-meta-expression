package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.AttributeBindingRole;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.binding.ReferenceBindingRole;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.QualifiedName;
import hu.blackbelt.judo.meta.jql.runtime.JqlParser;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
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
    private final EMap<C, Instance> entityInstances = ECollections.asEMap(new ConcurrentHashMap<>());
    private final Map<String, MeasureName> measureNames = new ConcurrentHashMap<>();
    private final Map<String, MeasureName> durationMeasures = new ConcurrentHashMap<>();
    private final Map<String, TypeName> enumTypes = new ConcurrentHashMap<>();
    private final Map<String, TypeName> primitiveTypes = new ConcurrentHashMap<>();
    private final EMap<TO, TypeName> transferObjectTypes = ECollections.asEMap(new ConcurrentHashMap<>());
    private final JqlParser jqlParser = new JqlParser();
    private final JqlTransformers jqlTransformers;
    private JqlExpressionBuilderConfig config;

    public JqlExpressionBuilder(ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> modelAdapter, Resource expressionResource, JqlExpressionBuilderConfig config) {
        this.modelAdapter = modelAdapter;
        this.expressionResource = expressionResource;
        this.jqlTransformers = new JqlTransformers<>(this);
        this.config = config;

        addMeasures();
        addEntityTypes();
        addEnums();
        addSequences();
        addTransferObjectTypes();
        addActors();
        addPrimitiveTypes();
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

    private void addSequences() {
        modelAdapter.getAllStaticSequences().forEach(e -> {
            TypeName typeName = modelAdapter.buildTypeName(e).get();
            storeTypeName(e, typeName);
        });
    }

    private void addTransferObjectTypes() {
        modelAdapter.getAllTransferObjectTypes().forEach(t -> {
            TypeName typeName = modelAdapter.buildTypeName(t).get();
            storeTypeName(t, typeName);
            transferObjectTypes.put(t, typeName);
        });
    }
    
    private void addActors() {
        modelAdapter.getAllActorTypes().forEach(t -> {
            TypeName typeName = modelAdapter.buildTypeName(t).get();
            storeTypeName(t, typeName);
        });
    }


    private void addEnums() {
        modelAdapter.getAllEnums().forEach(e -> {
            TypeName enumTypeName = modelAdapter.buildTypeName(e).get();
            storeTypeName(e, enumTypeName);
            enumTypes.put(enumTypeName.getNamespace() + NAMESPACE_SEPARATOR + enumTypeName.getName(), enumTypeName);
        });
    }

    private void addPrimitiveTypes() {
        modelAdapter.getAllPrimitiveTypes().forEach(e -> {
            TypeName primitiveTypeName = modelAdapter.buildTypeName(e).get();
            storeTypeName(e, primitiveTypeName);
            primitiveTypes.put(primitiveTypeName.getNamespace() + NAMESPACE_SEPARATOR + primitiveTypeName.getName(), primitiveTypeName);
        });
    }

    private void addEntityTypes() {
        modelAdapter.getAllEntityTypes().forEach(clazz -> {
            final TypeName typeName = modelAdapter.buildTypeName(clazz).get();
            storeTypeName(clazz, typeName);

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

    private void storeTypeName(NE namespaceElement, TypeName typeName) {
        if (all(expressionResource.getResourceSet(), TypeName.class)
                .noneMatch(tn -> Objects.equals(tn.getName(), typeName.getName()) && Objects.equals(tn.getNamespace(), typeName.getNamespace()))) {
            expressionResource.getContents().add(typeName);
        } else {
            LOGGER.trace("  - type name is already added to resource set: {}", namespaceElement);
        }
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

    public Expression createExpression(C clazz, String jqlExpressionString) {
        return createExpression(clazz, parseJqlString(jqlExpressionString));
    }

    public Expression createExpressionWithInput(C clazz, String jqlExpressionString, TO inputParameterType) {
        return createExpression(clazz, parseJqlString(jqlExpressionString), inputParameterType);
    }

    public Expression createExpression(String jqlString, ExpressionBuildingVariableResolver context) {
        return createExpression(parseJqlString(jqlString), context);
    }

    public Expression createExpression(C entityType, JqlExpression jqlExpression) {
        return createExpression(entityType, jqlExpression, new JqlExpressionBuildingContext<TO>(config));
    }

    public Expression createExpression(C entityType, JqlExpression jqlExpression, TO inputParameterType) {
        return createExpression(entityType, jqlExpression, new JqlExpressionBuildingContext<TO>(config, inputParameterType));
    }

    public Expression createExpression(C clazz, JqlExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        context.pushBase(clazz);
        return createExpression(jqlExpression, context);
    }

    /**
     * Create and return expression of a given JQL expression.
     *
     * @param jqlExpression JQL expression
     * @return expression
     */
    public Expression createExpression(JqlExpression jqlExpression, ExpressionBuildingVariableResolver context) {
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

    public Binding createGetterBinding(C base, Expression expression, String feature, JqlExpressionBuilder.BindingType bindingType) {
        Binding binding = createBinding(new JqlExpressionBuilder.BindingContext(feature, bindingType, JqlExpressionBuilder.BindingRole.GETTER), base, null, expression);
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
        String namespace = createQNamespaceString(qName);
        String name = qName.getName();
        return getTypeNameFromResource(namespace, name);
    }

    public TypeName getTypeNameFromResource(String namespace, String name) {
        if (!namespace.isEmpty()) {
        	TypeName resolvedTypeName = buildTypeName(namespace, name);
            return JqlExpressionBuilder.all(expressionResource.getResourceSet(), TypeName.class).filter(tn -> Objects.equals(tn.getName(), resolvedTypeName.getName()) && Objects.equals(tn.getNamespace(), resolvedTypeName.getNamespace())).findAny().orElse(null);
        } else {
            return null;
        }
    }

    public TypeName buildTypeName(QualifiedName qName) {
        return buildTypeName(createQNamespaceString(qName), qName.getName());
    }

    public TypeName buildTypeName(String namespace, String name) {
    	TypeName typeName = newTypeNameBuilder().withName(name).withNamespace(namespace).build();
        NE ne = modelAdapter.get(typeName).orElseThrow(() -> new NoSuchElementException("No such element: " + typeName));
        return modelAdapter.buildTypeName(ne).get();
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
