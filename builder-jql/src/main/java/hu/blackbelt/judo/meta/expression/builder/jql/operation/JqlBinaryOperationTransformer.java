package hu.blackbelt.judo.meta.expression.builder.jql.operation;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.AbstractJqlExpressionTransformer;
import hu.blackbelt.judo.meta.expression.operator.*;
import hu.blackbelt.judo.meta.expression.temporal.DateAdditionExpression;
import hu.blackbelt.judo.meta.expression.temporal.TimestampAdditionExpression;
import hu.blackbelt.judo.meta.jql.jqldsl.BinaryOperation;

import java.util.Objects;
import java.util.function.Supplier;

import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newDecimalArithmeticExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerArithmeticExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.newConcatenateBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newDateAdditionExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.temporal.util.builder.TemporalBuilders.newTimestampAdditionExpressionBuilder;

public class JqlBinaryOperationTransformer<ME, NE extends ME, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S extends ME, M, U> extends AbstractJqlExpressionTransformer<BinaryOperation, ME, NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> {

    public JqlBinaryOperationTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    protected Expression doTransform(BinaryOperation binaryOperation, ExpressionBuildingVariableResolver context) {
        Expression left = jqlTransformers.transform(binaryOperation.getLeftOperand(), context);
        Expression right = jqlTransformers.transform(binaryOperation.getRightOperand(), context);
        String operator = binaryOperation.getOperator();
        return createBinaryOperationExpression(left, right, operator);
    }

    public Expression createBinaryOperationExpression(Expression left, Expression right, String operator) {
        if ((left instanceof IntegerExpression) && (right instanceof IntegerExpression)) {
            return createIntegerOperation((IntegerExpression) left, (IntegerExpression) right, operator);
        } else if ((left instanceof NumericExpression) && (right instanceof NumericExpression)) {
            return createDecimalOperation((NumericExpression) left, (NumericExpression) right, operator);
        } else if (left instanceof StringExpression && right instanceof StringExpression) {
            return createStringOperation((StringExpression) left, (StringExpression) right, operator);
        } else if (left instanceof LogicalExpression && right instanceof LogicalExpression) {
            return createLogicalOperation((LogicalExpression) left, (LogicalExpression) right, operator);
        } else if (left instanceof EnumerationExpression && right instanceof EnumerationExpression) {
            return createEnumerationOperation((EnumerationExpression) left, (EnumerationExpression) right, operator);
        } else if (left instanceof DateExpression && right instanceof DateExpression) {
            return createDateOperation((DateExpression) left, (DateExpression) right, operator);
        } else if (left instanceof DateExpression && right instanceof NumericExpression || right instanceof DateExpression && left instanceof NumericExpression) {
            return createDateAdditionOperation(left, right, operator);
        } else if (left instanceof TimestampExpression && right instanceof TimestampExpression) {
            return createTimestampOperation((TimestampExpression) left, (TimestampExpression) right, operator);
        } else if (left instanceof TimestampExpression && right instanceof NumericExpression || right instanceof TimestampExpression && left instanceof NumericExpression) {
            return createTimestampAdditionOperation(left, right, operator);
        } else if (left instanceof ObjectExpression && right instanceof ObjectExpression) {
            return createObjectSelectorOperation((ObjectExpression) left, (ObjectExpression) right, operator);
        } else {
            throw new UnsupportedOperationException(String.format("Not supported operand types: %s %s %s", left.getClass(), operator, right.getClass()));
        }

    }

    private Expression createObjectSelectorOperation(ObjectExpression left, ObjectExpression right, String operator) {
        switch (operator) {
            case "==":
                return newObjectComparisonBuilder().withLeft(left).withRight(right).withOperator(ObjectComparator.EQUAL).build();
            case "!=":
                return newObjectComparisonBuilder().withLeft(left).withRight(right).withOperator(ObjectComparator.NOT_EQUAL).build();
            default:
                throw new UnsupportedOperationException(String.format("Not supported object selector operation: %s", operator));
        }
    }

