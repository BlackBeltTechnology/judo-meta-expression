package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.AttributeBinding;
import hu.blackbelt.judo.meta.expression.binding.AttributeBindingRole;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.binding.ReferenceBindingRole;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.QualifiedName;
import hu.blackbelt.judo.meta.jql.runtime.JqlParser;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.binding.util.builder.BindingBuilders.newAttributeBindingBuilder;
import static hu.blackbelt.judo.meta.expression.binding.util.builder.BindingBuilders.newReferenceBindingBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newInstanceBuilder;

/**
 * Expression builder from JQL.
 *
 * @param <NE>  namespace element
 * @param <P>   primitive
 * @param <PTE> primitive typed element (ie. attribute)
 * @param <E>   enumeration
 * @param <C>   class
 * @param <RTE> reference typed element (ie. reference)
 * @param <S> sequence
 * @param <M>   measure
 * @param <U>   unit
 */
public class JqlExpressionBuilder<NE, P, PTE, E extends NE, C extends NE, AP extends NE, RTE, S, M, U> {

    public static final String SELF_NAME = "self";
    private static final Logger LOGGER = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());
    private final Resource expressionResource;
    private final ModelAdapter<NE, P, PTE, E, C, AP, RTE, S, M, U> modelAdapter;
    private final EMap<C, Instance> entityInstances = ECollections.asEMap(new ConcurrentHashMap<>());
    private final Map<String, MeasureName> measureNames = new ConcurrentHashMap<>();
    private final Map<String, TypeName> enumTypes = new ConcurrentHashMap<>();
    private final Map<String, TypeName> accessPoints = new ConcurrentHashMap<>();
    private final JqlParser jqlParser = new JqlParser();
    private final JqlTransformers jqlTransformers;

    public JqlExpressionBuilder(final ModelAdapter<NE, P, PTE, E, C, AP, RTE, S, M, U> modelAdapter, final Resource expressionResource) {
        this.modelAdapter = modelAdapter;
        this.expressionResource = expressionResource;
        this.jqlTransformers = new JqlTransformers<>(this);

        addMeasures();
        addAccessPoints();
        addClasses();
        addEnums();
        addSequences();

    }

    @SuppressWarnings("unchecked")
    private static <T> Stream<T> all(final ResourceSet resourceSet, final Class<T> clazz) {
        final Iterable<Notifier> resourceContents = resourceSet::getAllContents;
        return StreamSupport.stream(resourceContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }

    private static String createQNamespaceString(QualifiedName qName) {
        String qNamespaceString;
        if (qName.getNamespaceElements() != null && !qName.getNamespaceElements().isEmpty()) {
            qNamespaceString = String.join("::", qName.getNamespaceElements());
        } else {
            qNamespaceString = null;
        }
        return qNamespaceString;
    }

    private void addSequences() {
        modelAdapter.getAllStaticSequences().forEach(e -> {
            TypeName typeName = modelAdapter.getTypeName(e).get();
            storeTypeName(e, typeName);
        });
    }

    private void addAccessPoints() {
        modelAdapter.getAllAccessPoints().forEach(e -> {
            TypeName typeName = modelAdapter.getTypeName(e).get();
            storeTypeName(e, typeName);
            accessPoints.put(typeName.getNamespace() + "::" + typeName.getName(), typeName);
        });
    }

    private void addEnums() {
        modelAdapter.getAllEnums().forEach(e -> {
            TypeName enumTypeName = modelAdapter.getEnumerationTypeName(e).get();
            storeTypeName(e, enumTypeName);
            enumTypes.put(enumTypeName.getNamespace() + "::" + enumTypeName.getName(), enumTypeName);
        });
    }

    private void addClasses() {
        modelAdapter.getAllClasses().forEach(clazz -> {
            final TypeName typeName = getTypeName(clazz).get();

            storeTypeName(clazz, typeName);

            Optional<Instance> foundSelf = all(expressionResource.getResourceSet(), Instance.class)
                    .filter(i -> Objects.equals(i.getName(), SELF_NAME) && EcoreUtil.equals(i.getElementName(), typeName))
                    .findAny();

            final Instance self;
            if (!foundSelf.isPresent()) {
                self = newInstanceBuilder()
                        .withElementName(typeName)
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
            MeasureName measureName = modelAdapter.getMeasureName(measure).get();
            boolean alreadyAdded = all(expressionResource.getResourceSet(), MeasureName.class)
                    .anyMatch(m -> Objects.equals(m.getName(), measureName.getName()) && Objects.equals(m.getNamespace(), measureName.getNamespace()));
            if (!alreadyAdded) {
                expressionResource.getContents().add(measureName);
                measureNames.put(String.join("::", measureName.getNamespace(), measureName.getName()), measureName);
            }

        });
    }

    /**
     * Get type name of a given class (ASM/PSM/ESM model element).
     *
     * @param clazz entity type of data model
     * @return type name (expression metamodel element)
     */
    public Optional<TypeName> getTypeName(final C clazz) {
        return modelAdapter.getTypeName(clazz);
    }

    public Expression createExpression(C entityType, String jqlExpressionAsString) {
        final JqlExpression jqlExpression = jqlParser.parseString(jqlExpressionAsString);
        JqlExpressionBuildingContext context = new JqlExpressionBuildingContext();
        context.pushBase(entityType);
        return createExpression(jqlExpression, context);
    }

    /**
     * Create and return expression of a given JQL string.
     *
     * @param jqlExpressionAsString JQL expression as string
     * @param context JQL building context, storing variables and navigation state
     * @return expression
     */
    public Expression createExpression(String jqlExpressionAsString, JqlExpressionBuildingContext context) {
        final JqlExpression jqlExpression = jqlParser.parseString(jqlExpressionAsString);
        return createExpression(jqlExpression, context);
    }

    /**
     * Create and return expression of a given JQL expression.
     *
     * @param jqlExpression JQL expression
     * @return expression
     */
    private Expression createExpression(JqlExpression jqlExpression, JqlExpressionBuildingContext context) {
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
    public Binding createBinding(final BindingContext bindingContext, final C entityType, AP accessPoint, final Expression expression) {
        final TypeName typeName;
        if (entityType != null) {
            typeName = entityInstances.get(entityType).getElementName();
        } else if (accessPoint != null) {
            typeName = accessPoints.get(modelAdapter.getTypeName(accessPoint).map(tn -> tn.getNamespace() + "::" + tn.getName()).get());
        } else {
            throw new IllegalArgumentException("Entity type or access point must be specified");
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

    public Optional<? extends Binding> getBinding(C base, String feature) {
        TypeName typeName = getModelAdapter().getTypeName(base).get();
        Optional<AttributeBinding> first = all(expressionResource.getResourceSet(), AttributeBinding.class)
                .filter(b -> b.getTypeName() != null &&
                        Objects.equals(b.getTypeName().getName(), typeName.getName()) &&
                        Objects.equals(b.getTypeName().getNamespace(), typeName.getNamespace()) &&
                        Objects.equals(b.getAttributeName(), feature)).findFirst();
        return first;
    }


    private Expression transformJqlToExpression(JqlExpression jqlExpression, JqlExpressionBuildingContext context) {
        return jqlTransformers.transform(jqlExpression, context);
    }

    public ModelAdapter<NE, P, PTE, E, C, AP, RTE, S, M, U> getModelAdapter() {
        return modelAdapter;
    }

    public Resource getExpressionResource() {
        return expressionResource;
    }

    public MeasureName getMeasureName(String qualifiedName) {
        return measureNames.get(qualifiedName);
    }

    public TypeName getEnumTypeName(String enumType) {
        return enumTypes.get(enumType);
    }

    public TypeName getTypeNameFromResource(QualifiedName qName) {
        String namespace = createQNamespaceString(qName);
        return JqlExpressionBuilder.all(expressionResource.getResourceSet(), TypeName.class).filter(tn -> Objects.equals(tn.getName(), qName.getName()) && Objects.equals(tn.getNamespace(), namespace)).findAny().orElse(null);
    }

    public TypeName getTypeName(String namespace, String name) {
        return JqlExpressionBuilder.all(expressionResource.getResourceSet(), TypeName.class).filter(tn -> Objects.equals(tn.getName(), name) && Objects.equals(tn.getNamespace(), namespace)).findAny().orElse(null);
    }

    public enum BindingRole {
        GETTER, SETTER, DEFAULT, RANGE
    }

    public enum BindingType {
        ATTRIBUTE, RELATION
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


}
