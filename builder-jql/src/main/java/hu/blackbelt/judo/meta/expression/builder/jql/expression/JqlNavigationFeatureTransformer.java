package hu.blackbelt.judo.meta.expression.builder.jql.expression;

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
import hu.blackbelt.judo.meta.expression.builder.jql.*;
import hu.blackbelt.judo.meta.expression.numeric.DecimalAttribute;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.*;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newLogicalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectNavigationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAttributeBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newObjectSequenceBuilder;

public class JqlNavigationFeatureTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> {
    public static final String INVALID_ATTRIBUTE_SELECTOR = "Attribute selector is supported only on ObjectExpressions";
    public static final String INVALID_SEQUENCE_SELECTOR = "Sequence selector is supported only on ObjectExpressions";

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
                    throw new IllegalStateException("Invalid selector: " + jqlFeature.getName());
                }
                // extended feature check must be done before searching for attributes and references
                if (isExtendedFeature(jqlFeature, context)) {
                    JqlFeatureTransformResult<C> transformResult =
                            transformExtendedFeature(jqlFeature, context, baseExpression, navigationBase);
                    baseExpression = transformResult.baseExpression;
                    navigationBase = transformResult.navigationBase;
                } else {
                    Optional<? extends PTE> attribute = getModelAdapter().getAttribute(navigationBase, jqlFeature.getName());
                    Optional<? extends RTE> reference = getModelAdapter().getReference(navigationBase, jqlFeature.getName());
                    Optional<? extends S> sequence = getModelAdapter().getSequence(navigationBase, jqlFeature.getName());
                    if (attribute.isPresent()) {
                        if (!(baseExpression instanceof ObjectExpression)) {
                            throw new IllegalStateException(String.format("%s. Got %s", INVALID_ATTRIBUTE_SELECTOR,
                                                                          baseExpression.getClass().getSimpleName()));
                        }
                        JqlFeatureTransformResult<C> transformResult =
                                transformAttribute(attribute.get(), context, baseExpression, navigationBase, jqlFeature);
                        baseExpression = transformResult.baseExpression;
                        navigationBase = transformResult.navigationBase;
                    } else if (reference.isPresent()) {
                        JqlFeatureTransformResult<C> transformResult =
                                transformReference(reference.get(), context, baseExpression, navigationBase, jqlFeature);
                        baseExpression = transformResult.baseExpression;
                        navigationBase = transformResult.navigationBase;
                    } else if (sequence.isPresent()) {
                        if (!(baseExpression instanceof ObjectExpression)) {
                            throw new IllegalStateException(String.format("%s. Got %s", INVALID_SEQUENCE_SELECTOR,
                                                                          baseExpression.getClass().getSimpleName()));
                        }
                        baseExpression = newObjectSequenceBuilder()
                                .withObjectExpression((ObjectExpression) baseExpression)
                                .withSequenceName(jqlFeature.getName())
                                .build();
                    } else {
                        LOG.error("Feature {} of {} not found", jqlFeature.getName(), navigationBase);
                        String errorMessage = String.format("Feature %s of %s not found", jqlFeature.getName(),
                                                            getModelAdapter().buildTypeName(navigationBase).orElse(null));
                        JqlExpressionBuildingError error = new JqlExpressionBuildingError(errorMessage, jqlFeature);
                        throw new JqlExpressionBuildException(subject, List.of(error));
                    }
                }
            } catch (Exception e) {
                throw new JqlExpressionBuildException(
                        baseExpression, List.of(new JqlExpressionBuildingError(e.getMessage(), jqlFeature)), e);
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

    private boolean isCompatibleParameter(TO inputParameterType, TO derivedFeatureParameterType) {
        return inputParameterType != null && derivedFeatureParameterType != null && getModelAdapter().isMixin(derivedFeatureParameterType, inputParameterType);
    }

    private JqlFeatureTransformResult<C> transformReference(RTE accessor, ExpressionBuildingVariableResolver context, Expression baseExpression, C navigationBase, Feature jqlFeature) {
        C resultNavigationBase = navigationBase;
        Expression resultBaseExpression = baseExpression;
        Optional<String> getterExpression = getModelAdapter().getReferenceGetter(accessor);
        if (jqlTransformers.isResolveDerived() && getModelAdapter().isDerivedReference(accessor) && getterExpression.isPresent()) {
            Object contextInputParameterType = context.getInputParameterType();
            Optional<TO> referenceParameterType = getModelAdapter().getReferenceParameterType(accessor);
            if (context.containsAccessor(accessor)) {
                throw new CircularReferenceException(getModelAdapter().getFqName(accessor));
            }
            if (contextInputParameterType != null && referenceParameterType.isPresent() &&
                !isCompatibleParameter((TO) contextInputParameterType, referenceParameterType.orElse(null))) {
                throw new IncompatibleInputParameterException(getModelAdapter().getFqName(accessor), getModelAdapter().getFqName(contextInputParameterType));
            }

            context.pushAccessor(accessor);
            context.pushBaseExpression(resultBaseExpression);
            context.pushBase(resultNavigationBase);
            resultBaseExpression =
                    jqlTransformers.getExpressionBuilder()
                                   .createExpression(CreateExpressionArguments.<C, TO, NE>builder()
                                                                              .withJqlExpressionAsString(getterExpression.get())
                                                                              .withContext(context)
                                                                              .build());
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
            Optional<TO> attributeParameterType = getModelAdapter().getAttributeParameterType(accessor);
            TO contextInputParameterType = (TO) context.getInputParameterType();
            if (context.containsAccessor(accessor)) {
                throw new CircularReferenceException(getModelAdapter().getFqName(accessor));
            }
            if (contextInputParameterType != null && attributeParameterType.isPresent() &&
                !isCompatibleParameter(contextInputParameterType, attributeParameterType.get())) {
                throw new IncompatibleInputParameterException(getModelAdapter().getFqName(accessor), getModelAdapter().getFqName(contextInputParameterType));
            }

            context.pushAccessor(accessor);
            context.pushBaseExpression(resultBaseExpression);
            context.pushBase(resultNavigationBase);
            resultBaseExpression =
                    jqlTransformers.getExpressionBuilder()
                                   .createExpression(CreateExpressionArguments.<C, TO, NE>builder()
                                                                              .withJqlExpressionAsString(getterExpression.get())
                                                                              .withContext(context)
                                                                              .build());
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
            } else if (getModelAdapter().isTime(attributeType.get())) {
                return newTimeAttributeBuilder().withAttributeName(attributeName)
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
