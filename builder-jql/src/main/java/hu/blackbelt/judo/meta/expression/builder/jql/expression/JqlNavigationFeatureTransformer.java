package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.*;
import hu.blackbelt.judo.meta.expression.collection.CollectionFilterExpression;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromCollectionExpression;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.collection.ObjectNavigationFromCollectionExpression;
import hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders;
import hu.blackbelt.judo.meta.expression.numeric.DecimalAttribute;
import hu.blackbelt.judo.meta.expression.object.ObjectSelectorExpression;
import hu.blackbelt.judo.meta.expression.object.util.builder.ObjectSelectorExpressionBuilder;
import hu.blackbelt.judo.meta.expression.object.util.builder.ObjectVariableReferenceBuilder;
import hu.blackbelt.judo.meta.expression.operator.ObjectComparator;
import hu.blackbelt.judo.meta.expression.operator.ObjectSelector;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.expression.variable.util.builder.TypedObjectVariableBuilder;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newExistsBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newLogicalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newObjectSequenceBuilder;

public class JqlNavigationFeatureTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> {

    private static final Logger LOG = LoggerFactory.getLogger(JqlNavigationFeatureTransformer.class.getName());
    private final JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers;

    public JqlNavigationFeatureTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        this.jqlTransformers = jqlTransformers;
    }

    public JqlFeatureTransformResult<C> transform(List<Feature> features, Expression subject, C navigationBase, ExpressionBuildingVariableResolver context) {
        Expression baseExpression = subject;
        for (Feature jqlFeature : features) {
            try {
                if (navigationBase == null) {
                    throw new IllegalStateException(String.format("Invalid selector: %s", jqlFeature.getName()));
                }
                Optional<? extends PTE> attribute = getModelAdapter().getAttribute(navigationBase,
                        jqlFeature.getName());
                Optional<? extends RTE> reference = getModelAdapter().getReference(navigationBase,
                        jqlFeature.getName());
                Optional<? extends S> sequence = getModelAdapter().getSequence(navigationBase, jqlFeature.getName());
                if (attribute.isPresent()) {
                    JqlFeatureTransformResult<C> transformResult = transformAttribute(attribute.get(), context,
                            baseExpression, navigationBase, jqlFeature);
                    baseExpression = transformResult.baseExpression;
                    navigationBase = transformResult.navigationBase;
                } else if (reference.isPresent()) {
                    JqlFeatureTransformResult<C> transformResult = transformReference(reference.get(), context,
                            baseExpression, navigationBase, jqlFeature);
                    baseExpression = transformResult.baseExpression;
                    navigationBase = transformResult.navigationBase;
                } else if (sequence.isPresent()) {
                    baseExpression = newObjectSequenceBuilder().withObjectExpression((ObjectExpression) baseExpression)
                            .withSequenceName(jqlFeature.getName()).build();
                } else if (isExtendedFeature(jqlFeature, context)) {
                    JqlFeatureTransformResult<C> transformResult = transformExtendedFeature(jqlFeature, context,
                            baseExpression, navigationBase);
                    baseExpression = transformResult.baseExpression;
                    navigationBase = transformResult.navigationBase;
                } else {
                    LOG.error("Feature {} of {} not found", jqlFeature.getName(), navigationBase);
                    String errorMessage = String.format("Feature %s of %s not found", jqlFeature.getName(),
                            getModelAdapter().buildTypeName(navigationBase).orElse(null));
                    JqlExpressionBuildingError error = new JqlExpressionBuildingError(errorMessage, jqlFeature);
                    throw new JqlExpressionBuildException(subject, Arrays.asList(error));
                }
            } catch (Exception e) {
                throw new JqlExpressionBuildException(baseExpression,
                        Arrays.asList(new JqlExpressionBuildingError(e.getMessage(), jqlFeature)), e);
            }
        }
        return new JqlFeatureTransformResult<>(navigationBase, baseExpression);
    }

    protected boolean isExtendedFeature(Feature jqlFeature, ExpressionBuildingVariableResolver context) {
        return false;
    }

    protected JqlFeatureTransformResult<C> transformExtendedFeature(Feature jqlFeature, ExpressionBuildingVariableResolver context, Expression baseExpression, C navigationBase) {
        return null;
    }

    protected ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> getModelAdapter() {
        return jqlTransformers.getModelAdapter();
    }

    private JqlFeatureTransformResult<C> transformReference(RTE accessor, ExpressionBuildingVariableResolver context, Expression baseExpression, C navigationBase, Feature jqlFeature) {
        C resultNavigationBase = navigationBase;
        Expression resultBaseExpression = baseExpression;
        Optional<String> getterExpression = getModelAdapter().getReferenceGetter(accessor);
        if (jqlTransformers.isResolveDerived() && getModelAdapter().isDerivedReference(accessor) && getterExpression.isPresent()) {
            if (context.containsAccessor(accessor)) {
                throw new CircularReferenceException(accessor.toString());
            } else if (context.getInputParameterType() != null && getModelAdapter().getReferenceParameterType(accessor).isPresent() && !EcoreUtil.equals((EObject) context.getInputParameterType(), (EObject) getModelAdapter().getReferenceParameterType(accessor).get())) {
                throw new IncompatibleInputParameterException(accessor.toString());
            } else {
                context.pushAccessor(accessor);
            }
            context.pushBaseExpression(resultBaseExpression);
            context.pushBase(resultNavigationBase);
            resultBaseExpression = jqlTransformers.getExpressionBuilder().createExpression(getterExpression.get(), context);
            context.popBaseExpression();
            context.popBase();
            context.popAccessor();
        } else {
            resultBaseExpression = createReferenceSelector(accessor, jqlFeature.getName(),
                    (ReferenceExpression) resultBaseExpression);
        }
        resultNavigationBase = getModelAdapter().getTarget(accessor);
        return new JqlFeatureTransformResult<>(resultNavigationBase, resultBaseExpression);
    }

    private JqlFeatureTransformResult<C> transformAttribute(PTE accessor, ExpressionBuildingVariableResolver context, Expression baseExpression, C navigationBase, Feature jqlFeature) {
        C resultNavigationBase = navigationBase;
        Expression resultBaseExpression = baseExpression;
        Optional<String> getterExpression = getModelAdapter().getAttributeGetter(accessor);
        if (jqlTransformers.isResolveDerived() && getModelAdapter().isDerivedAttribute(accessor) && getterExpression.isPresent()) {
            if (context.containsAccessor(accessor)) {
                throw new CircularReferenceException(accessor.toString());
            } else if (context.getInputParameterType() != null && getModelAdapter().getAttributeParameterType(accessor).isPresent() && !EcoreUtil.equals((EObject) context.getInputParameterType(), (EObject) getModelAdapter().getAttributeParameterType(accessor).get())) {
                throw new IncompatibleInputParameterException(accessor.toString());
            } else {
                context.pushAccessor(accessor);
            }
            context.pushBaseExpression(resultBaseExpression);
            context.pushBase(resultNavigationBase);
            resultBaseExpression = jqlTransformers.getExpressionBuilder().createExpression(getterExpression.get(), context);
            context.popBaseExpression();
            context.popBase();
            context.popAccessor();
        } else {
            resultBaseExpression = createAttributeSelector(accessor, jqlFeature.getName(),
                    (ObjectExpression) resultBaseExpression);
        }
        return new JqlFeatureTransformResult<>(null, resultBaseExpression);
    }

    /**
     * Create attribute selector.
     *
     * @param attribute        (transfer) attribute
     * @param attributeName    mapped attribute name
     * @param objectExpression object expression that attribute selector is created
     *                         in
     * @return attribute selector
     */
    protected AttributeSelector createAttributeSelector(final PTE attribute, final String attributeName, final ObjectExpression objectExpression) {
        final Optional<? extends P> attributeType = getModelAdapter().getAttributeType(attribute);
        if (attributeType.isPresent()) {
            if (getModelAdapter().isBoolean(attributeType.get())) {
                return newLogicalAttributeBuilder().withAttributeName(attributeName)
                        .withObjectExpression(objectExpression).build();
            } else if (getModelAdapter().isDecimal(attributeType.get())) {
                DecimalAttribute result = newDecimalAttributeBuilder().withAttributeName(attributeName)
                        .withObjectExpression(objectExpression).build();
                getModelAdapter().getUnit(result).ifPresent(unit -> {
                    ModelAdapter.UnitFraction unitRates = getModelAdapter().getUnitRates(unit);
                    result.setRateDividend(unitRates.getDividend());
                    result.setRateDivisor(unitRates.getDivisor());
                });
                return result;
            } else if (getModelAdapter().isDate(attributeType.get())) {
                return newDateAttributeBuilder().withAttributeName(attributeName).withObjectExpression(objectExpression)
                        .build();
            } else if (getModelAdapter().isEnumeration(attributeType.get())) {
                return newEnumerationAttributeBuilder().withAttributeName(attributeName)
                        .withObjectExpression(objectExpression).build();
            } else if (getModelAdapter().isInteger(attributeType.get())) {
                AttributeSelector result;
                if (getModelAdapter().isMeasuredType(attributeType.get())) {
                    result = newDecimalAttributeBuilder().withAttributeName(attributeName)
                            .withObjectExpression(objectExpression).build();
                    getModelAdapter().getUnit((DecimalAttribute) result).ifPresent(unit -> {
                        ModelAdapter.UnitFraction unitRates = getModelAdapter().getUnitRates(unit);
                        ((DecimalAttribute) result).setRateDividend(unitRates.getDividend());
                        ((DecimalAttribute) result).setRateDivisor(unitRates.getDivisor());
                    });
                } else {
                    result = newIntegerAttributeBuilder().withAttributeName(attributeName)
                            .withObjectExpression(objectExpression).build();
                }
                return result;
            } else if (getModelAdapter().isString(attributeType.get())) {
                return newStringAttributeBuilder().withAttributeName(attributeName)
                        .withObjectExpression(objectExpression).build();
            } else if (getModelAdapter().isTimestamp(attributeType.get())) {
                return newTimestampAttributeBuilder().withAttributeName(attributeName)
                        .withObjectExpression(objectExpression).build();
            } else {
                return newCustomAttributeBuilder().withAttributeName(attributeName)
                        .withObjectExpression(objectExpression).build();
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
     * @param referenceExpression object expression that reference selector is
     *                            created in
     * @return attribute selector
     */
    protected ReferenceExpression createReferenceSelector(final RTE reference, final String referenceName, final ReferenceExpression referenceExpression) {
        if (getModelAdapter().isCollectionReference(reference)) {
            if (referenceExpression instanceof CollectionExpression) {
                return newCollectionNavigationFromCollectionExpressionBuilder().withReferenceName(referenceName)
                        .withCollectionExpression((CollectionExpression) referenceExpression).build();
            } else if (referenceExpression instanceof ObjectExpression) {
                return newCollectionNavigationFromObjectExpressionBuilder().withReferenceName(referenceName)
                        .withObjectExpression((ObjectExpression) referenceExpression).build();
            } else {
                throw new IllegalStateException("Reference expression must be object or collection");
            }
        } else {
            if (referenceExpression instanceof CollectionExpression) {
                return newObjectNavigationFromCollectionExpressionBuilder().withReferenceName(referenceName)
                        .withCollectionExpression((CollectionExpression) referenceExpression).build();
            } else if (referenceExpression instanceof ObjectExpression) {
                return newObjectNavigationExpressionBuilder().withReferenceName(referenceName)
                        .withObjectExpression((ObjectExpression) referenceExpression).build();
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
