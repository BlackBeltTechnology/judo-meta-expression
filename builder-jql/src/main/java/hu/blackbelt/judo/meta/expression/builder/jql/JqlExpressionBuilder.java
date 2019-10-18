package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.expression.binding.AttributeBindingRole;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.binding.ReferenceBindingRole;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.binding.util.builder.BindingBuilders.newAttributeBindingBuilder;
import static hu.blackbelt.judo.meta.expression.binding.util.builder.BindingBuilders.newReferenceBindingBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;

/**
 * Expression builder from JQL.
 *
 * @param <NE>  namespace element
 * @param <P>   primitive
 * @param <PTE> primitive typed element (ie. attribute)
 * @param <E>   enumeration
 * @param <C>   class
 * @param <RTE> reference typed element (ie. reference)
 * @param <M>   measure
 * @param <U>   unit
 */
public class JqlExpressionBuilder<NE, P, PTE, E extends NE, C extends NE, RTE, M, U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());

    public static final String SELF_NAME = "self";

    private MeasureProvider<M, U> measureProvider;
    private final Resource expressionResource;
    private final ModelAdapter<NE, P, PTE, E, C, RTE, M, U> modelAdapter;

    private final EMap<C, Instance> entityInstances = ECollections.asEMap(new ConcurrentHashMap<>());
    private final Map<String, MeasureName> measureNames = new ConcurrentHashMap<>();
    private final Map<String, TypeName> enumTypes = new ConcurrentHashMap<>();

    private final JqlParser jqlParser = new JqlParser();
    private final JqlTransformers jqlTransformers;

    public JqlExpressionBuilder(final ModelAdapter<NE, P, PTE, E, C, RTE, M, U> modelAdapter, final Resource expressionResource) {
        this.modelAdapter = modelAdapter;
        this.jqlTransformers = new JqlTransformers<>(modelAdapter, measureNames, enumTypes, expressionResource);
        this.expressionResource = expressionResource;

        addMeasures();
        addClasses();
        addEnums();

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
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("  - self instance is already added to resource set: {}", clazz);
                }
                self = foundSelf.get();
            }

            entityInstances.put(clazz, self);
        });
    }

    private void storeTypeName(NE namespaceElement, TypeName typeName) {
        if (!all(expressionResource.getResourceSet(), TypeName.class)
                .anyMatch(tn -> Objects.equals(tn.getName(), typeName.getName()) && Objects.equals(tn.getNamespace(), typeName.getNamespace()))) {
            expressionResource.getContents().add(typeName);
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("  - type name is already added to resource set: {}", namespaceElement);
            }
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

    /**
     * Create and return expression of a given JQL string.
     *
     * @param entityType            entity type
     * @param jqlExpressionAsString JQL expression as string
     * @return expression
     */
    public Expression createExpression(final C entityType, String jqlExpressionAsString) {
        final JqlExpression jqlExpression = jqlParser.parseString(jqlExpressionAsString);
        return createExpression(entityType, jqlExpression);
    }

    /**
     * Create and return expression of a given JQL expression.
     *
     * @param entityType    entity type
     * @param jqlExpression JQL expression
     * @return expression
     */
    public Expression createExpression(final C entityType, JqlExpression jqlExpression) {
        final Instance instance = entityType != null ? entityInstances.get(entityType) : null;
        final Expression expression = transformJqlToExpression(jqlExpression, instance != null ? ECollections.singletonEList(instance) : ECollections.emptyEList());

        if (expression != null) {
            LOGGER.info("Expression created: {}", expression);
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
    public Binding createBinding(final BindingContext bindingContext, final C entityType, final Expression expression) {
        final Instance instance = entityInstances.get(entityType);

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
                        .withTypeName(instance.getElementName())
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
                        .withTypeName(instance.getElementName())
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

    private Expression transformJqlToExpression(JqlExpression jqlExpression, final EList<ObjectVariable> variables) {
        return jqlTransformers.transform(jqlExpression, variables);
    }

    public static <T> Stream<T> all(final ResourceSet resourceSet, final Class<T> clazz) {
        final Iterable<Notifier> asmContents = resourceSet::getAllContents;
        return StreamSupport.stream(asmContents.spliterator(), true)
                .filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
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

    public enum BindingRole {
        GETTER, SETTER, DEFAULT, RANGE
    }

    public enum BindingType {
        ATTRIBUTE, RELATION
    }
}
