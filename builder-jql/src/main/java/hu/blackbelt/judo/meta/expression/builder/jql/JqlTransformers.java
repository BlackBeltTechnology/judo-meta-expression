package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.constant.JqlDateLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.constant.JqlTimestampLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlEnumLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlMeasuredLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlNavigationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.collection.JqlAggregatedExpressionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.collection.JqlIntegerParamCollectionFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.collection.JqlJoinFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.collection.JqlSortFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.temporal.JqlDifferenceFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlBinaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlTernaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlUnaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.collection.CollectionNavigationFromObjectExpression;
import hu.blackbelt.judo.meta.expression.collection.SubCollectionExpression;
import hu.blackbelt.judo.meta.expression.numeric.CountExpression;
import hu.blackbelt.judo.meta.expression.numeric.IntegerAggregatedExpression;
import hu.blackbelt.judo.meta.expression.operator.DecimalAggregator;
import hu.blackbelt.judo.meta.expression.operator.IntegerAggregator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.*;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionFilterExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newSubCollectionExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;
import static org.eclipse.emf.ecore.util.EcoreUtil.copy;

public class JqlTransformers<NE, P, PTE, E, C extends NE, RTE, M, U> {

    private Map<Class<? extends JqlExpression>, JqlExpressionTransformerFunction> transformers = new LinkedHashMap<>();
    private Map<String, JqlFunctionTransformer> functionTransformers = new LinkedHashMap<>();

    private ModelAdapter<NE, P, PTE, E, C, RTE, M, U> modelAdapter;
    private Map<String, MeasureName> measureNames;

    private Map<String, TypeName> enumTypes;
    private Resource expressionResource;

    public JqlTransformers(ModelAdapter<NE, P, PTE, E, C, RTE, M, U> modelAdapter, Map<String, MeasureName> measureNames, Map<String, TypeName> enumTypes, Resource expressionResource) {
        this.modelAdapter = modelAdapter;
        this.measureNames = measureNames;
        this.enumTypes = enumTypes;
        this.expressionResource = expressionResource;
        transformers.put(NavigationExpression.class, new JqlNavigationTransformer<>(this));
        transformers.put(BooleanLiteral.class, (jqlExpression, variables) -> newBooleanConstantBuilder().withValue(((BooleanLiteral) jqlExpression).isIsTrue()).build());
        transformers.put(DecimalLiteral.class, (jqlExpression, variables) -> newDecimalConstantBuilder().withValue(((DecimalLiteral) jqlExpression).getValue()).build());
        transformers.put(IntegerLiteral.class, (jqlExpression, variables) -> newIntegerConstantBuilder().withValue(((IntegerLiteral) jqlExpression).getValue()).build());
        transformers.put(StringLiteral.class, (jqlExpression, variables) -> newStringConstantBuilder().withValue(((StringLiteral) jqlExpression).getValue()).build());
        transformers.put(MeasuredLiteral.class, new JqlMeasuredLiteralTransformer<>(this));
        transformers.put(BinaryOperation.class, new JqlBinaryOperationTransformer<>(this));
        transformers.put(UnaryOperation.class, new JqlUnaryOperationTransformer<>(this));
        transformers.put(TernaryOperation.class, new JqlTernaryOperationTransformer<>(this));
        transformers.put(DateLiteral.class, new JqlDateLiteralTransformer());
        transformers.put(TimeStampLiteral.class, new JqlTimestampLiteralTransformer());
        transformers.put(EnumLiteral.class, new JqlEnumLiteralTransformer<>(this));

        functionTransformers.put("length", (expression, functionCall, variables) -> newLengthBuilder().withExpression((StringExpression) expression).build());
        functionTransformers.put("lowercase", (expression, functionCall, variables) -> newLowerCaseBuilder().withExpression((StringExpression) expression).build());
        functionTransformers.put("uppercase", (expression, functionCall, variables) -> newUpperCaseBuilder().withExpression((StringExpression) expression).build());
        functionTransformers.put("trim", (expression, functionCall, variables) -> newTrimBuilder().withExpression((StringExpression) expression).build());
        functionTransformers.put("substring", new JqlSubstringFunctionTransformer(this));
        functionTransformers.put("position", new JqlPositionFunctionTransformer(this));
        functionTransformers.put("replace", new JqlReplaceFunctionTransformer(this));
        functionTransformers.put("first", new JqlIntegerParamStringFunctionTransformer(this,
                (stringExpression, parameter) -> newSubStringBuilder()
                        .withExpression(stringExpression)
                        .withPosition(newIntegerConstantBuilder().withValue(BigInteger.ZERO).build())
                        .withLength(parameter).build()));
        functionTransformers.put("last", new JqlIntegerParamStringFunctionTransformer(this,
                (stringExpression, parameter) -> {
                    IntegerExpression stringLength = newLengthBuilder().withExpression(copy(stringExpression)).build();
                    IntegerExpression position = newIntegerAritmeticExpressionBuilder().withLeft(stringLength).withRight(copy(parameter)).withOperator(IntegerOperator.SUBSTRACT).build();
                    return newSubStringBuilder()
                            .withExpression(stringExpression)
                            .withPosition(position)
                            .withLength(parameter).build();
                }));
        functionTransformers.put("matches", new JqlMatchesFunctionTransformer(this));

        functionTransformers.put("round", (expression, functionCall, variables) -> newRoundExpressionBuilder().withExpression((DecimalExpression) expression).build());

        functionTransformers.put("difference", new JqlDifferenceFunctionTransformer(this));

        functionTransformers.put("count", (expression, functionCall, variables) -> newCountExpressionBuilder().withCollectionExpression((CollectionExpression) expression).build());
        functionTransformers.put("head", new JqlIntegerParamCollectionFunctionTransformer(this,
                (collectionExpression, parameter) -> newSubCollectionExpressionBuilder()
                        .withCollectionExpression((OrderedCollectionExpression) collectionExpression)
                        .withLength(parameter)
                        .withPosition(newIntegerConstantBuilder().withValue(BigInteger.ZERO).build()).build()));
        functionTransformers.put("tail", new JqlIntegerParamCollectionFunctionTransformer(this,
                (collectionExpression, parameter) -> {
                    IntegerExpression length = newCountExpressionBuilder().withCollectionExpression(copy(collectionExpression)).build();
                    IntegerExpression position = newIntegerAritmeticExpressionBuilder().withLeft(length).withRight(copy(parameter)).withOperator(IntegerOperator.SUBSTRACT).build();

                    return newSubCollectionExpressionBuilder()
                            .withCollectionExpression((OrderedCollectionExpression) collectionExpression)
                            .withLength(parameter)
                            .withPosition(position).build();
                }));
        functionTransformers.put("filter", (expression, functionCall, variables) -> newCollectionFilterExpressionBuilder().withCollectionExpression((CollectionExpression) expression).withCondition((LogicalExpression) transform(functionCall.getParameters().get(0).getExpression(), variables)).build());
        functionTransformers.put("join", new JqlJoinFunctionTransformer(this));
        functionTransformers.put("sort", new JqlSortFunctionTransformer(this));
        functionTransformers.put("min", new JqlAggregatedExpressionTransformer(this, IntegerAggregator.MIN, DecimalAggregator.MIN));
        functionTransformers.put("max", new JqlAggregatedExpressionTransformer(this, IntegerAggregator.MAX, DecimalAggregator.MAX));
        functionTransformers.put("sum", new JqlAggregatedExpressionTransformer(this, IntegerAggregator.SUM, DecimalAggregator.SUM));
        functionTransformers.put("avg", new JqlAggregatedExpressionTransformer(this, null, DecimalAggregator.AVG));
    }

