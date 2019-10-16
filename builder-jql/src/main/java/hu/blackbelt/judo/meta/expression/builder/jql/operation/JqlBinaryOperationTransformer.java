package hu.blackbelt.judo.meta.expression.builder.jql.operation;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.logical.EnumerationComparison;
import hu.blackbelt.judo.meta.expression.operator.*;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.BinaryOperation;

import java.util.Enumeration;
import java.util.List;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;
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
            return createIntegerOperation((IntegerExpression) left, (IntegerExpression) right, operator);
        } else if ((left instanceof NumericExpression) && (right instanceof NumericExpression)) {
            return createDecimalOperation((NumericExpression) left, (NumericExpression) right, operator);
        } else if (left instanceof StringExpression && right instanceof StringExpression) {
            return createStringOperation((StringExpression) left, (StringExpression) right, operator);
        } else if (left instanceof LogicalExpression && right instanceof LogicalExpression) {
            return createLogicalOperation((LogicalExpression) left, (LogicalExpression) right, operator);
        } else if (left instanceof EnumerationExpression && right instanceof EnumerationExpression) {
            return createEnumerationOperation((EnumerationExpression)left, (EnumerationExpression)right, operator);
        } else {
            throw new UnsupportedOperationException(String.format("Not supported operand types: %s %s %s", left.getClass(), binaryOperation, right.getClass()));
        }
    }

    private Expression createEnumerationOperation(EnumerationExpression left, EnumerationExpression right, String operator) {
        switch (operator) {
            case "=":
                return newEnumerationComparisonBuilder().withLeft(left).withRight(right).withOperator(IntegerComparator.EQUAL).build();
            default:
                throw new UnsupportedOperationException("Invalid enumeration operation: " + operator);
        }
    }

    private Expression createLogicalOperation(LogicalExpression left, LogicalExpression right, String operator) {
        switch (operator) {
            case "and":
                return newKleeneExpressionBuilder().withLeft(left).withRight(right).withOperator(LogicalOperator.AND).build();
            case "or":
                return newKleeneExpressionBuilder().withLeft(left).withRight(right).withOperator(LogicalOperator.OR).build();
            case "xor":
                return newKleeneExpressionBuilder().withLeft(left).withRight(right).withOperator(LogicalOperator.XOR).build();
            case "implies":
                return newKleeneExpressionBuilder().withLeft(left).withRight(right).withOperator(LogicalOperator.IMPLIES).build();
            default:
                throw new UnsupportedOperationException("Invalid logical operation: " + operator);
        }
    }

    private Expression createStringOperation(StringExpression left, StringExpression right, String operator) {
        switch (operator) {
            case "+":
                return newConcatenateBuilder().withLeft(left).withRight(right).build();
            case "<":
                return newStringComparisonBuilder().withLeft(left).withRight(right).withOperator(StringComparator.LESS_THAN).build();
            case ">":
                return newStringComparisonBuilder().withLeft(left).withRight(right).withOperator(StringComparator.GREATER_THAN).build();
            case "<=":
                return newStringComparisonBuilder().withLeft(left).withRight(right).withOperator(StringComparator.LESS_OR_EQUAL).build();
            case ">=":
                return newStringComparisonBuilder().withLeft(left).withRight(right).withOperator(StringComparator.GREATER_OR_EQUAL).build();
            case "=":
                return newStringComparisonBuilder().withLeft(left).withRight(right).withOperator(StringComparator.EQUAL).build();
            case "<>":
                return newStringComparisonBuilder().withLeft(left).withRight(right).withOperator(StringComparator.NOT_EQUAL).build();
            default:
                throw new UnsupportedOperationException("Invalid string operation: " + operator);
        }
    }

    private Expression createDecimalOperation(NumericExpression left, NumericExpression right, String operator) {
        switch (operator) {
            case "+":
                return newDecimalAritmeticExpressionBuilder().withLeft(left).withRight(right).withOperator(DecimalOperator.ADD).build();
            case "-":
                return newDecimalAritmeticExpressionBuilder().withLeft(left).withRight(right).withOperator(DecimalOperator.SUBSTRACT).build();
            case "*":
                return newDecimalAritmeticExpressionBuilder().withLeft(left).withRight(right).withOperator(DecimalOperator.MULTIPLY).build();
            case "/":
                return newDecimalAritmeticExpressionBuilder().withLeft(left).withRight(right).withOperator(DecimalOperator.DIVIDE).build();
            case "<":
                return newDecimalComparisonBuilder().withLeft(left).withRight(right).withOperator(DecimalComparator.LESS_THAN).build();
            case ">":
                return newDecimalComparisonBuilder().withLeft(left).withRight(right).withOperator(DecimalComparator.GREATER_THAN).build();
            default:
                throw new UnsupportedOperationException("Invalid numeric operation: " + operator);
        }
    }

    private Expression createIntegerOperation(IntegerExpression left, IntegerExpression right, String operator) {
        switch (operator) {
            case "+":
                return newIntegerAritmeticExpressionBuilder().withLeft((IntegerExpression) left).withOperator(IntegerOperator.ADD).withRight((IntegerExpression) right).build();
            case "-":
                return newIntegerAritmeticExpressionBuilder().withLeft((IntegerExpression) left).withOperator(IntegerOperator.SUBSTRACT).withRight((IntegerExpression) right).build();
            case "*":
                return newIntegerAritmeticExpressionBuilder().withLeft((IntegerExpression) left).withOperator(IntegerOperator.MULTIPLY).withRight((IntegerExpression) right).build();
            case "div":
                return newIntegerAritmeticExpressionBuilder().withLeft((IntegerExpression) left).withOperator(IntegerOperator.DIVIDE).withRight((IntegerExpression) right).build();
            case "mod":
                return newIntegerAritmeticExpressionBuilder().withLeft((IntegerExpression) left).withOperator(IntegerOperator.MODULO).withRight((IntegerExpression) right).build();
            case "/":
                return newDecimalAritmeticExpressionBuilder().withLeft(left).withOperator(DecimalOperator.DIVIDE).withRight(right).build();
            case "<":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(IntegerComparator.LESS_THAN).build();
            case ">":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(IntegerComparator.GREATER_THAN).build();
            case "<=":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(IntegerComparator.LESS_OR_EQUAL).build();
            case ">=":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(IntegerComparator.GREATER_OR_EQUAL).build();
            case "=":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(IntegerComparator.EQUAL).build();
            case "<>":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(IntegerComparator.NOT_EQUAL).build();
            default:
                throw new UnsupportedOperationException("Invalid integer operation: " + operator);
        }
    }

}
