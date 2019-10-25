package hu.blackbelt.judo.meta.expression.builder.jql.operation;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.expression.enumeration.util.builder.EnumerationSwitchExpressionBuilder;
import hu.blackbelt.judo.meta.expression.util.builder.SwitchCaseBuilder;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.TernaryOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private final Map<Class<? extends Expression>, BiFunction<SwitchCase, DataExpression, SwitchExpression>> supportedTypes = new HashMap<Class<? extends Expression>, BiFunction<SwitchCase, DataExpression, SwitchExpression>>() {
        {
            put(IntegerExpression.class, (thenCase, elseExpression) -> newIntegerSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(DecimalExpression.class, (thenCase, elseExpression) -> newDecimalSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(CollectionExpression.class, (thenCase, elseExpression) -> newCollectionSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(CustomExpression.class, (thenCase, elseExpression) -> newCustomSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(EnumerationExpression.class, (thenCase, elseExpression) -> {
                EnumerationSwitchExpressionBuilder builder = newEnumerationSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression);
                Object enumeration = ((EnumerationExpression) elseExpression).getEnumeration(getModelAdapter());
                return builder.withElementName((TypeName) enumeration).build();
            });
            put(DateExpression.class, (thenCase, elseExpression) -> newDateSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(ObjectExpression.class, (thenCase, elseExpression) -> newObjectSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(StringExpression.class, (thenCase, elseExpression) -> newStringSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
            put(TimestampExpression.class, (thenCase, elseExpression) -> newTimestampSwitchExpressionBuilder().withCases(thenCase).withDefaultExpression(elseExpression).build());
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
        BiFunction<SwitchCase, DataExpression, SwitchExpression> resultBuilder = findResultType(thenExpression, elseExpression);
        return resultBuilder.apply(thenCase, (DataExpression) elseExpression);
    }

    private void validateExpressionTypes(Expression condition, Expression elseExpression, Expression thenExpression) {
        if (!(condition instanceof LogicalExpression)) {
            throw new IllegalArgumentException("Not a logical expression: " + condition);
        }
        if (!(thenExpression instanceof DataExpression)) {
            throw new IllegalArgumentException("Not a data expression in THEN: " + thenExpression);
        }
        if (!(elseExpression instanceof DataExpression)) {
            throw new IllegalArgumentException("Not a data expression in ELSE: " + elseExpression);
        }
    }

    private BiFunction<SwitchCase, DataExpression, SwitchExpression> findResultType(Expression thenExpression, Expression elseExpression) {
        if (thenExpression instanceof NumericExpression && elseExpression instanceof NumericExpression && (thenExpression instanceof DecimalExpression || elseExpression instanceof DecimalExpression)) {
            return supportedTypes.get(DecimalExpression.class);
        }
        for (Map.Entry<Class<? extends Expression>, BiFunction<SwitchCase, DataExpression, SwitchExpression>> supportedTypeEntry : supportedTypes.entrySet()) {
            if (supportedTypeEntry.getKey().isAssignableFrom(thenExpression.getClass())) {
                if (!supportedTypeEntry.getKey().isAssignableFrom(elseExpression.getClass())) {
                    throw new IllegalArgumentException(String.format("THEN and ELSE part type differs: %s vs %s", thenExpression.getClass().getName(), elseExpression.getClass().getName()));
                } else {
                    if (supportedTypeEntry.getKey().isAssignableFrom(EnumerationExpression.class)) {
                        validateEnumTypes((EnumerationExpression) thenExpression, (EnumerationExpression) elseExpression);
                    }
                    return supportedTypeEntry.getValue();
                }
            }
        }
        throw new IllegalArgumentException("Not supported result type for ternary operation: " + thenExpression.getClass());
    }

    private void validateEnumTypes(EnumerationExpression thenExpression, EnumerationExpression elseExpression) {
        Object thenEnum = thenExpression.getEnumeration(getModelAdapter());
        Object elseEnum = elseExpression.getEnumeration(getModelAdapter());
        if (!Objects.equals(thenEnum, elseEnum)) {
            throw new IllegalArgumentException(String.format("THEN and ELSE part different enumerations: %s vs %s", thenEnum, elseEnum));
        }
    }

}
