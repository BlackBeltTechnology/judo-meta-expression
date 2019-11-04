package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.collection.CastCollection;
import hu.blackbelt.judo.meta.expression.object.CastObject;
import hu.blackbelt.judo.meta.expression.util.builder.ObjectSequenceBuilder;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newImmutableCollectionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newSequenceExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.*;

public class JqlNavigationTransformer<NE, P, PTE, E extends NE, C extends NE, RTE, S, M, U> extends AbstractJqlExpressionTransformer<NavigationExpression, NE, P, PTE, E, C, RTE, S, M, U> {

    private static final Logger LOG = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());

    public JqlNavigationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Expression doTransform(hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression jqlExpression, List<ObjectVariable> variables) {
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
            final Optional<ObjectVariable> baseVariable = variables.stream()
                    .filter(v -> Objects.equals(v.getName(), name))
                    .findAny();
            if (!baseVariable.isPresent()) {
                throw new IllegalStateException("Base variable " + jqlExpression.getBase() + " not found");
            }
            baseExpression = newObjectVariableReferenceBuilder().withVariable(baseVariable.get()).build();
            navigationBase = (C) baseVariable.get().getObjectType(getModelAdapter());
        }
        baseExpression = jqlTransformers.applyFunctions(jqlExpression.getFunctions(), baseExpression, variables);
        for (Feature jqlFeature : jqlExpression.getFeatures()) {
            Optional<? extends PTE> attribute = getModelAdapter().getAttribute(navigationBase, jqlFeature.getName());
            Optional<? extends RTE> reference = getModelAdapter().getReference(navigationBase, jqlFeature.getName());
            Optional<? extends S> sequence = getModelAdapter().getSequence(navigationBase, jqlFeature.getName());
            if (attribute.isPresent()) {
                //TODO - handle data expressions that are not attribute selectors (necessary for getters only, setters must be selectors!)
                baseExpression = createAttributeSelector(attribute.get(), jqlFeature.getName(), (ObjectExpression) baseExpression);
                baseExpression = jqlTransformers.applyFunctions(jqlFeature.getFunctions(), baseExpression, variables);
                navigationBase = null;
            } else if (reference.isPresent()) {
                //TODO - handle reference expressions that are not reference selectors (necessary for getters only, setters must be selectors!)
                baseExpression = createReferenceSelector(reference.get(), jqlFeature.getName(), (ReferenceExpression) baseExpression);
                baseExpression = jqlTransformers.applyFunctions(jqlFeature.getFunctions(), baseExpression, variables);
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
                baseExpression  = newObjectSequenceBuilder().withObjectExpression((ObjectExpression) baseExpression).withSequenceName(jqlFeature.getName()).build();
                baseExpression = jqlTransformers.applyFunctions(jqlFeature.getFunctions(), baseExpression, variables);
            } else {
                LOG.error("Feature {} of {} not found", jqlFeature.getName(), navigationBase);
                throw new IllegalStateException(String.format("Feature %s of %s not found", jqlFeature.getName(), navigationBase));
            }
        }
        return baseExpression;
    }

}
