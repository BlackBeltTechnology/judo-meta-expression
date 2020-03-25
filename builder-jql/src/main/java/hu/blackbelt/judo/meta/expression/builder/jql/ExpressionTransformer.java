package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlExpression;

public interface ExpressionTransformer {
    Expression transform(JqlExpression jqlExpression, ExpressionBuildingVariableResolver context);
}
