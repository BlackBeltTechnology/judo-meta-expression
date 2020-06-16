package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.operator.*;
import hu.blackbelt.judo.meta.jql.jqldsl.JqlFunction;

import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAggregatedExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAggregatedExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringAggregatedExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateAggregatedExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAggregatedExpressionBuilder;

public class JqlAggregatedExpressionTransformer extends AbstractJqlFunctionTransformer<CollectionExpression> {

    private final IntegerAggregator integerAggregator;
    private final DecimalAggregator decimalAggregator;
    private final StringAggregator stringAggregator;
    private final DateAggregator dateAggregator;
    private final TimestampAggregator timestampAggregator;

    public JqlAggregatedExpressionTransformer(ExpressionTransformer jqlTransformers, IntegerAggregator integerAggregator,
                                              DecimalAggregator decimalAggregator, StringAggregator stringAggregator,
                                              DateAggregator dateAggregator, TimestampAggregator timestampAggregator) {
        super(jqlTransformers);
        this.integerAggregator = integerAggregator;
        this.decimalAggregator = decimalAggregator;
        this.stringAggregator = stringAggregator;
        this.dateAggregator = dateAggregator;
        this.timestampAggregator = timestampAggregator;
    }

    @Override
    public Expression apply(CollectionExpression collection, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        Expression parameter = jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
        if (parameter instanceof IntegerExpression && integerAggregator != null) {
            return newIntegerAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((IntegerExpression) parameter).withOperator(integerAggregator).build();
        } else if ((parameter instanceof DecimalExpression || parameter instanceof IntegerExpression) && decimalAggregator != null) {
            return newDecimalAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((NumericExpression) parameter).withOperator(decimalAggregator).build();
        } else if (parameter instanceof StringExpression && stringAggregator != null) {
            return newStringAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((StringExpression) parameter).withOperator(stringAggregator).build();
        } else if (parameter instanceof DateExpression && dateAggregator != null) {
            return newDateAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((DateExpression) parameter).withOperator(dateAggregator).build();
        } else if (parameter instanceof TimestampExpression && timestampAggregator != null) {
            return newTimestampAggregatedExpressionBuilder().withCollectionExpression(collection).withExpression((TimestampExpression) parameter).withOperator(timestampAggregator).build();
        } else {
            throw new IllegalArgumentException("Invalid expression for aggregation: " + parameter);
        }
    }
}
