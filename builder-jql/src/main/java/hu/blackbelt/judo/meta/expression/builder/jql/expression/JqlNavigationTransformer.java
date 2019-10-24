package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.collection.CastCollection;
import hu.blackbelt.judo.meta.expression.object.CastObject;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newImmutableCollectionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;

public class JqlNavigationTransformer<NE, P, PTE, E, C extends NE, RTE, M, U> extends AbstractJqlExpressionTransformer<NavigationExpression, NE, P, PTE, E, C, RTE, M, U> {

    private static final Logger LOG = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());

    public JqlNavigationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression doTransform(hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression jqlExpression, List<ObjectVariable> variables) {
        Expression baseExpression;
        C navigationBase;
        TypeName typeName = jqlTransformers.getTypeNameFromResource(jqlExpression.getBase());
        if (typeName != null) {
            baseExpression = newImmutableCollectionBuilder().withElementName(typeName).build();
            navigationBase = (C) getModelAdapter().get(typeName).get();
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
        baseExpression = jqlTransformers.applyFunctions(jqlExpression, baseExpression, variables);
        for (final Feature jqlFeature : jqlExpression.getFeatures()) {
            final Optional<? extends PTE> attribute = getModelAdapter().getAttribute(navigationBase, jqlFeature.getName());
            final Optional<? extends RTE> reference = getModelAdapter().getReference(navigationBase, jqlFeature.getName());
            if (attribute.isPresent()) {
                //TODO - handle data expressions that are not attribute selectors (necessary for getters only, setters must be selectors!)
                baseExpression = createAttributeSelector(attribute.get(), jqlFeature.getName(), (ObjectExpression) baseExpression);
                baseExpression = jqlTransformers.applySelectorFunctions(jqlFeature, baseExpression, variables);
                navigationBase = null;
            } else if (reference.isPresent()) {
                //TODO - handle reference expressions that are not reference selectors (necessary for getters only, setters must be selectors!)
                baseExpression = createReferenceSelector(reference.get(), jqlFeature.getName(), (ReferenceExpression) baseExpression);
                baseExpression = jqlTransformers.applySelectorFunctions(jqlFeature, baseExpression, variables);
                if (baseExpression instanceof CastCollection) {
                    CastCollection castCollection = (CastCollection) baseExpression;
                    navigationBase = (C) castCollection.getElementName().get(getModelAdapter());
                } else if (baseExpression instanceof CastObject) {
                    CastObject castObject = (CastObject) baseExpression;
                    navigationBase = (C) castObject.getElementName().get(getModelAdapter());
                } else {
                    navigationBase = getModelAdapter().getTarget(reference.get());
                }
            } else {
                LOG.error("Feature {} of {} not found", jqlFeature.getName(), navigationBase);
                throw new IllegalStateException("Feature not found");
            }
        }
        return baseExpression;
    }

}
