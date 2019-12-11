package hu.blackbelt.judo.meta.expression.builder.jql.operation;

import hu.blackbelt.judo.meta.expression.DecimalExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuildingContext;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.jql.jqldsl.UnaryOperation;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newNegationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalOppositeExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerOppositeExpressionBuilder;

public class JqlUnaryOperationTransformer<NE, P extends NE, PTE, E extends P, C extends NE, RTE, S, M, U> extends AbstractJqlExpressionTransformer<UnaryOperation, NE, P, PTE, E, C, RTE, S, M, U> {

    public JqlUnaryOperationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(UnaryOperation unaryOperation, JqlExpressionBuildingContext context) {
        Expression operand = jqlTransformers.transform(unaryOperation.getOperand(), context);
        String operator = unaryOperation.getOperator();
        if (operand instanceof IntegerExpression) {
            if ("-".equals(operator)) {
                return newIntegerOppositeExpressionBuilder().withExpression((IntegerExpression) operand).build();
            }
            throw new UnsupportedOperationException("Invalid integer unary operation: " + operator);
        } else if (operand instanceof DecimalExpression) {
            if ("-".equals(operator)) {
                return newDecimalOppositeExpressionBuilder().withExpression((DecimalExpression) operand).build();
            }
            throw new UnsupportedOperationException("Invalid decimal unary operation: " + operator);
        } else if (operand instanceof LogicalExpression) {
            if ("not".equals(operator)) {
                return newNegationExpressionBuilder().withExpression((LogicalExpression) operand).build();
            }
            throw new UnsupportedOperationException("Invalid logical unary operation: " + operator);
        }
        throw new UnsupportedOperationException("Not supported operand type");
    }
}
