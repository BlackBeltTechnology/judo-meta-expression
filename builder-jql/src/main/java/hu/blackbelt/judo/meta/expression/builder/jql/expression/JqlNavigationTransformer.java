package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.esm.structure.PrimitiveAccessor;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildingContext;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.collection.CastCollection;
import hu.blackbelt.judo.meta.expression.object.CastObject;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
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

    @SuppressWarnings("unchecked")
    @Override
    public Expression doTransform(hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression jqlExpression, JqlExpressionBuildingContext context) {
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
            String name = jqlExpression.getBase().getName();
            final Optional<ObjectVariable> baseVariable = context.getVariables().stream()
                    .filter(v -> Objects.equals(v.getName(), name))
                    .findAny();
            if (!baseVariable.isPresent()) {
                throw new IllegalStateException("Base variable " + jqlExpression.getBase() + " not found");
            }
            baseExpression = newObjectVariableReferenceBuilder().withVariable(baseVariable.get()).build();
            navigationBase = (C) baseVariable.get().getObjectType(getModelAdapter());
        }
        baseExpression = jqlTransformers.applyFunctions(jqlExpression.getFunctions(), baseExpression, context);
        for (Feature jqlFeature : jqlExpression.getFeatures()) {
            Optional<? extends PTE> attribute = getModelAdapter().getAttribute(navigationBase, jqlFeature.getName());
            Optional<? extends RTE> reference = getModelAdapter().getReference(navigationBase, jqlFeature.getName());
            Optional<? extends S> sequence = getModelAdapter().getSequence(navigationBase, jqlFeature.getName());
            if (attribute.isPresent()) {
                if (getModelAdapter().isDerived(attribute.get())) {
                    PrimitiveAccessor accessor = (PrimitiveAccessor) attribute.get();
                    if (context.containsAccessor(accessor)) {
                        throw new IllegalStateException("Already resolved accessor: " + accessor.getGetterExpression().getExpression());
                    } else {
                        context.pushAccessor(accessor);
                    }
                    String getterExpression = accessor.getGetterExpression().getExpression();
                    baseExpression = jqlTransformers.getExpressionBuilder().createExpression(navigationBase, getterExpression, context);
                    context.popAccessor();
                } else {
                    baseExpression = createAttributeSelector(attribute.get(), jqlFeature.getName(), (ObjectExpression) baseExpression);
                }
                baseExpression = jqlTransformers.applyFunctions(jqlFeature.getFunctions(), baseExpression, context);
                navigationBase = null;
            } else if (reference.isPresent()) {
                baseExpression = createReferenceSelector(reference.get(), jqlFeature.getName(), (ReferenceExpression) baseExpression);
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
                throw new IllegalStateException(String.format("Feature %s of %s not found", jqlFeature.getName(), navigationBase));
            }
        }
        return baseExpression;
    }

}
