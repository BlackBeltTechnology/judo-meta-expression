package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.AttributeBindingRole;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.binding.ReferenceBindingRole;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.QualifiedName;
import hu.blackbelt.judo.meta.jql.runtime.JqlParser;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
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
 * @param <NE>  namespace element
 * @param <P>   primitive
 * @param <PTE> primitive typed element (ie. attribute)
 * @param <E>   enumeration
 * @param <C>   class
 * @param <RTE> reference typed element (ie. reference)
 * @param <S>   sequence
 * @param <M>   measure
 * @param <U>   unit
 */
public class JqlExpressionBuilder<NE, P extends NE, PTE, E extends P, C extends NE, T extends NE, RTE, S, M, U> {

    public static final String SELF_NAME = "self";
    private static final Logger LOGGER = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());
    public static final String NAMESPACE_SEPARATOR = "::";
    private final Resource expressionResource;
    private final ModelAdapter<NE, P, PTE, E, C, T, RTE, S, M, U> modelAdapter;
    private final EMap<C, Instance> entityInstances = ECollections.asEMap(new ConcurrentHashMap<>());
    private final Map<String, MeasureName> measureNames = new ConcurrentHashMap<>();
    private final Map<String, MeasureName> durationMeasures = new ConcurrentHashMap<>();
    private final Map<String, TypeName> enumTypes = new ConcurrentHashMap<>();
    private final EMap<T, TypeName> transferObjectTypes = ECollections.asEMap(new ConcurrentHashMap<>());
    private final JqlParser jqlParser = new JqlParser();
    private final JqlTransformers jqlTransformers;

    public JqlExpressionBuilder(final ModelAdapter<NE, P, PTE, E, C, T, RTE, S, M, U> modelAdapter, final Resource expressionResource) {
        this.modelAdapter = modelAdapter;
        this.expressionResource = expressionResource;
        this.jqlTransformers = new JqlTransformers<>(this);

        addMeasures();
        addEntityTypes();
        addEnums();
        addSequences();
        addTransferObjectTypes();
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Stream<T> all(final ResourceSet resourceSet, final Class<T> clazz) {
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

    private void addEnums() {
        modelAdapter.getAllEnums().forEach(e -> {
            TypeName enumTypeName = modelAdapter.buildTypeName(e).get();
            storeTypeName(e, enumTypeName);
            enumTypes.put(enumTypeName.getNamespace() + NAMESPACE_SEPARATOR + enumTypeName.getName(), enumTypeName);
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
                        .withElementName(typeName.eResource() != null ? typeName : getTypeName(typeName.getNamespace(), typeName.getName()))
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
            boolean alreadyAdded = all(expressionResource.getResourceSet(), MeasureName.class)
                    .anyMatch(m -> Objects.equals(m.getName(), measureName.getName()) && Objects.equals(m.getNamespace(), measureName.getNamespace()));
            if (!alreadyAdded) {
                expressionResource.getContents().add(measureName);
                String measureNameString = String.join(NAMESPACE_SEPARATOR, measureName.getNamespace(), measureName.getName());
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

    public Expression createExpression(String jqlString, ExpressionBuildingVariableResolver context) {
        return createExpression(parseJqlString(jqlString), context);
    }

    public Expression createExpression(C entityType, JqlExpression jqlExpression) {
        return createExpression(entityType, jqlExpression, new JqlExpressionBuildingContext());
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
        if (expression != null) {
            LOGGER.trace("Expression created: {}", expression);
            expressionResource.getContents().add(expression);
        } else {
            LOGGER.warn("No expression created");
        }

        return expression;
    }

    /**
     * Create binding of a given expression.
     *
     * @param bindingContext binding context
     * @param expression     expression
     * @return expression binding
     */
    public Binding createBinding(final BindingContext bindingContext, final C entityType, T transferObjectType, final Expression expression) {
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
                    case DEFAULT:
                        attributeBindingRole = AttributeBindingRole.DEFAULT;
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
                    case DEFAULT:
                        referenceBindingRole = ReferenceBindingRole.DEFAULT;
                        break;
                    case RANGE:
                        referenceBindingRole = ReferenceBindingRole.RANGE;
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

    public ModelAdapter<NE, P, PTE, E, C, T, RTE, S, M, U> getModelAdapter() {
        return modelAdapter;
    }

    public Resource getExpressionResource() {
        return expressionResource;
    }

    public MeasureName getMeasureName(String qualifiedName) {
        return measureNames.get(qualifiedName);
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
        if (!namespace.isEmpty()) {
            String name = qName.getName();
            TypeName typeName = newTypeNameBuilder().withName(name).withNamespace(namespace).build();
            NE ne = modelAdapter.get(typeName).orElseThrow(() -> new NoSuchElementException(String.valueOf(typeName)));
            TypeName resolvedTypeName = modelAdapter.buildTypeName(ne).get();
            return JqlExpressionBuilder.all(expressionResource.getResourceSet(), TypeName.class).filter(tn -> Objects.equals(tn.getName(), resolvedTypeName.getName()) && Objects.equals(tn.getNamespace(), resolvedTypeName.getNamespace())).findAny().orElse(null);
        } else {
            return null;
        }
    }

    public TypeName getTypeName(String namespace, String name) {
        return JqlExpressionBuilder.all(expressionResource.getResourceSet(), TypeName.class).filter(tn -> Objects.equals(tn.getName(), name) && Objects.equals(tn.getNamespace(), namespace)).findAny().orElse(null);
    }

    public enum BindingRole {
        GETTER, SETTER, DEFAULT, RANGE
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

    public void overrideTransformer(Class<? extends JqlExpression> jqlType, Function<JqlTransformers<NE, P, PTE, E, C, T, RTE, S, M, U>, ? extends JqlExpressionTransformerFunction> transformer) {
        jqlTransformers.overrideTransformer(jqlType, transformer);
    }

    public void setResolveDerived(boolean resolveDerived) {
		jqlTransformers.setResolveDerived(resolveDerived);
    }


}
