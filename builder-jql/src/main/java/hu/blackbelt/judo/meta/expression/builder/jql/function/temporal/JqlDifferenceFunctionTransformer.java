package hu.blackbelt.judo.meta.expression.builder.jql.function.temporal;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionMeasureProvider;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.*;

import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateDifferenceExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampDifferenceExpressionBuilder;

public class JqlDifferenceFunctionTransformer extends AbstractJqlFunctionTransformer<DataExpression> {

    private ExpressionMeasureProvider measureProvider;

    public JqlDifferenceFunctionTransformer(ExpressionTransformer expressionTransformer, ExpressionMeasureProvider measureProvider) {
        super(expressionTransformer);
        this.measureProvider = measureProvider;
    }

    @Override
    public Expression apply(DataExpression argument, JqlFunction functionCall, ExpressionBuildingVariableResolver context) {
        if (argument instanceof DateExpression) {
            DateExpression startDate = (DateExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
            DateExpression endDate = (DateExpression) argument;
            return newDateDifferenceExpressionBuilder()
                    .withStartDate(startDate)
                    .withEndDate(endDate)
                    .withMeasure(findMeasure(functionCall))
                    .build();
        } else if (argument instanceof TimestampExpression) {
            TimestampExpression startTimestamp = (TimestampExpression) jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
            TimestampExpression endTimestamp = (TimestampExpression) argument;
            return newTimestampDifferenceExpressionBuilder()
                    .withStartTimestamp(startTimestamp)
                    .withEndTimestamp(endTimestamp)
                    .withMeasure(findMeasure(functionCall))
                    .build();
        } else {
            throw new IllegalArgumentException(("Unsupported argument type: " + argument));
        }
    }

    private MeasureName findMeasure(JqlFunction functionCall) {
        if ( functionCall.getParameters().size() > 1) {
            FunctionParameter measureNameParam = functionCall.getParameters().get(1);
            QualifiedName measureQName = ((NavigationExpression) measureNameParam.getExpression()).getBase();
            MeasureName measureName = measureProvider.getDurationMeasureName(measureQName);
            if (measureName == null) {
                throw new IllegalArgumentException("Measure is not a duration");
            }
            return measureName;
        } else {
            Optional<MeasureName> measureName = measureProvider.getDefaultDurationMeasure();
            return measureName.orElseThrow(() -> new IllegalArgumentException("No single duration measure in the model"));
        }
    }
}