    public Expression transform(JqlExpression jqlExpression, List<ObjectVariable> variables) {
        Optional<JqlExpressionTransformerFunction> foundTransformer = transformers.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(jqlExpression.getClass()))
                .findAny()
                .map(Map.Entry::getValue);
        Expression transformedExpression = foundTransformer
                .orElseThrow(() -> new UnsupportedOperationException("Not implemented transformation for " + jqlExpression.getClass()))
                .apply(jqlExpression, variables);
        return applyFunctions(jqlExpression, transformedExpression, variables);
    }

    public Expression applyFunctions(JqlExpression jqlExpression, Expression argument, List<ObjectVariable> variables) {
        EList<FunctionCall> functionCalls = jqlExpression.getFunctions();
        Expression result = argument;
        for (FunctionCall functionCall : functionCalls) {
            String functionName = functionCall.getFunction().getName().toLowerCase();
            JqlFunctionTransformer functionTransformer = functionTransformers.get(functionName);
            List<ObjectVariable> variablesWithLambdaArgument = new ArrayList<>(variables);
            String lambdaArgument = functionCall.getLambdaArgument();
            if (lambdaArgument != null) {
                CollectionVariable collection = (CollectionVariable) result;
                ObjectVariable iterator = collection.createIterator(lambdaArgument, getModelAdapter(), expressionResource);
                variablesWithLambdaArgument.add(0, iterator);
            }
            if (functionTransformer != null) {
                result = functionTransformer.apply(result, functionCall, variablesWithLambdaArgument);
            } else {
                throw new IllegalStateException("Unknown function: " + functionName);
            }
        }
        return result;
    }

    public ModelAdapter<NE, P, PTE, E, C, RTE, M, U> getModelAdapter() {
        return modelAdapter;
    }

    public Map<String, MeasureName> getMeasureNames() {
        return measureNames;
    }

    public Map<String, TypeName> getEnumTypes() {
        return enumTypes;
    }

}
