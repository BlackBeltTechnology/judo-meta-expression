package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.*;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlNavigationFeatureTransformer.JqlFeatureTransformResult;
import hu.blackbelt.judo.meta.expression.constant.StringConstant;
import hu.blackbelt.judo.meta.expression.variable.*;
import hu.blackbelt.judo.meta.jql.jqldsl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.getTypeNameOf;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newImmutableCollectionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newLiteralBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newStringConstantBuilder;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newBooleanVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newStaticSequenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newTypeNameExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.variable.util.builder.VariableBuilders.*;

public class JqlNavigationTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlExpressionTransformer<NavigationExpression, NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> {

    private static final Logger LOG = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());

    public static final String PARAMETER_CATEGORY = "PARAMETER";

    private final JqlNavigationFeatureTransformer<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> featureTransformer;

    public JqlNavigationTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
        featureTransformer = new JqlNavigationFeatureTransformer<>(jqlTransformers);
    }

    public JqlNavigationFeatureTransformer<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> getFeatureTransformer() {
        return featureTransformer;
    }

    private static String navigationString(NavigationExpression jqlExpression) {
        StringBuilder str = new StringBuilder();
        if (jqlExpression.getBase() != null) {
            // parenthesized expression
            str.append("(");
            if (jqlExpression.getBase() instanceof NavigationExpression) {
                str.append(navigationString((NavigationExpression) jqlExpression.getBase()));
            }
            str.append(")");
        } else {
            QualifiedName qName = jqlExpression.getQName();
            str.append(JqlExpressionBuilder.getFqString(qName.getNamespaceElements(), qName.getName()));
        }
        for (Feature feature : jqlExpression.getFeatures()) {
            str.append(".");
            str.append(feature.getName());
        }
        if (jqlExpression.getEnumValue() != null) {
            str.append("#");
            str.append(jqlExpression.getEnumValue());
        }
        return str.toString();
    }

    protected JqlNavigationFeatureTransformer.JqlFeatureTransformResult<C> findBase(JqlExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        // According to the JQL grammar a Navigation's base is not necessarily a NavigationExpression. This is used by JCL.
        NavigationExpression navigation = (NavigationExpression) jqlExpression;
        Expression baseExpression;
        C navigationBase;
        if (navigation.getBase() != null) {
            JqlNavigationFeatureTransformer.JqlFeatureTransformResult<C> base = findBase(navigation.getBase(), context);
            baseExpression = base.baseExpression;
            navigationBase = base.navigationBase;
        } else {
            QualifiedName qualifiedName = navigation.getQName();
            boolean transformFailure = false;
            try {
                Expression contextBaseExpression = context.peekBaseExpression();
                if (!qualifiedName.getNamespaceElements().isEmpty()) {
                    Optional<? extends NE> optNe = getModelAdapter().get(jqlTransformers.getTypeNameFromResource(qualifiedName));
                    String name = optNe.isPresent()
                            ? getModelAdapter().getFqName(optNe.get())
                            : qualifiedName.getName();
                    throw new IllegalArgumentException(name + " is a type");
                }
                String name = qualifiedName.getName();
                if (name.equals(JqlExpressionBuilder.SELF_NAME) && contextBaseExpression != null) {
                    baseExpression = EcoreUtil.copy(contextBaseExpression);
                    navigationBase = (C) context.peekBase();
                } else {
                    Optional<Variable> baseVariable = context.resolveVariable(name);
                    if (baseVariable.isEmpty()) {
                        TA parameterAttribute = Optional.ofNullable(navigation.getFeatures().size() == 1 ? (TO) context.getInputParameterType() : null)
                                .flatMap(t -> getModelAdapter().getTransferAttribute(t, navigation.getFeatures().get(0).getName()))
                                .orElseThrow(() -> {
                                    String errorMessage = "Base variable '" + name;
                                    if (JqlExpressionBuilder.SELF_NAME.equals(name) && context.resolveOnlyCurrentLambdaScope()) {
                                        errorMessage += "' cannot be used in lambda expression";
                                    } else {
                                        errorMessage += "' not found";
                                    }
                                    return new JqlExpressionBuildException(contextBaseExpression, Arrays.asList(new JqlExpressionBuildingError(errorMessage, navigation)));
                                });
                        Expression getVariableExpression = null;
                        StringConstant variableName = newStringConstantBuilder().withValue(navigation.getFeatures().get(0).getName()).build();
                        P parameterAttributeType = getModelAdapter().getTransferAttributeType(parameterAttribute);
                        TypeName parameterAttributeTypeName = getModelAdapter().buildTypeName(parameterAttributeType).get();
                        if (getModelAdapter().isInteger(parameterAttributeType)) {
                            if (getModelAdapter().isMeasuredType(parameterAttributeType)) {
                                M measure = jqlTransformers.getModelAdapter().getMeasureOfType(parameterAttributeType).get();
                                U unit = jqlTransformers.getModelAdapter().getUnitOfType(parameterAttributeType).get();
                                MeasureName measureName = jqlTransformers.getModelAdapter().buildMeasureName(measure).get();
                                getVariableExpression = newMeasuredDecimalEnvironmentVariableBuilder()
                                        .withCategory(PARAMETER_CATEGORY)
                                        .withVariableName(variableName)
                                        .withTypeName(parameterAttributeTypeName)
                                        .withMeasure(measureName)
                                        .withUnitName(jqlTransformers.getModelAdapter().getUnitName(unit))
                                        .build();
                            } else {
                                getVariableExpression = newIntegerEnvironmentVariableBuilder()
                                        .withCategory(PARAMETER_CATEGORY)
                                        .withVariableName(variableName)
                                        .withTypeName(parameterAttributeTypeName)
                                        .build();
                            }
                        } else if (getModelAdapter().isDecimal(parameterAttributeType)) {
                            if (getModelAdapter().isMeasuredType(parameterAttributeType)) {
                                M measure = jqlTransformers.getModelAdapter().getMeasureOfType(parameterAttributeType).get();
                                U unit = jqlTransformers.getModelAdapter().getUnitOfType(parameterAttributeType).get();
                                MeasureName measureName = jqlTransformers.getModelAdapter().buildMeasureName(measure).get();
                                getVariableExpression = newMeasuredDecimalEnvironmentVariableBuilder()
                                        .withCategory(PARAMETER_CATEGORY)
                                        .withVariableName(variableName)
                                        .withTypeName(parameterAttributeTypeName)
                                        .withMeasure(measureName)
                                        .withUnitName(jqlTransformers.getModelAdapter().getUnitName(unit))
                                        .build();
                            } else {
                                getVariableExpression = newDecimalEnvironmentVariableBuilder()
                                        .withCategory(PARAMETER_CATEGORY)
                                        .withVariableName(variableName)
                                        .withTypeName(parameterAttributeTypeName)
                                        .build();
                            }
                        } else if (getModelAdapter().isBoolean(parameterAttributeType)) {
                            getVariableExpression = newBooleanEnvironmentVariableBuilder()
                                    .withCategory(PARAMETER_CATEGORY)
                                    .withVariableName(variableName)
                                    .withTypeName(parameterAttributeTypeName)
                                    .build();
                        } else if (getModelAdapter().isString(parameterAttributeType)) {
                            getVariableExpression = newStringEnvironmentVariableBuilder()
                                    .withCategory(PARAMETER_CATEGORY)
                                    .withVariableName(variableName)
                                    .withTypeName(parameterAttributeTypeName)
                                    .build();
                        } else if (getModelAdapter().isEnumeration(parameterAttributeType)) {
                            getVariableExpression = newLiteralEnvironmentVariableBuilder()
                                    .withCategory(PARAMETER_CATEGORY)
                                    .withVariableName(variableName)
                                    .withTypeName(parameterAttributeTypeName)
                                    .build();
                        } else if (getModelAdapter().isCustom(parameterAttributeType)) {
                            getVariableExpression = newCustomEnvironmentVariableBuilder()
                                    .withCategory(PARAMETER_CATEGORY)
                                    .withVariableName(variableName)
                                    .withTypeName(parameterAttributeTypeName)
                                    .build();
                        } else if (getModelAdapter().isDate(parameterAttributeType)) {
                            getVariableExpression = newDateEnvironmentVariableBuilder()
                                    .withCategory(PARAMETER_CATEGORY)
                                    .withVariableName(variableName)
                                    .withTypeName(parameterAttributeTypeName)
                                    .build();
                        } else if (getModelAdapter().isTimestamp(parameterAttributeType)) {
                            getVariableExpression = newTimestampEnvironmentVariableBuilder()
                                    .withCategory(PARAMETER_CATEGORY)
                                    .withVariableName(variableName)
                                    .withTypeName(parameterAttributeTypeName)
                                    .build();
                        }
                        return new JqlFeatureTransformResult<>(null, getVariableExpression);
                    } else {
                        JqlFeatureTransformResult<C> createVariableReferenceResult = createVariableReference(baseVariable.get(), null);
                        baseExpression = createVariableReferenceResult.baseExpression;
                        navigationBase = createVariableReferenceResult.navigationBase;
                    }
                }
                transformFailure = true; // IntelliJ might suggest it is not used, but it is
                return getFeatureTransformer().transform(navigation.getFeatures(), baseExpression, navigationBase, context);
            } catch (Exception e) {
                if (transformFailure) throw e;
            }
            TypeName typeName = getTypeNameOf(
                    qualifiedName,
                    () -> jqlTransformers.getTypeNameFromResource(context.getContextNamespace().get(), qualifiedName.getName()),
                    () -> jqlTransformers.getTypeNameFromResource(navigation.getQName()));
            if (getModelAdapter().isSequence(getModelAdapter().get(typeName).get())) {
                baseExpression = newStaticSequenceBuilder().withTypeName(typeName).build();
                navigationBase = null;
            } else if (getModelAdapter().isPrimitiveType(getModelAdapter().get(typeName).get())) {
                baseExpression = newTypeNameExpressionBuilder().withName(typeName.getName()).withNamespace(typeName.getNamespace()).build();
                navigationBase = null;
            } else {
                baseExpression = newImmutableCollectionBuilder().withElementName(typeName).build();
                navigationBase = (C) getModelAdapter().get(typeName).get();
            }
        }
        return getFeatureTransformer().transform(navigation.getFeatures(), baseExpression, navigationBase, context);
    }

    protected JqlFeatureTransformResult<C> createVariableReference(Variable baseVariable, Expression defaultBaseExpression) {
        C navigationBase = null;
        Expression baseExpression = defaultBaseExpression;
        if (baseVariable instanceof ObjectVariable) {
            navigationBase = (C) ((ObjectVariable) baseVariable).getObjectType(getModelAdapter());
            baseExpression = newObjectVariableReferenceBuilder().withVariable((ObjectVariable) baseVariable).build();
        } else if (baseVariable instanceof CollectionVariable) {
            navigationBase = (C) ((CollectionVariable) baseVariable).getObjectType(getModelAdapter());
            baseExpression = newCollectionVariableReferenceBuilder().withVariable((CollectionVariable) baseVariable).build();
        } else if (baseVariable instanceof IntegerVariable) {
            baseExpression = newIntegerVariableReferenceBuilder().withVariable((IntegerVariable) baseVariable).build();
        } else if (baseVariable instanceof StringVariable) {
            baseExpression = newStringVariableReferenceBuilder().withVariable((StringVariable) baseVariable).build();
        } else if (baseVariable instanceof DecimalVariable) {
            baseExpression = newDecimalVariableReferenceBuilder().withVariable((DecimalVariable) baseVariable).build();
        } else if (baseVariable instanceof BooleanVariable) {
            baseExpression = newBooleanVariableReferenceBuilder().withVariable((BooleanVariable) baseVariable).build();
        } else if (baseVariable instanceof DateVariable) {
            baseExpression = newDateVariableReferenceBuilder().withVariable((DateVariable) baseVariable).build();
        } else if (baseVariable instanceof TimestampVariable) {
            baseExpression = newTimestampVariableReferenceBuilder().withVariable((TimestampVariable) baseVariable).build();
        } else if (baseVariable instanceof TimeVariable) {
            baseExpression = newTimeVariableReferenceBuilder().withVariable((TimeVariable) baseVariable).build();
        } else if (baseVariable instanceof CustomVariable) {
            baseExpression = newCustomVariableReferenceBuilder().withVariable((CustomVariable) baseVariable).build();
        } else if (baseVariable instanceof EnumerationVariable) {
            baseExpression = newEnumerationVariableReferenceBuilder().withVariable((EnumerationVariable) baseVariable).build();
        }
        return new JqlFeatureTransformResult<C>(navigationBase, baseExpression);
    }


    public Expression doTransform(NavigationExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        NavigationExpression navigation = jqlExpression;
        LOG.debug("Transform navigation: {}", navigationString(navigation));
        if (jqlExpression.getEnumValue() != null) {
            return createEnumLiteral(jqlExpression);
        } else {
            JqlNavigationFeatureTransformer.JqlFeatureTransformResult<C> base = findBase(jqlExpression, context);
            Expression baseExpression = base.baseExpression;
            C navigationBase = base.navigationBase;
            LOG.debug("Base: {} ({})", baseExpression, getModelAdapter().buildTypeName(navigationBase).orElse(null));
            return baseExpression;
        }
    }

    private Expression createEnumLiteral(NavigationExpression jqlExpression) {
        String enumValue = jqlExpression.getEnumValue();
        String enumName = JqlExpressionBuilder.getFqString(jqlExpression.getQName().getNamespaceElements(), jqlExpression.getQName().getName());
        TypeName enumTypeName = jqlTransformers.getEnumType(enumName);
        if (enumTypeName == null) {
            throw new IllegalArgumentException("Unknown enum: " + enumName);
        }
        if (!getModelAdapter().contains((E) enumTypeName.get(getModelAdapter()), enumValue)) {
            throw new IllegalArgumentException("Unknown literal: " + enumValue);
        }
        return newLiteralBuilder().withEnumeration(enumTypeName).withValue(enumValue).build();
    }

}
