package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.StringExpression;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.constant.JqlDateLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.constant.JqlTimestampLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlMeasuredLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlNavigationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlPositionFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlReplaceFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlSubstringFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlBinaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlTernaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlUnaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.string.util.builder.TrimBuilder;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.*;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;
import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;

public class JqlTransformers<NE, P, PTE, E, C extends NE, RTE, M, U> {

    private Map<Class<? extends hu.blackbelt.judo.meta.jql.jqldsl.Expression>, JqlExpressionTransformerFunction> transformers = new LinkedHashMap<>();
    private Map<String, JqlFunctionTransformer> functionTransformers = new LinkedHashMap<>();

    private ModelAdapter<NE, P, PTE, E, C, RTE, M, U> modelAdapter;

    public JqlTransformers(ModelAdapter<NE, P, PTE, E, C, RTE, M, U> modelAdapter) {
        this.modelAdapter = modelAdapter;
        transformers.put(NavigationExpression.class, new JqlNavigationTransformer<>(this));
        transformers.put(BooleanLiteral.class, (jqlExpression, variables) -> newBooleanConstantBuilder().withValue(((BooleanLiteral) jqlExpression).isIsTrue()).build());
        transformers.put(DecimalLiteral.class, (jqlExpression, variables) -> newDecimalConstantBuilder().withValue(((DecimalLiteral) jqlExpression).getValue()).build());
        transformers.put(IntegerLiteral.class, (jqlExpression, variables) -> newIntegerConstantBuilder().withValue(((IntegerLiteral) jqlExpression).getValue()).build());
        transformers.put(StringLiteral.class, (jqlExpression, variables) -> newStringConstantBuilder().withValue(((StringLiteral) jqlExpression).getValue()).build());
//        transformers.put(EnumLiteral.class, (jqlExpression, variables) -> {
//
//        });
        transformers.put(MeasuredLiteral.class, new JqlMeasuredLiteralTransformer(this));
        transformers.put(BinaryOperation.class, new JqlBinaryOperationTransformer<>(this));
        transformers.put(UnaryOperation.class, new JqlUnaryOperationTransformer<>(this));
        transformers.put(TernaryOperation.class, new JqlTernaryOperationTransformer<>(this));
        transformers.put(DateLiteral.class, new JqlDateLiteralTransformer());
        transformers.put(TimeStampLiteral.class, new JqlTimestampLiteralTransformer());


        functionTransformers.put("length", (expression, functionCall, variables) -> newLengthBuilder().withExpression((StringExpression)expression).build());
        functionTransformers.put("lowercase", (expression, functionCall, variables) -> newLowerCaseBuilder().withExpression((StringExpression)expression).build());
        functionTransformers.put("uppercase", (expression, functionCall, variables) -> newUpperCaseBuilder().withExpression((StringExpression)expression).build());
        functionTransformers.put("trim", (expression, functionCall, variables) -> newTrimBuilder().withExpression((StringExpression)expression).build());
        functionTransformers.put("substring", new JqlSubstringFunctionTransformer(this));
        functionTransformers.put("position", new JqlPositionFunctionTransformer(this));
        functionTransformers.put("replace", new JqlReplaceFunctionTransformer(this));
    }

    public Expression transform(hu.blackbelt.judo.meta.jql.jqldsl.Expression jqlExpression, List<ObjectVariable> variables) {
        Optional<JqlExpressionTransformerFunction> foundTransformer = transformers.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(jqlExpression.getClass()))
                .findAny()
                .map(Map.Entry::getValue);
        return foundTransformer
                .orElseThrow(() -> new UnsupportedOperationException("Not implemented transformation for " + jqlExpression.getClass()))
                .apply(jqlExpression, variables);
    }

    public Expression applyFunctions(Expression argument, List<FunctionCall> functionCalls, List<ObjectVariable> variables) {
        Expression result = argument;
        for (FunctionCall functionCall : functionCalls) {
            String functionName = functionCall.getFunction().getName().toLowerCase();
            JqlFunctionTransformer functionTransformer = functionTransformers.get(functionName);
            if (functionTransformer != null) {
                result = functionTransformer.apply(argument, functionCall, variables);
            } else {
                throw new IllegalStateException("Unknown function: " + functionName);
            }
        }
        return result;
    }

    public ModelAdapter<NE, P, PTE, E, C, RTE, M, U> getModelAdapter() {
        return modelAdapter;
    }

}
