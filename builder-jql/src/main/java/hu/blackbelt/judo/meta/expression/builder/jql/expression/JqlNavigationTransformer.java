package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.Feature;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectVariableReferenceBuilder;

public class JqlNavigationTransformer<NE, P, PTE, E, C extends NE, RTE, M, U> extends AbstractJqlExpressionTransformer<NavigationExpression, NE, P, PTE, E, C, RTE, M, U> {

    private static final Logger LOG = LoggerFactory.getLogger(JqlExpressionBuilder.class.getName());

    public JqlNavigationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression doTransform(hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression jqlExpression, List<ObjectVariable> variables) {
        String name = jqlExpression.getBase().getName();
        final Optional<ObjectVariable> baseVariable = variables.stream()
                .filter(v -> Objects.equals(v.getName(), name))
                .findAny();
        if (!baseVariable.isPresent()) {
            throw new IllegalStateException("Base variable " + jqlExpression.getBase() + " not found");
        } else {
            Expression expression = newObjectVariableReferenceBuilder().withVariable(baseVariable.get()).build();
            C navigationBase = (C) baseVariable.get().getObjectType(getModelAdapter());
            for (final Feature jqlFeature : jqlExpression.getFeatures()) {
                final Optional<? extends PTE> attribute = getModelAdapter().getAttribute(navigationBase, jqlFeature.getName());
                final Optional<? extends RTE> reference = getModelAdapter().getReference(navigationBase, jqlFeature.getName());
                if (attribute.isPresent()) {
                    //TODO - handle data expressions that are not attribute selectors (necessary for getters only, setters must be selectors!)
                    expression = createAttributeSelector(attribute.get(), jqlFeature.getName(), (ObjectExpression) expression);
                    navigationBase = null;
                } else if (reference.isPresent()) {
                    //TODO - handle reference expressions that are not reference selectors (necessary for getters only, setters must be selectors!)
                    String lambdaArgumentName = null;
                    if (jqlExpression.getFunctions() != null && !jqlExpression.getFunctions().isEmpty()) {
                            lambdaArgumentName = jqlExpression.getFunctions().get(0).getLambdaArgument();
                    }
                    expression = createReferenceSelector(reference.get(), jqlFeature.getName(), (ReferenceExpression) expression);
                    navigationBase = getModelAdapter().getTarget(reference.get());
                } else {
                    LOG.error("Feature {} of {} not found", jqlFeature.getName(), navigationBase);
                    throw new IllegalStateException("Feature not found");
                }
            }
            return expression;
        }
    }

}
