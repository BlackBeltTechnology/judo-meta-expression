package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.variable.*;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.QualifiedName;
import hu.blackbelt.judo.meta.jql.jqldsl.util.builder.NavigationExpressionBuilder;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newImmutableCollectionBuilder;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newBooleanVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newStaticSequenceBuilder;

public class JqlNavigationTransformer<NE, P extends NE, PTE, E extends P, C extends NE, AP extends NE, RTE, S, M, U> extends AbstractJqlExpressionTransformer<NavigationExpression, NE, P, PTE, E, C, AP, RTE, S, M, U> {

    private static final Logger LOG = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());

    private final JqlNavigationFeatureTransformer<NE, P, PTE, E, C, AP, RTE, S, M, U> featureTransformer;

    public JqlNavigationTransformer(JqlTransformers<NE, P, PTE, E, C, AP, RTE, S, M, U> jqlTransformers) {
        super(jqlTransformers);
        featureTransformer = new JqlNavigationFeatureTransformer<>(jqlTransformers);
    }

    protected JqlNavigationFeatureTransformer<NE, P, PTE, E, C, AP, RTE, S, M, U> getFeatureTransformer() {
        return featureTransformer;
    }

    private static String navigationString(NavigationExpression jqlExpression) {
        StringBuilder str = new StringBuilder();
        if (jqlExpression.getBase() != null) {
            // parenthesized expression
            str.append("(");
            str.append(navigationString((NavigationExpression) jqlExpression.getBase()));
            str.append(")");
        } else {
            QualifiedName qName = jqlExpression.getQName();
            str.append(JqlExpressionBuilder.getFqString(qName.getNamespaceElements(), qName.getName()));
        }
        for (Feature feature : jqlExpression.getFeatures()) {
            str.append(".");
            str.append(feature.getName());
        }
        return str.toString();
    }

    private JqlNavigationFeatureTransformer.JqlFeatureTransformResult<C> findBase(NavigationExpression navigation, ExpressionBuildingVariableResolver context) {
        Expression baseExpression = null;
        C navigationBase;
        if (navigation.getBase() != null) {
            JqlNavigationFeatureTransformer.JqlFeatureTransformResult<C> base = findBase((NavigationExpression) navigation.getBase(), context);
            baseExpression = base.baseExpression;
            navigationBase = base.navigationBase;
        } else {
            TypeName typeName = jqlTransformers.getTypeNameFromResource(navigation.getQName());
            if (typeName != null) {
                if (getModelAdapter().isSequence(getModelAdapter().get(typeName).get())) {
                    baseExpression = newStaticSequenceBuilder().withTypeName(typeName).build();
                    navigationBase = null;
                } else {
                    baseExpression = newImmutableCollectionBuilder().withElementName(typeName).build();
                    navigationBase = (C) getModelAdapter().get(typeName).get();
                }
            } else {
                Expression contextBaseExpression = context.peekBaseExpression();
                String name = navigation.getQName().getName();
                if (name.equals(JqlExpressionBuilder.SELF_NAME) && contextBaseExpression != null) {
                    baseExpression = EcoreUtil.copy(contextBaseExpression);
                    navigationBase = (C) context.peekBase();
                } else {
                    Variable baseVariable = context.resolveVariable(name).orElseThrow(() -> new IllegalStateException("Base variable " + navigation.getBase() + " not found"));
                    navigationBase = null;
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
                    } else if (baseVariable instanceof CustomVariable) {
                        baseExpression = newCustomVariableReferenceBuilder().withVariable((CustomVariable) baseVariable).build();
                    } else if (baseVariable instanceof EnumerationVariable) {
                        baseExpression = newEnumerationVariableReferenceBuilder().withVariable((EnumerationVariable) baseVariable).build();
                    }
                }
            }
        }
        return getFeatureTransformer().transform(navigation.getFeatures(), baseExpression, navigationBase, context);
    }

    public Expression doTransform(NavigationExpression jqlExpression, ExpressionBuildingVariableResolver context) {
//        NavigationExpression navigation = flattenNavigationExpression(jqlExpression);
        NavigationExpression navigation = jqlExpression;
        LOG.debug("Transform navigation: {}", navigationString(navigation));
        JqlNavigationFeatureTransformer.JqlFeatureTransformResult<C> base = findBase(jqlExpression, context);
        Expression baseExpression = base.baseExpression;
        C navigationBase = base.navigationBase;
        LOG.debug("Base: {} ({})", baseExpression, getModelAdapter().getTypeName(navigationBase).orElse(null));
        return baseExpression;
    }

}
