package hu.blackbelt.judo.meta.expression.builder.jql.operation;

import hu.blackbelt.judo.meta.expression.DecimalExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.LogicalExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.UnaryOperation;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newNegationExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalOppositeExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerOppositeExpressionBuilder;

public class JqlUnaryOperationTransformer<NE, P, PTE, E extends NE, C extends NE, RTE, M, U> extends AbstractJqlExpressionTransformer<UnaryOperation, NE, P, PTE, E, C, RTE, M, U> {

    public JqlUnaryOperationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(UnaryOperation unaryOperation, List<ObjectVariable> variables) {
        Expression operand = jqlTransformers.transform(unaryOperation.getOperand(), variables);
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
