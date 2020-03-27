package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.operator.DecimalAggregator;
import hu.blackbelt.judo.meta.expression.operator.IntegerAggregator;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAggregatedExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAggregatedExpressionBuilder;

public class JqlAggregatedExpressionTransformer extends AbstractJqlFunctionTransformer<CollectionExpression> {

    private final IntegerAggregator integerAggregator;
    private final DecimalAggregator decimalAggregator;

    public JqlAggregatedExpressionTransformer(ExpressionTransformer jqlTransformers, IntegerAggregator integerAggregator, DecimalAggregator decimalAggregator) {
        super(jqlTransformers);
        this.integerAggregator = integerAggregator;
        this.decimalAggregator = decimalAggregator;
    }

    @Override
    public Expression apply(CollectionExpression collection, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        Expression parameter = jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
        if (parameter instanceof IntegerExpression && integerAggregator != null) {
            return newIntegerAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((IntegerExpression) parameter).withOperator(integerAggregator).build();
        } else if (parameter instanceof DecimalExpression || parameter instanceof IntegerExpression) {
            return newDecimalAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((NumericExpression) parameter).withOperator(decimalAggregator).build();
        } else {
            throw new IllegalArgumentException("Invalid expression for aggregation: " + parameter);
        }
    }
}
