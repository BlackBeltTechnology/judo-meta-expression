package hu.blackbelt.judo.meta.expression.builder.jql.function.object;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newNegationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newUndefinedAttributeComparisonBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newUndefinedEnvironmentVariableComparisonBuilder;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newUndefinedNavigationComparisonBuilder;

import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.ObjectExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.variable.EnvironmentVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

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
        } else if (expression instanceof ObjectExpression) {
            ObjectExpression objectExpression = (ObjectExpression) expression;
            comparisonExpression = newUndefinedNavigationComparisonBuilder().withObjectNavigationExpression(objectExpression).build();
        } else if (expression instanceof EnvironmentVariable) {
            EnvironmentVariable environmentVariable = (EnvironmentVariable) expression;
            comparisonExpression = newUndefinedEnvironmentVariableComparisonBuilder().withEnvironmentVariable(environmentVariable).build();
        } else {
            throw new IllegalArgumentException("Not an attribute selector, object navigation or environment variable: " + expression);
        }
        if (checkDefined) {
            return newNegationExpressionBuilder().withExpression(comparisonExpression).build();
        } else {
            return comparisonExpression;
        }
    }
}
