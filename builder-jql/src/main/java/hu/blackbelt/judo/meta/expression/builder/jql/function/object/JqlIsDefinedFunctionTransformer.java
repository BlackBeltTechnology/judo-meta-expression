package hu.blackbelt.judo.meta.expression.builder.jql.function.object;

import hu.blackbelt.judo.meta.expression.AttributeSelector;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.object.ObjectNavigationExpression;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;

public class JqlIsDefinedFunctionTransformer extends AbstractJqlFunctionTransformer<Expression> {

    private boolean checkDefined;

    public JqlIsDefinedFunctionTransformer(JqlTransformers jqlTransformers, boolean checkDefined) {
        super(jqlTransformers);
        this.checkDefined = checkDefined;
    }

    @Override
    public Expression apply(Expression expression, FunctionCall functionCall, List<ObjectVariable> variables) {
        LogicalExpression comparisonExpression;
            if (expression instanceof AttributeSelector) {
                AttributeSelector attributeSelector = (AttributeSelector) expression;
                comparisonExpression =  newUndefinedAttributeComparisonBuilder().withObjectExpression(attributeSelector.getObjectExpression()).withAttributeName(attributeSelector.getAttributeName()).build();
            } else if (expression instanceof ObjectNavigationExpression) {
                ObjectNavigationExpression objectNavigationExpression = (ObjectNavigationExpression) expression;
                comparisonExpression =  newUndefinedNavigationComparisonBuilder().withObjectExpression(objectNavigationExpression.getObjectExpression()).build();
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
