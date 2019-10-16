package hu.blackbelt.judo.meta.expression.builder.jql.operation;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.custom.util.builder.CustomSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.numeric.util.builder.DecimalSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.numeric.util.builder.IntegerSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.object.util.builder.ObjectSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.string.util.builder.StringSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.temporal.util.builder.DateSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.temporal.util.builder.TimestampSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.util.builder.SwitchCaseBuilder;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.TernaryOperation;

import java.util.*;
import java.util.function.BiFunction;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.custom.util.builder.CustomBuilders.newCustomSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationBuilders.newEnumerationSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newStringSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateSwitchExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampSwitchExpressionBuilder;

public class JqlTernaryOperationTransformer<NE, P, PTE, E, C extends NE, RTE, M, U> extends AbstractJqlExpressionTransformer<TernaryOperation, NE, P, PTE, E, C, RTE, M, U> {

    private static final Set<SupportedType> SUPPORTED_TYPES = new HashSet<SupportedType>() {
        {
            add(new SupportedType(IntegerExpression.class, (thenCase, elseExpression) -> newIntegerSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build()));
            add(new SupportedType(DecimalExpression.class, (thenCase, elseExpression) -> newDecimalSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build()));
            add(new SupportedType(CollectionExpression.class, (thenCase, elseExpression) -> newCollectionSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build()));
            add(new SupportedType(CustomExpression.class, (thenCase, elseExpression) -> newCustomSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build()));
            add(new SupportedType(EnumerationExpression.class, (thenCase, elseExpression) -> newEnumerationSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build()));
            add(new SupportedType(DateExpression.class, (thenCase, elseExpression) -> newDateSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build()));
            add(new SupportedType(ObjectExpression.class, (thenCase, elseExpression) -> newObjectSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build()));
            add(new SupportedType(StringExpression.class, (thenCase, elseExpression) -> newStringSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build()));
            add(new SupportedType(TimestampExpression.class, (thenCase, elseExpression) -> newTimestampSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build()));
        }
    };

    public JqlTernaryOperationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(TernaryOperation ternaryOperation, List<ObjectVariable> variables) {
        Expression condition = jqlTransformers.transform(ternaryOperation.getCondition(), variables);
        Expression thenExpression = jqlTransformers.transform(ternaryOperation.getThenExpression(), variables);
        Expression elseExpression = jqlTransformers.transform(ternaryOperation.getElseExpression(), variables);
        validateExpressionTypes(condition, elseExpression, thenExpression);
        SwitchCase thenCase = SwitchCaseBuilder.create().withCondition((LogicalExpression) condition).withExpression((DataExpression) thenExpression).build();
        SupportedType resultType = findResultType(elseExpression, thenExpression);
        return resultType.builderFunction.apply(thenCase, (DataExpression) elseExpression);
    }

    private void validateExpressionTypes(Expression condition, Expression elseExpression, Expression thenExpression) {
        if (!LogicalExpression.class.isInstance(condition)) {
            throw new IllegalArgumentException("Not a logical expression: " + condition);
        }
        if (!DataExpression.class.isInstance(elseExpression)) {
            throw new IllegalArgumentException("Not a data expression: " + elseExpression);
        }
        if (!DataExpression.class.isInstance(thenExpression)) {
            throw new IllegalArgumentException("Not a data expression: " + elseExpression);
        }
    }

    private SupportedType findResultType(Expression thenExpression, Expression elseExpression) {
        for (SupportedType supportedType : SUPPORTED_TYPES) {
            if (supportedType.type.isAssignableFrom(thenExpression.getClass())) {
                if (!supportedType.type.isAssignableFrom(elseExpression.getClass())) {
                    throw new IllegalArgumentException(String.format("THEN and ELSE part type differs: %s vs %s", thenExpression.getClass(), elseExpression.getClass()));
                } else {
                    return supportedType;
                }
            }
        }
        throw new IllegalArgumentException("Not supported result type for ternary operation: " + thenExpression.getClass());
    }

    private static final class SupportedType {

        private Class<? extends Expression> type;
        private BiFunction<SwitchCase, DataExpression, SwitchExpression> builderFunction;

        private SupportedType(Class<? extends Expression> type, BiFunction<SwitchCase, DataExpression, SwitchExpression> builderFunction) {
            this.type = type;
            this.builderFunction = builderFunction;
        }
    }

}
