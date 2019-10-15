package hu.blackbelt.judo.meta.expression.builder.jql.operation;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.operator.DecimalOperator;
import hu.blackbelt.judo.meta.expression.operator.IntegerComparator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.BinaryOperation;

import java.util.List;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.newIntegerComparisonBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalAritmeticExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAritmeticExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newConcatenateBuilder;

public class JqlBinaryOperationTransformer<NE, P, PTE, E, C extends NE, RTE, M, U> extends AbstractJqlExpressionTransformer<BinaryOperation, NE, P, PTE, E, C, RTE, M, U> {

    public JqlBinaryOperationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(BinaryOperation binaryOperation, List<ObjectVariable> variables) {
        Expression left = jqlTransformers.transform(binaryOperation.getLeftOperand(), variables);
        Expression right = jqlTransformers.transform(binaryOperation.getRightOperand(), variables);
        String operator = binaryOperation.getOperator();
        if ((left instanceof IntegerExpression) && (right instanceof IntegerExpression)) {
            switch (operator) {
                case "+":
                    return newIntegerAritmeticExpressionBuilder()
                            .withLeft((IntegerExpression) left)
                            .withOperator(IntegerOperator.ADD)
                            .withRight((IntegerExpression) right)
                            .build();
                case "-":
                    return newIntegerAritmeticExpressionBuilder()
                            .withLeft((IntegerExpression) left)
                            .withOperator(IntegerOperator.SUBSTRACT)
                            .withRight((IntegerExpression) right)
                            .build();
                case "*":
                    return newIntegerAritmeticExpressionBuilder()
                            .withLeft((IntegerExpression) left)
                            .withOperator(IntegerOperator.MULTIPLY)
                            .withRight((IntegerExpression) right)
                            .build();
                case "div":
                    return newIntegerAritmeticExpressionBuilder()
                            .withLeft((IntegerExpression) left)
                            .withOperator(IntegerOperator.DIVIDE)
                            .withRight((IntegerExpression) right)
                            .build();
                case "mod":
                    return newIntegerAritmeticExpressionBuilder()
                            .withLeft((IntegerExpression) left)
                            .withOperator(IntegerOperator.MODULO)
                            .withRight((IntegerExpression) right)
                            .build();
                case "/":
                    return newDecimalAritmeticExpressionBuilder()
                            .withLeft((NumericExpression) left)
                            .withOperator(DecimalOperator.DIVIDE)
                            .withRight((NumericExpression) right)
                            .build();
                case ">":
                    return newIntegerComparisonBuilder().withLeft((IntegerExpression) left).withOperator(IntegerComparator.GREATER_THAN).withRight((IntegerExpression) right).build();
                default:
                    throw new UnsupportedOperationException("Invalid integer operation: " + operator);
            }
        } else if ((left instanceof NumericExpression) && (right instanceof NumericExpression)) {
            switch (operator) {
                case "+":
                    return newDecimalAritmeticExpressionBuilder()
                            .withLeft((NumericExpression) left)
                            .withOperator(DecimalOperator.ADD)
                            .withRight((NumericExpression) right)
                            .build();
                case "-":
                    return newDecimalAritmeticExpressionBuilder()
                            .withLeft((NumericExpression) left)
                            .withOperator(DecimalOperator.SUBSTRACT)
                            .withRight((NumericExpression) right)
                            .build();
                case "*":
                    return newDecimalAritmeticExpressionBuilder()
                            .withLeft((NumericExpression) left)
                            .withOperator(DecimalOperator.MULTIPLY)
                            .withRight((NumericExpression) right)
                            .build();
                case "/":
                    return newDecimalAritmeticExpressionBuilder()
                            .withLeft((NumericExpression) left)
                            .withOperator(DecimalOperator.DIVIDE)
                            .withRight((NumericExpression) right)
                            .build();
                default:
                    throw new UnsupportedOperationException("Invalid numeric operation: " + operator);
            }
        } else if (left instanceof StringExpression && right instanceof StringExpression) {
            switch (operator) {
                case "+":
                    return newConcatenateBuilder().withLeft((StringExpression) left).withRight((StringExpression) right).build();
                default:
                    throw new UnsupportedOperationException("Invalid string opration: " + operator);
            }
        } else if (left instanceof LogicalExpression && right instanceof LogicalExpression) {
            return null;
        } else {
            throw new UnsupportedOperationException("Not supported operand types");
        }
    }
}
