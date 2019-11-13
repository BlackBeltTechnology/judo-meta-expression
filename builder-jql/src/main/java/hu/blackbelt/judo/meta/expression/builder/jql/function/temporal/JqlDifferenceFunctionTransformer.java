package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.DataExpression;
import hu.blackbelt.judo.meta.expression.DateExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TimestampExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateDifferenceExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampDifferenceExpressionBuilder;

public class JqlDifferenceFunctionTransformer extends AbstractJqlFunctionTransformer<DataExpression> {

    public JqlDifferenceFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(DataExpression argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        if (argument instanceof DateExpression) {
            DateExpression startDate = (DateExpression) argument;
            DateExpression endDate = (DateExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), variables);
            return newDateDifferenceExpressionBuilder().withStartDate(startDate).withEndDate(endDate).build();
        } else if (argument instanceof TimestampExpression) {
            TimestampExpression startTimestamp = (TimestampExpression) argument;
            TimestampExpression endTimestamp = (TimestampExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), variables);
            return newTimestampDifferenceExpressionBuilder().withStartTimestamp(startTimestamp).withEndTimestamp(endTimestamp).build();
        } else {
            throw new IllegalArgumentException(("Unsupported argument type: " + argument));
        }
    }
}
