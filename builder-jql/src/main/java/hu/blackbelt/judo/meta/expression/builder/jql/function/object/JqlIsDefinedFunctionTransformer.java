package hu.blackbelt.judo.meta.expression.builder.jql.function.object;

import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildingContext;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.object.ObjectNavigationExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;

public class JqlIsDefinedFunctionTransformer extends AbstractJqlFunctionTransformer<Expression> {

    private boolean checkDefined;

    public JqlIsDefinedFunctionTransformer(JqlTransformers jqlTransformers, boolean checkDefined) {
        super(jqlTransformers);
        this.checkDefined = checkDefined;
    }

    @Override
    public Expression apply(Expression expression, FunctionCall functionCall, JqlExpressionBuildingContext variables) {
        LogicalExpression comparisonExpression;
        if (expression instanceof AttributeSelector) {
            AttributeSelector attributeSelector = (AttributeSelector) expression;
            comparisonExpression = newUndefinedAttributeComparisonBuilder().withAttributeSelector(attributeSelector).build();
        } else if (expression instanceof ReferenceExpression) {
            ReferenceExpression referenceExpression = (ReferenceExpression) expression;
            comparisonExpression = newUndefinedNavigationComparisonBuilder().withReferenceExpression(referenceExpression).build();
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
