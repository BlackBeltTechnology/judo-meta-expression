package hu.blackbelt.judo.meta.expression.builder.jql.expression;

import hu.blackbelt.judo.meta.expression.CollectionExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionedExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.NavigationExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JqlFunctionedExpressionTransformer<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U> extends AbstractJqlExpressionTransformer<FunctionedExpression, NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> {

    private static final Logger LOG = LoggerFactory.getLogger(JqlFunctionedExpressionTransformer.class.getName());

    public JqlFunctionedExpressionTransformer(JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(FunctionedExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        Expression operand = jqlTransformers.transform(jqlExpression.getOperand(), context);
        C objectType;
        if (operand instanceof ObjectExpression) {
            objectType = (C) ((ObjectExpression) operand).getObjectType(getModelAdapter());
        } else if (operand instanceof CollectionExpression) {
            objectType = (C) ((CollectionExpression) operand).getObjectType(getModelAdapter());
        } else {
            objectType = null;
        }
        return jqlTransformers.applyFunctions(jqlExpression, operand, objectType, context);
    }
}