    private Expression createTimestampOperation(TimestampExpression left, TimestampExpression right, String operator) {
        switch (operator) {
            case "==":
                return newTimestampComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.EQUAL).build();
            case "!=":
                return newTimestampComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.NOT_EQUAL).build();
            case "<":
                return newTimestampComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.LESS_THAN).build();
            case ">":
                return newTimestampComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.GREATER_THAN).build();
            case "<=":
                return newTimestampComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.LESS_OR_EQUAL).build();
            case ">=":
                return newTimestampComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.GREATER_OR_EQUAL).build();
            default:
                throw new UnsupportedOperationException("Invalid timestamp operation: " + operator);
        }
    }

    private Expression createTimestampAdditionOperation(Expression left, Expression right, String operator) {
        switch (operator) {
            case "+": {
                TimestampExpression timestamp = (TimestampExpression) (left instanceof TimestampExpression ? left : right);
                NumericExpression duration = (NumericExpression) (left instanceof NumericExpression ? left : right);
                TimestampAdditionExpression additionExpression = createMeasuredTemporalAddition(duration,
                        () -> newTimestampAdditionExpressionBuilder().withTimestamp(timestamp).withDuration(duration).withOperator(TemporalOperator.ADD).build());
                M measure = getModelAdapter().getMeasure(duration).get();
                U unit = getModelAdapter().getUnits(measure).get(0);
                ModelAdapter.UnitFraction durationRatio = getModelAdapter().getBaseDurationRatio(unit, ModelAdapter.DurationType.SECOND);
                additionExpression.setSecondDividend(durationRatio.getDividend());
                additionExpression.setSecondDivisor(durationRatio.getDivisor());
                return additionExpression;
            }
            case "-": {
                if (!(left instanceof TimestampExpression) || !(right instanceof NumericExpression)) {
                    throw new IllegalArgumentException(String.format("Arguments must be timestamp and measured integer, got %s, %s", left, right));
                }
                TimestampExpression date = (TimestampExpression) left;
                NumericExpression duration = (NumericExpression) right;
                TimestampAdditionExpression additionExpression = createMeasuredTemporalAddition(duration,
                        () -> newTimestampAdditionExpressionBuilder().withTimestamp(date).withDuration(duration).withOperator(TemporalOperator.SUBSTRACT).build());
                M measure = getModelAdapter().getMeasure(duration).get();
                U unit = getModelAdapter().getUnits(measure).get(0);
                ModelAdapter.UnitFraction durationRatio = getModelAdapter().getBaseDurationRatio(unit, ModelAdapter.DurationType.SECOND);
                additionExpression.setSecondDividend(durationRatio.getDividend());
                additionExpression.setSecondDivisor(durationRatio.getDivisor());
                return additionExpression;
            }
            default:
                throw new UnsupportedOperationException("Unsupported timestamp operation: " + operator);
        }
    }

    private Expression createDateAdditionOperation(Expression left, Expression right, String operator) {
        switch (operator) {
            case "+": {
                DateExpression date = (DateExpression) (left instanceof DateExpression ? left : right);
                NumericExpression duration = (NumericExpression) (left instanceof NumericExpression ? left : right);
                DateAdditionExpression additionExpression = createMeasuredTemporalAddition(duration,
                        () -> newDateAdditionExpressionBuilder().withExpression(date).withDuration(duration).withOperator(TemporalOperator.ADD).build());
                M measure = getModelAdapter().getMeasure(duration).get();
                U unit = getModelAdapter().getUnits(measure).get(0);
                ModelAdapter.UnitFraction durationRatio = getModelAdapter().getBaseDurationRatio(unit, ModelAdapter.DurationType.DAY);
                additionExpression.setDayDividend(durationRatio.getDividend());
                additionExpression.setDayDivisor(durationRatio.getDivisor());
                return additionExpression;
            }
            case "-": {
                if (!(left instanceof DateExpression) || !(right instanceof NumericExpression)) {
                    throw new IllegalArgumentException(String.format("Arguments must be date and measured integer, got %s, %s", left, right));
                }
                DateExpression date = (DateExpression) left;
                NumericExpression duration = (NumericExpression) right;
                DateAdditionExpression additionExpression = createMeasuredTemporalAddition(duration,
                        () -> newDateAdditionExpressionBuilder().withExpression(date).withDuration(duration).withOperator(TemporalOperator.SUBSTRACT).build());
                M measure = getModelAdapter().getMeasure(duration).get();
                U unit = getModelAdapter().getUnits(measure).get(0);
                ModelAdapter.UnitFraction durationRatio = getModelAdapter().getBaseDurationRatio(unit, ModelAdapter.DurationType.DAY);
                additionExpression.setDayDividend(durationRatio.getDividend());
                additionExpression.setDayDivisor(durationRatio.getDivisor());
                return additionExpression;
            }
            default:
                throw new UnsupportedOperationException("Unsupported date operation: " + operator);
        }
    }

    private <T extends Expression> T createMeasuredTemporalAddition(NumericExpression duration, Supplier<T> supplier) {
        return getModelAdapter().getMeasure(duration).filter(m -> getModelAdapter().getUnits(m).stream().anyMatch(getModelAdapter()::isDurationSupportingAddition)).
                map(unit -> supplier.get()).orElseThrow(() -> new UnsupportedOperationException("Operand is not a temporal duration"));
    }

    private Expression createDateOperation(DateExpression left, DateExpression right, String operator) {
        switch (operator) {
            case "<":
                return newDateComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.LESS_THAN).build();
            case ">":
                return newDateComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.GREATER_THAN).build();
            case "<=":
                return newDateComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.LESS_OR_EQUAL).build();
            case ">=":
                return newDateComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.GREATER_OR_EQUAL).build();
            case "==":
                return newDateComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.EQUAL).build();
            case "!=":
                return newDateComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.NOT_EQUAL).build();
            default:
                throw new UnsupportedOperationException("Invalid date operation: " + operator);
        }

    }

    private Expression createEnumerationOperation(EnumerationExpression left, EnumerationExpression right, String operator) {
        TypeName leftType = left.getEnumeration(getModelAdapter());
        TypeName rightType = right.getEnumeration(getModelAdapter());
        if (!(Objects.equals(leftType.getName(), rightType.getName()) && Objects.equals(leftType.getNamespace(), rightType.getNamespace()))) {
            throw new IllegalArgumentException(String.format("Operands are not of the same enumeration, %s <-> %s", leftType, rightType));
        }
        switch (operator) {
            case "==":
                return newEnumerationComparisonBuilder().withLeft(left).withRight(right).withOperator(ObjectComparator.EQUAL).build();
            case "!=":
                return newEnumerationComparisonBuilder().withLeft(left).withRight(right).withOperator(ObjectComparator.NOT_EQUAL).build();
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
            case "==":
                return newStringComparisonBuilder().withLeft(left).withRight(right).withOperator(StringComparator.EQUAL).build();
            case "!=":
                return newStringComparisonBuilder().withLeft(left).withRight(right).withOperator(StringComparator.NOT_EQUAL).build();
            default:
                throw new UnsupportedOperationException("Invalid string operation: " + operator);
        }
    }

    private Expression createDecimalOperation(NumericExpression left, NumericExpression right, String operator) {
        switch (operator) {
            case "*":
                return newDecimalArithmeticExpressionBuilder().withLeft(left).withRight(right).withOperator(DecimalOperator.MULTIPLY).build();
            case "/":
                return newDecimalArithmeticExpressionBuilder().withLeft(left).withRight(right).withOperator(DecimalOperator.DIVIDE).build();
        }
        
        if (left.isMeasured(getModelAdapter()) != right.isMeasured(getModelAdapter())) {
            throw new UnsupportedOperationException("Invalid numeric operation: " + operator + ", both sides must be meausered or not measured.");
        } else if (left.isMeasured(getModelAdapter()) && right.isMeasured(getModelAdapter()) &&
                !getModelAdapter().getMeasure(left).equals(getModelAdapter().getMeasure(right))) {
            throw new UnsupportedOperationException("Invalid measured operation: " + operator + ", measures of operation are not matching.");
        } else {
            switch (operator) {
                case "+":
                    return newDecimalArithmeticExpressionBuilder().withLeft(left).withRight(right).withOperator(DecimalOperator.ADD).build();
                case "-":
                    return newDecimalArithmeticExpressionBuilder().withLeft(left).withRight(right).withOperator(DecimalOperator.SUBSTRACT).build();
                case "<":
                    return newDecimalComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.LESS_THAN).build();
                case ">":
                    return newDecimalComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.GREATER_THAN).build();
                case "<=":
                    return newDecimalComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.LESS_OR_EQUAL).build();
                case ">=":
                    return newDecimalComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.GREATER_OR_EQUAL).build();
                case "==":
                    return newDecimalComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.EQUAL).build();
                case "!=":
                    return newDecimalComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.NOT_EQUAL).build();
                default:
                    throw new UnsupportedOperationException("Invalid numeric operation: " + operator);
            }
        }
    }

    private Expression createIntegerOperation(IntegerExpression left, IntegerExpression right, String operator) {
        switch (operator) {
            case "+":
                return newIntegerArithmeticExpressionBuilder().withLeft(left).withOperator(IntegerOperator.ADD).withRight(right).build();
            case "-":
                return newIntegerArithmeticExpressionBuilder().withLeft(left).withOperator(IntegerOperator.SUBSTRACT).withRight(right).build();
            case "*":
                return newIntegerArithmeticExpressionBuilder().withLeft(left).withOperator(IntegerOperator.MULTIPLY).withRight(right).build();
            case "div":
                return newIntegerArithmeticExpressionBuilder().withLeft(left).withOperator(IntegerOperator.DIVIDE).withRight(right).build();
            case "mod":
                return newIntegerArithmeticExpressionBuilder().withLeft(left).withOperator(IntegerOperator.MODULO).withRight(right).build();
            case "/":
                return newDecimalArithmeticExpressionBuilder().withLeft(left).withOperator(DecimalOperator.DIVIDE).withRight(right).build();
            case "<":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.LESS_THAN).build();
            case ">":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.GREATER_THAN).build();
            case "<=":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.LESS_OR_EQUAL).build();
            case ">=":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.GREATER_OR_EQUAL).build();
            case "==":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.EQUAL).build();
            case "!=":
                return newIntegerComparisonBuilder().withLeft(left).withRight(right).withOperator(NumericComparator.NOT_EQUAL).build();
            default:
                throw new UnsupportedOperationException("Invalid integer operation: " + operator);
        }
    }

}
