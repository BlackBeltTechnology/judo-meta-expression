package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.builder.jql.CircularReferenceException;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildingContext;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.collection.CastCollection;
import hu.blackbelt.judo.meta.expression.object.CastObject;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newImmutableCollectionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newObjectSequenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newStaticSequenceBuilder;

public class JqlNavigationTransformer<NE, P, PTE, E extends NE, C extends NE, RTE, S, M, U> extends AbstractJqlExpressionTransformer<NavigationExpression, NE, P, PTE, E, C, RTE, S, M, U> {

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
    public Expression doTransform(hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression jqlExpression, JqlExpressionBuildingContext context) {
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
            if (name.equals("self") && contextBaseExpression != null) {
                baseExpression = EcoreUtil.copy(contextBaseExpression);
                navigationBase = (C) context.peekBase();
            } else {
                final Optional<ObjectVariable> baseVariable = context.resolveVariable(name);
                if (!baseVariable.isPresent()) {
                    throw new IllegalStateException("Base variable " + jqlExpression.getBase() + " not found");
                }
                navigationBase = (C) baseVariable.get().getObjectType(getModelAdapter());
                baseExpression = newObjectVariableReferenceBuilder().withVariable(baseVariable.get()).build();
            }
        }
        LOG.debug("Base: {} ({})", baseExpression, getModelAdapter().getTypeName(navigationBase).orElse(null));
        baseExpression = jqlTransformers.applyFunctions(jqlExpression.getFunctions(), baseExpression, context);
        for (Feature jqlFeature : jqlExpression.getFeatures()) {
            Optional<? extends PTE> attribute = getModelAdapter().getAttribute(navigationBase, jqlFeature.getName());
            Optional<? extends RTE> reference = getModelAdapter().getReference(navigationBase, jqlFeature.getName());
            Optional<? extends S> sequence = getModelAdapter().getSequence(navigationBase, jqlFeature.getName());
            if (attribute.isPresent()) {
                if (getModelAdapter().isDerivedAttribute(attribute.get())) {
                    PTE accessor = attribute.get();
                    if (context.containsAccessor(accessor)) {
                        throw new CircularReferenceException(accessor.toString());
                    } else {
                        context.pushAccessor(accessor);
                    }
                    String getterExpression = getModelAdapter().getAttributeGetter(accessor).get();
                    context.pushBaseExpression(baseExpression);
                    context.pushBase(navigationBase);
                    baseExpression = jqlTransformers.getExpressionBuilder().createExpression(getterExpression, context);
                    context.popBaseExpression();
                    context.popBase();
                    context.popAccessor();
                } else {
                    baseExpression = createAttributeSelector(attribute.get(), jqlFeature.getName(), (ObjectExpression) baseExpression);
                }
                baseExpression = jqlTransformers.applyFunctions(jqlFeature.getFunctions(), baseExpression, context);
                navigationBase = null;
            } else if (reference.isPresent()) {
                if (getModelAdapter().isDerivedReference(reference.get())) {
                    RTE accessor = reference.get();
                    if (context.containsAccessor(accessor)) {
                        throw new CircularReferenceException(accessor.toString());
                    } else {
                        context.pushAccessor(accessor);
                    }
                    String getterExpression = getModelAdapter().getReferenceGetter(accessor).get();
                    context.pushBaseExpression(baseExpression);
                    context.pushBase(navigationBase);
                    baseExpression = jqlTransformers.getExpressionBuilder().createExpression(getterExpression, context);
                    context.popBaseExpression();
                    context.popBase();
                    context.popAccessor();
                } else {
                    baseExpression = createReferenceSelector(reference.get(), jqlFeature.getName(), (ReferenceExpression) baseExpression);
                }
                baseExpression = jqlTransformers.applyFunctions(jqlFeature.getFunctions(), baseExpression, context);
                if (baseExpression instanceof CastCollection) {
                    CastCollection castCollection = (CastCollection) baseExpression;
                    navigationBase = (C) castCollection.getElementName().get(getModelAdapter());
                } else if (baseExpression instanceof CastObject) {
                    CastObject castObject = (CastObject) baseExpression;
                    navigationBase = (C) castObject.getElementName().get(getModelAdapter());
                } else {
                    navigationBase = getModelAdapter().getTarget(reference.get());
                }
            } else if (sequence.isPresent()) {
                baseExpression = newObjectSequenceBuilder().withObjectExpression((ObjectExpression) baseExpression).withSequenceName(jqlFeature.getName()).build();
                baseExpression = jqlTransformers.applyFunctions(jqlFeature.getFunctions(), baseExpression, context);
            } else {
                LOG.error("Feature {} of {} not found", jqlFeature.getName(), navigationBase);
                throw new IllegalStateException(String.format("Feature %s of %s not found", jqlFeature.getName(), getModelAdapter().getTypeName(navigationBase).orElse(null)));
            }
        }
        return baseExpression;
    }

}
