package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.builder.jql.*;
import hu.blackbelt.judo.meta.expression.collection.CastCollection;
import hu.blackbelt.judo.meta.expression.object.CastObject;
import hu.blackbelt.judo.meta.expression.variable.*;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newObjectSequenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newStaticSequenceBuilder;

public class JqlNavigationTransformer<NE, P extends NE, PTE, E extends P, C extends NE, AP extends NE, RTE, S, M, U> extends AbstractJqlExpressionTransformer<NavigationExpression, NE, P, PTE, E, C, AP, RTE, S, M, U> {

    private static final Logger LOG = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());

    public JqlNavigationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    private static String navigationString(NavigationExpression jqlExpression) {
        StringBuilder str = new StringBuilder();
        if (!jqlExpression.getBase().getNamespaceElements().isEmpty()) {
            str.append(jqlExpression.getBase().getNamespaceElements());
            str.append("::");
        }
        str.append(jqlExpression.getBase().getName());
        for (Feature feature : jqlExpression.getFeatures()) {
            str.append(".");
            str.append(feature.getName());
            for (FunctionCall f : feature.getFunctions()) {
                str.append("!");
                str.append(f.getFunction().getName());
            }
        }
        return str.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Expression doTransform(hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        LOG.debug("Transform navigation: {}", navigationString(jqlExpression));
        Expression baseExpression = null;
        C navigationBase;

        TypeName typeName = jqlTransformers.getTypeNameFromResource(jqlExpression.getBase());
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
            String name = jqlExpression.getBase().getName();
            if (name.equals(JqlExpressionBuilder.SELF_NAME) && contextBaseExpression != null) {
                baseExpression = EcoreUtil.copy(contextBaseExpression);
                navigationBase = (C) context.peekBase();
            } else {
                Variable baseVariable = context.resolveVariable(name).orElseThrow(() -> new IllegalStateException("Base variable " + jqlExpression.getBase() + " not found"));
                navigationBase = null;
                if (baseVariable instanceof ObjectVariable) {
                    navigationBase = (C) ((ObjectVariable) baseVariable).getObjectType(getModelAdapter());
                    baseExpression = newObjectVariableReferenceBuilder().withVariable((ObjectVariable) baseVariable).build();
                } else if (baseVariable instanceof CollectionVariable) {
                    navigationBase = (C) ((CollectionVariable) baseVariable).getObjectType(getModelAdapter());
                    baseExpression = newCollectionVariableReferenceBuilder().withVariable((CollectionVariable) baseVariable).build();
                } else if (baseVariable instanceof IntegerVariable) {
                    baseExpression = newIntegerVariableReferenceBuilder().withVariable((IntegerVariable)baseVariable).build();
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
        LOG.debug("Base: {} ({})", baseExpression, getModelAdapter().getTypeName(navigationBase).orElse(null));
        baseExpression = jqlTransformers.applyFunctions(jqlExpression.getFunctions(), baseExpression, context);
        for (Feature jqlFeature : jqlExpression.getFeatures()) {
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
                baseExpression = jqlTransformers.applyFunctions(jqlFeature.getFunctions(), baseExpression, context);
            } else if (isExtendedFeaturePresent(jqlExpression, jqlFeature, context)) {
            	JqlFeatureTransformResult<C> transformResult = transformExtendedFeature(jqlExpression, context, baseExpression, navigationBase, jqlFeature);
            	baseExpression = transformResult.baseExpression;
            	navigationBase = transformResult.navigationBase;
            } else {
                LOG.error("Feature {} of {} not found", jqlFeature.getName(), navigationBase);
                throw new IllegalStateException(String.format("Feature %s of %s not found", jqlFeature.getName(), getModelAdapter().getTypeName(navigationBase).orElse(null)));
            }
        }
        return baseExpression;
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
        resultBaseExpression = jqlTransformers.applyFunctions(jqlFeature.getFunctions(), resultBaseExpression, context);
        if (resultBaseExpression instanceof CastCollection) {
            CastCollection castCollection = (CastCollection) resultBaseExpression;
            resultNavigationBase = (C) castCollection.getElementName().get(getModelAdapter());
        } else if (resultBaseExpression instanceof CastObject) {
            CastObject castObject = (CastObject) resultBaseExpression;
            resultNavigationBase = (C) castObject.getElementName().get(getModelAdapter());
        } else {
            resultNavigationBase = getModelAdapter().getTarget(accessor);
        }
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
        return new JqlFeatureTransformResult<>(null, jqlTransformers.applyFunctions(jqlFeature.getFunctions(), resultBaseExpression, context));
    }

    protected boolean isExtendedFeaturePresent(NavigationExpression jqlExpression, Feature jqlFeature, ExpressionBuildingVariableResolver context) {
        return false;
    }

    protected JqlFeatureTransformResult<C> transformExtendedFeature(NavigationExpression jqlExpression,
			ExpressionBuildingVariableResolver context, Expression baseExpression, C navigationBase,
			Feature jqlFeature) {
		return null;
	}

    protected static class JqlFeatureTransformResult<C> {
        public C navigationBase;
        public Expression baseExpression;

        public JqlFeatureTransformResult(C navigationBase, Expression baseExpression) {
            this.navigationBase = navigationBase;
            this.baseExpression = baseExpression;
        }
    }

}
