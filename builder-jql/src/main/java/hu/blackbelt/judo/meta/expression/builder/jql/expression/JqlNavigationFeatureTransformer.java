package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.CircularReferenceException;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newLogicalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newObjectSequenceBuilder;

public class JqlNavigationFeatureTransformer<NE, P extends NE, PTE, E extends P, C extends NE, AP extends NE, RTE, S, M, U> {

    private static final Logger LOG = LoggerFactory.getLogger(JqlNavigationFeatureTransformer.class.getName());
    private final JqlTransformers<NE, P, PTE, E, C, AP, RTE, S, M, U> jqlTransformers;

    public JqlNavigationFeatureTransformer(JqlTransformers<NE, P, PTE, E, C, AP, RTE, S, M, U> jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

    public JqlFeatureTransformResult<C> transform(JqlExpression expression, Expression subject, C navigationBase, ExpressionBuildingVariableResolver context) {
        Expression baseExpression = subject;
        EList<Feature> features;
        if (expression instanceof NavigationExpression) {
            features = ((NavigationExpression) expression).getFeatures();
        } else if (expression instanceof FunctionCall) {
            features = ((FunctionCall) expression).getFeatures();
        } else {
            throw new IllegalStateException("Feature transformer called on object with no features");
        }
        for (Feature jqlFeature : features) {
            Optional<? extends PTE> attribute = getModelAdapter().getAttribute(navigationBase, jqlFeature.getName());
            Optional<? extends RTE> reference = getModelAdapter().getReference(navigationBase, jqlFeature.getName());
            Optional<? extends S> sequence = getModelAdapter().getSequence(navigationBase, jqlFeature.getName());
            if (attribute.isPresent()) {
                JqlFeatureTransformResult<C> transformResult = transformAttribute(attribute.get(), context, baseExpression, navigationBase, jqlFeature);
                baseExpression = transformResult.baseExpression;
                navigationBase = transformResult.navigationBase;
            } else if (reference.isPresent()) {
                JqlFeatureTransformResult<C> transformResult = transformReference(reference.get(), context, baseExpression, navigationBase, jqlFeature);
                baseExpression = transformResult.baseExpression;
                navigationBase = transformResult.navigationBase;
            } else if (sequence.isPresent()) {
                baseExpression = newObjectSequenceBuilder().withObjectExpression((ObjectExpression) baseExpression).withSequenceName(jqlFeature.getName()).build();
            } else if (isExtendedFeaturePresent(expression, jqlFeature, context)) {
                JqlFeatureTransformResult<C> transformResult = transformExtendedFeature(expression, context, baseExpression, navigationBase, jqlFeature);
                baseExpression = transformResult.baseExpression;
                navigationBase = transformResult.navigationBase;
            } else {
                LOG.error("Feature {} of {} not found", jqlFeature.getName(), navigationBase);
                throw new IllegalStateException(String.format("Feature %s of %s not found", jqlFeature.getName(), getModelAdapter().getTypeName(navigationBase).orElse(null)));
            }
        }
        return new JqlFeatureTransformResult<>(navigationBase, baseExpression);
    }

    protected boolean isExtendedFeaturePresent(JqlExpression jqlExpression, Feature jqlFeature, ExpressionBuildingVariableResolver context) {
        return false;
    }

    protected JqlFeatureTransformResult<C> transformExtendedFeature(JqlExpression jqlExpression,
                                                                    ExpressionBuildingVariableResolver context, Expression baseExpression, C navigationBase,
                                                                    Feature jqlFeature) {
        return null;
    }

    private ModelAdapter<NE, P, PTE, E, C, AP, RTE, S, M, U> getModelAdapter() {
        return jqlTransformers.getModelAdapter();
    }

    private JqlFeatureTransformResult<C> transformReference(RTE accessor, ExpressionBuildingVariableResolver context, Expression baseExpression, C navigationBase, Feature jqlFeature) {
        C resultNavigationBase = navigationBase;
        Expression resultBaseExpression = baseExpression;
        if (getModelAdapter().isDerivedReference(accessor)) {
            if (context.containsAccessor(accessor)) {
                throw new CircularReferenceException(accessor.toString());
            } else {
                context.pushAccessor(accessor);
            }
            String getterExpression = getModelAdapter().getReferenceGetter(accessor).get();
            context.pushBaseExpression(resultBaseExpression);
            context.pushBase(resultNavigationBase);
            resultBaseExpression = jqlTransformers.getExpressionBuilder().createExpression(getterExpression, context);
            context.popBaseExpression();
            context.popBase();
            context.popAccessor();
        } else {
            resultBaseExpression = createReferenceSelector(accessor, jqlFeature.getName(), (ReferenceExpression) resultBaseExpression);
        }
        resultNavigationBase = getModelAdapter().getTarget(accessor);
        return new JqlFeatureTransformResult<>(resultNavigationBase, resultBaseExpression);
    }

    private JqlFeatureTransformResult<C> transformAttribute(PTE accessor, ExpressionBuildingVariableResolver context, Expression baseExpression, C navigationBase, Feature jqlFeature) {
        C resultNavigationBase = navigationBase;
        Expression resultBaseExpression = baseExpression;
        if (getModelAdapter().isDerivedAttribute(accessor)) {
            if (context.containsAccessor(accessor)) {
                throw new CircularReferenceException(accessor.toString());
            } else {
                context.pushAccessor(accessor);
            }
            String getterExpression = getModelAdapter().getAttributeGetter(accessor).get();
            context.pushBaseExpression(resultBaseExpression);
            context.pushBase(resultNavigationBase);
            resultBaseExpression = jqlTransformers.getExpressionBuilder().createExpression(getterExpression, context);
            context.popBaseExpression();
            context.popBase();
            context.popAccessor();
        } else {
            resultBaseExpression = createAttributeSelector(accessor, jqlFeature.getName(), (ObjectExpression) resultBaseExpression);
        }
        return new JqlFeatureTransformResult<>(null, resultBaseExpression);
    }

    /**
     * Create attribute selector.
     *
     * @param attribute        (transfer) attribute
     * @param attributeName    mapped attribute name
     * @param objectExpression object expression that attribute selector is created in
     * @return attribute selector
     */
    protected AttributeSelector createAttributeSelector(final PTE attribute, final String attributeName, final ObjectExpression objectExpression) {
        final Optional<? extends P> attributeType = getModelAdapter().getAttributeType(attribute);
        if (attributeType.isPresent()) {
            if (getModelAdapter().isBoolean(attributeType.get())) {
                return newLogicalAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isDecimal(attributeType.get())) {
                return newDecimalAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isDate(attributeType.get())) {
                return newDateAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isEnumeration(attributeType.get())) {
                return newEnumerationAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isInteger(attributeType.get())) {
                return newIntegerAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isString(attributeType.get())) {
                return newStringAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isTimestamp(attributeType.get())) {
                return newTimestampAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            } else {
                return newCustomAttributeBuilder()
                        .withAttributeName(attributeName)
                        .withObjectExpression(objectExpression)
                        .build();
            }
        } else {
            throw new IllegalStateException("Unknown attribute");
        }
    }

    /**
     * Create reference selector.
     *
     * @param reference           (transfer) relation
     * @param referenceName       mapped relation name
     * @param referenceExpression object expression that reference selector is created in
     * @return attribute selector
     */
    protected ReferenceExpression createReferenceSelector(final RTE reference, final String referenceName, final ReferenceExpression referenceExpression) {
        if (getModelAdapter().isCollectionReference(reference)) {
            if (referenceExpression instanceof CollectionExpression) {
                return newCollectionNavigationFromCollectionExpressionBuilder()
                        .withReferenceName(referenceName)
                        .withCollectionExpression((CollectionExpression) referenceExpression)
                        .build();
            } else if (referenceExpression instanceof ObjectExpression) {
                return newCollectionNavigationFromObjectExpressionBuilder()
                        .withReferenceName(referenceName)
                        .withObjectExpression((ObjectExpression) referenceExpression)
                        .build();
            } else {
                throw new IllegalStateException("Reference expression must be object or collection");
            }
        } else {
            if (referenceExpression instanceof CollectionExpression) {
                return newObjectNavigationFromCollectionExpressionBuilder()
                        .withReferenceName(referenceName)
                        .withCollectionExpression((CollectionExpression) referenceExpression)
                        .build();
            } else if (referenceExpression instanceof ObjectExpression) {
                return newObjectNavigationExpressionBuilder()
                        .withReferenceName(referenceName)
                        .withObjectExpression((ObjectExpression) referenceExpression)
                        .build();
            } else {
                throw new IllegalStateException("Reference expression must be object or collection");
            }
        }
    }

    public static class JqlFeatureTransformResult<C> {
        public C navigationBase;
        public Expression baseExpression;

        public JqlFeatureTransformResult(C navigationBase, Expression baseExpression) {
            this.navigationBase = navigationBase;
            this.baseExpression = baseExpression;
        }
    }

}
