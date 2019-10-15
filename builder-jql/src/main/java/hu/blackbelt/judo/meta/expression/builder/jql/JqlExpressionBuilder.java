package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.runtime.JqlParser;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

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
public class JqlExpressionBuilder<NE, P, PTE, E, C extends NE, RTE, M, U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());

    private static final String SELF_NAME = "self";

    private MeasureProvider<M, U> measureProvider;
    private final Resource expressionResource;
    private final ModelAdapter<NE, P, PTE, E, C, RTE, M, U> modelAdapter;

    private final EMap<C, Instance> entityInstances = ECollections.asEMap(new ConcurrentHashMap<>());

    private final JqlParser jqlParser = new JqlParser();
    private final JqlTransformers jqlTransformers;

    public JqlExpressionBuilder(final ModelAdapter<NE, P, PTE, E, C, RTE, M, U> modelAdapter, final Resource expressionResource) {
        this.modelAdapter = modelAdapter;
        this.jqlTransformers = new JqlTransformers<>(modelAdapter);
        this.expressionResource = expressionResource;

        modelAdapter.getAllClasses().forEach(clazz -> {
            final TypeName typeName = modelAdapter.getTypeName(clazz).get();

            final Instance self = newInstanceBuilder()
                    .withElementName(typeName)
                    .withName(SELF_NAME)
                    .build();

            expressionResource.getContents().addAll(Arrays.asList(typeName, self));
            entityInstances.put(clazz, self);
        });

    }

    /**
     * Create and return expression of a given JQL string.
     *
     * @param entityType            entity type
     * @param jqlExpressionAsString JQL expression as string
     * @return expression
     */
    public Expression createExpression(final C entityType, String jqlExpressionAsString) {
        final hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression = jqlParser.parseString(jqlExpressionAsString);
        return createExpression(entityType, jqlExpression);
    }

    /**
     * Create and return expression of a given JQL expression.
     *
     * @param entityType    entity type
     * @param jqlExpression JQL expression
     * @return expression
     */
    public Expression createExpression(final C entityType, hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression) {
        final Instance instance = entityType != null ? entityInstances.get(entityType) : null;
        final Expression expression = transformJqlToExpression(jqlExpression, instance != null ? ECollections.singletonEList(instance) : ECollections.emptyEList());

        if (expression != null) {
            LOGGER.debug("Expression created: {}", expression);
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

        final hu.blackbelt.judo.meta.expression.binding.BindingRole bindingRole;
        switch (bindingContext.getRole()) {
            case GETTER:
                bindingRole = hu.blackbelt.judo.meta.expression.binding.BindingRole.GETTER;
                break;
            case SETTER:
                bindingRole = hu.blackbelt.judo.meta.expression.binding.BindingRole.SETTER;
                break;
            default:
                throw new IllegalStateException("Unsupported binding role");
        }

        final Binding binding;
        switch (bindingContext.getType()) {
            case ATTRIBUTE:
                binding = newAttributeBindingBuilder()
                        .withExpression(expression)
                        .withTypeName(instance.getElementName())
                        .withAttributeName(bindingContext.getFeatureName())
                        .withRole(bindingRole)
                        .build();
                break;
            case RELATION:
                binding = newReferenceBindingBuilder()
                        .withExpression(expression)
                        .withTypeName(instance.getElementName())
                        .withReferenceName(bindingContext.getFeatureName())
                        .withRole(bindingRole)
                        .build();
                break;
            default:
                throw new IllegalStateException("Unsupported feature");
        }

        LOGGER.debug("Binding created for expression: {}", expression);
        expressionResource.getContents().add(binding);

        return binding;
    }

    private Expression transformJqlToExpression(hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression, final EList<ObjectVariable> variables) {
        return jqlTransformers.transform(jqlExpression, variables);
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
        GETTER, SETTER
    }

    public enum BindingType {
        ATTRIBUTE, RELATION
    }
}
