package hu.blackbelt.judo.meta.expression.builder.jql.function.object;

import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.object.ObjectNavigationExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;

public class JqlIsDefinedFunctionTransformer extends AbstractJqlFunctionTransformer<Expression> {

    private boolean checkDefined;

    public JqlIsDefinedFunctionTransformer(ExpressionTransformer jqlTransformers, boolean checkDefined) {
        super(jqlTransformers);
        this.checkDefined = checkDefined;
    }

    @Override
    public Expression apply(Expression expression, JqlFunction functionCall, ExpressionBuildingVariableResolver variables) {
        LogicalExpression comparisonExpression;
        if (expression instanceof AttributeSelector) {
            AttributeSelector attributeSelector = (AttributeSelector) expression;
            comparisonExpression = newUndefinedAttributeComparisonBuilder().withAttributeSelector(attributeSelector).build();
        } else if (expression instanceof ObjectNavigationExpression) {
            ObjectNavigationExpression objectNavigationExpression = (ObjectNavigationExpression) expression;
            comparisonExpression = newUndefinedNavigationComparisonBuilder().withObjectNavigationExpression(objectNavigationExpression).build();
        } else {
            throw new IllegalArgumentException("Not an attribute selector or object navigation: " + expression);
        }
        if (checkDefined) {
            return newNegationExpressionBuilder().withExpression(comparisonExpression).build();
        } else {
            return comparisonExpression;
        }
    }
}
