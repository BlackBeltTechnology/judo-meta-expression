package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.binding.Binding;
import hu.blackbelt.judo.meta.expression.builder.jql.constant.JqlDateLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.constant.JqlTimestampLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlEnumLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlExpressionTransformerFunction;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlMeasuredLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.JqlNavigationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlParameterizedFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.collection.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.object.JqlIsDefinedFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlReplaceFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlSubstringFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.temporal.JqlDifferenceFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlBinaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlTernaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlUnaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.collection.CastCollection;
import hu.blackbelt.judo.meta.expression.collection.CollectionFilterExpression;
import hu.blackbelt.judo.meta.expression.collection.SortExpression;
import hu.blackbelt.judo.meta.expression.logical.*;
import hu.blackbelt.judo.meta.expression.numeric.Position;
import hu.blackbelt.judo.meta.expression.object.CastObject;
import hu.blackbelt.judo.meta.expression.object.ContainerExpression;
import hu.blackbelt.judo.meta.expression.object.ObjectFilterExpression;
import hu.blackbelt.judo.meta.expression.operator.DecimalAggregator;
import hu.blackbelt.judo.meta.expression.operator.IntegerAggregator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.operator.SequenceOperator;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.*;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCastCollectionBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionFilterExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;
import static org.eclipse.emf.ecore.util.EcoreUtil.copy;

public class JqlTransformers<NE, P, PTE, E extends NE, C extends NE, RTE, S, M, U> {

    private final JqlExpressionBuilder<NE, P, PTE, E, C, RTE, S, M, U> expressionBuilder;
    private final Map<Class<? extends JqlExpression>, JqlExpressionTransformerFunction> transformers = new LinkedHashMap<>();
    private final Map<String, JqlFunctionTransformer> functionTransformers = new LinkedHashMap<>();

    public JqlTransformers(JqlExpressionBuilder<NE, P, PTE, E, C, RTE, S, M, U> expressionBuilder) {
        this.expressionBuilder = expressionBuilder;
        literals();
        operations();
        stringFunctions();
        numericFunctions();
        temporalFunctions();
        collectionFunctions();
        objectFunctions();
        sequenceFunctions();
    }

    private void objectFunctions() {
        functionTransformers.put("isundefined", new JqlIsDefinedFunctionTransformer(this, false));
        functionTransformers.put("isdefined", new JqlIsDefinedFunctionTransformer(this, true));

        functionTransformers.put("kindof", new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, InstanceOfExpression>(this,
                (expression, parameter) -> {
                    TypeName typeNameFromResource = expressionBuilder.getTypeNameFromResource(parameter);
                    if (typeNameFromResource == null) {
                        throw new IllegalArgumentException("Type not found: " + parameter);
                    }
                    return newInstanceOfExpressionBuilder().withObjectExpression(expression).withElementName(typeNameFromResource).build();
                },
                (jqlExpression) -> ((NavigationExpression) jqlExpression).getBase()
        ));
        functionTransformers.put("typeof", new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, TypeOfExpression>(this,
                (expression, parameter) -> newTypeOfExpressionBuilder().withObjectExpression(expression).withElementName(expressionBuilder.getTypeNameFromResource(parameter)).build(),
                (jqlExpression -> ((NavigationExpression) jqlExpression).getBase())
        ));

        functionTransformers.put("astype", new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, CastObject>(this,
                (expression, parameter) -> newCastObjectBuilder().withElementName(expressionBuilder.getTypeNameFromResource(parameter)).withObjectExpression(expression).build(),
                jqlExpression -> ((NavigationExpression) jqlExpression).getBase()));

        functionTransformers.put("container", new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, ContainerExpression>(this,
                (expression, parameter) -> newContainerExpressionBuilder().withElementName(expressionBuilder.getTypeNameFromResource(parameter)).withObjectExpression(expression).build(),
                jqlExpression -> ((NavigationExpression) jqlExpression).getBase()));

    }

    private void collectionFunctions() {
        functionTransformers.put("count", (expression, functionCall, variables) -> newCountExpressionBuilder().withCollectionExpression((CollectionExpression) expression).build());
        functionTransformers.put("head", new JqlHeadFunctionTransformer(this));
        functionTransformers.put("tail", new JqlTailFunctionTransformer(this));
        functionTransformers.put("filter", new JqlParameterizedFunctionTransformer<ReferenceExpression, LogicalExpression, FilteringExpression>(this,
                (expression, parameter) -> {
                    if (expression instanceof ObjectExpression) {
                        return newObjectFilterExpressionBuilder().withObjectExpression((ObjectExpression) expression).withCondition(parameter).build();
                    } else {
                        return newCollectionFilterExpressionBuilder().withCollectionExpression((CollectionExpression) expression).withCondition(parameter).build();
                    }
                }));
        functionTransformers.put("exists", new JqlParameterizedFunctionTransformer<CollectionExpression, LogicalExpression, FilteringExpression>(this,
                (expression, parameter) -> newExistsBuilder().withCollectionExpression(expression).withCondition(parameter).build()));
        functionTransformers.put("forall", new JqlParameterizedFunctionTransformer<CollectionExpression, LogicalExpression, FilteringExpression>(this,
                (expression, parameter) -> newForAllBuilder().withCollectionExpression(expression).withCondition(parameter).build()));
        functionTransformers.put("empty", (expression, functionCall, variables) -> newEmptyBuilder().withCollectionExpression((CollectionExpression) expression).build());
        functionTransformers.put("join", new JqlJoinFunctionTransformer(this));
        functionTransformers.put("sort", new JqlSortFunctionTransformer(this));
        functionTransformers.put("min", new JqlAggregatedExpressionTransformer(this, IntegerAggregator.MIN, DecimalAggregator.MIN));
        functionTransformers.put("max", new JqlAggregatedExpressionTransformer(this, IntegerAggregator.MAX, DecimalAggregator.MAX));
        functionTransformers.put("sum", new JqlAggregatedExpressionTransformer(this, IntegerAggregator.SUM, DecimalAggregator.SUM));
        functionTransformers.put("avg", new JqlAggregatedExpressionTransformer(this, null, DecimalAggregator.AVG));
        functionTransformers.put("contains", new JqlParameterizedFunctionTransformer<CollectionExpression, ObjectExpression, ContainsExpression>(this,
                (expression, parameter) -> newContainsExpressionBuilder().withCollectionExpression(expression).withObjectExpression(parameter).build()));
        functionTransformers.put("memberof", new JqlParameterizedFunctionTransformer<ObjectExpression, CollectionExpression, MemberOfExpression>(this,
                (expression, parameter) -> newMemberOfExpressionBuilder().withCollectionExpression(parameter).withObjectExpression(expression).build()));

        functionTransformers.put("ascollection", new JqlParameterizedFunctionTransformer<CollectionExpression, QualifiedName, CastCollection>(this,
                (expression, parameter) -> newCastCollectionBuilder().withElementName(expressionBuilder.getTypeNameFromResource(parameter)).withCollectionExpression(expression).build(),
                jqlExpression -> ((NavigationExpression) jqlExpression).getBase()));

    }

    private void numericFunctions() {
        functionTransformers.put("round", (expression, functionCall, variables) -> newRoundExpressionBuilder().withExpression((DecimalExpression) expression).build());
    }

    private void temporalFunctions() {
        functionTransformers.put("difference", new JqlDifferenceFunctionTransformer(this));
    }

    private void stringFunctions() {
        functionTransformers.put("length", (expression, functionCall, variables) -> newLengthBuilder().withExpression((StringExpression) expression).build());
        functionTransformers.put("lowercase", (expression, functionCall, variables) -> newLowerCaseBuilder().withExpression((StringExpression) expression).build());
        functionTransformers.put("uppercase", (expression, functionCall, variables) -> newUpperCaseBuilder().withExpression((StringExpression) expression).build());
        functionTransformers.put("trim", (expression, functionCall, variables) -> newTrimBuilder().withExpression((StringExpression) expression).build());
        functionTransformers.put("substring", new JqlSubstringFunctionTransformer(this));
        functionTransformers.put("position", new JqlParameterizedFunctionTransformer<StringExpression, StringExpression, Position>(this, (argument, parameter) -> newPositionBuilder().withContainer(argument).withContainment(parameter).build()));
        functionTransformers.put("replace", new JqlReplaceFunctionTransformer(this));
        functionTransformers.put("first", new JqlParameterizedFunctionTransformer<StringExpression, IntegerExpression, Expression>(this,
                (stringExpression, parameter) -> newSubStringBuilder()
                        .withExpression(stringExpression)
                        .withPosition(newIntegerConstantBuilder().withValue(BigInteger.ZERO).build())
                        .withLength(parameter).build()));
        functionTransformers.put("last", new JqlParameterizedFunctionTransformer<StringExpression, IntegerExpression, Expression>(this,
                (stringExpression, parameter) -> {
                    IntegerExpression stringLength = newLengthBuilder().withExpression(copy(stringExpression)).build();
                    IntegerExpression position = newIntegerArithmeticExpressionBuilder().withLeft(stringLength).withRight(copy(parameter)).withOperator(IntegerOperator.SUBSTRACT).build();
                    return newSubStringBuilder()
                            .withExpression(stringExpression)
                            .withPosition(position)
                            .withLength(parameter).build();
                }));
        functionTransformers.put("matches", new JqlParameterizedFunctionTransformer<StringExpression, StringExpression, Matches>(this, (argument, parameter) -> newMatchesBuilder().withExpression(argument).withPattern(parameter).build()));
    }

    private void sequenceFunctions() {
        functionTransformers.put("next", (expression, functionCall, variables) ->
                newSequenceExpressionBuilder().withSequence((Sequence) expression).withOperator(SequenceOperator.NEXT).build());
        functionTransformers.put("current", (expression, functionCall, variables) ->
                newSequenceExpressionBuilder().withSequence((Sequence) expression).withOperator(SequenceOperator.CURRENT).build());
    }

    private void operations() {
        transformers.put(BinaryOperation.class, new JqlBinaryOperationTransformer<>(this));
        transformers.put(UnaryOperation.class, new JqlUnaryOperationTransformer<>(this));
        transformers.put(TernaryOperation.class, new JqlTernaryOperationTransformer<>(this));
    }

    private void literals() {
        transformers.put(NavigationExpression.class, new JqlNavigationTransformer<>(this));
        transformers.put(BooleanLiteral.class, (jqlExpression, context) -> newBooleanConstantBuilder().withValue(((BooleanLiteral) jqlExpression).isIsTrue()).build());
        transformers.put(DecimalLiteral.class, (jqlExpression, context) -> newDecimalConstantBuilder().withValue(((DecimalLiteral) jqlExpression).getValue()).build());
        transformers.put(IntegerLiteral.class, (jqlExpression, context) -> newIntegerConstantBuilder().withValue(((IntegerLiteral) jqlExpression).getValue()).build());
        transformers.put(StringLiteral.class, (jqlExpression, context) -> newStringConstantBuilder().withValue(((StringLiteral) jqlExpression).getValue()).build());
        transformers.put(MeasuredLiteral.class, new JqlMeasuredLiteralTransformer<>(this));
        transformers.put(DateLiteral.class, new JqlDateLiteralTransformer());
        transformers.put(TimeStampLiteral.class, new JqlTimestampLiteralTransformer());
        transformers.put(EnumLiteral.class, new JqlEnumLiteralTransformer<>(this));
    }

    public Expression transform(JqlExpression jqlExpression, JqlExpressionBuildingContext context) {
        Optional<JqlExpressionTransformerFunction> foundTransformer = transformers.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(jqlExpression.getClass()))
                .findAny()
                .map(Map.Entry::getValue);
        Expression transformedExpression = foundTransformer
                .orElseThrow(() -> new UnsupportedOperationException("Not implemented transformation for " + jqlExpression.getClass()))
                .apply(jqlExpression, context);
        if (jqlExpression instanceof NavigationExpression) {
            return transformedExpression;
        } else {
            return applyFunctions(jqlExpression.getFunctions(), transformedExpression, context);
        }
    }

    public Expression applyFunctions(List<FunctionCall> functionCalls, Expression baseExpression, JqlExpressionBuildingContext context) {
        Expression subject = baseExpression;
        for (FunctionCall functionCall : functionCalls) {
            String functionName = functionCall.getFunction().getName().toLowerCase();
            JqlFunctionTransformer functionTransformer = functionTransformers.get(functionName);
            if (functionCall.getLambdaArgument() != null) {
                addLambdaVariable(subject, context, functionCall.getLambdaArgument());
            }
            if (functionTransformer != null) {
                subject = functionTransformer.apply(subject, functionCall, context);
                if (functionCall.getLambdaArgument() != null) {
                    // TODO handle scoping of lambda variables
//                    context.popVariable();
                }
            } else {
                throw new IllegalStateException("Unknown function: " + functionName);
            }
        }
        return subject;
    }

    private void addLambdaVariable(Expression subject, JqlExpressionBuildingContext context, String lambdaArgument) {
        if (subject instanceof CollectionVariable) {
            CollectionVariable collection = (CollectionVariable) subject;
            if (collection.getIteratorVariable() == null) {
                ObjectVariable variable = collection.createIterator(lambdaArgument, expressionBuilder.getModelAdapter(), expressionBuilder.getExpressionResource());
                context.pushVariable(variable);
            } else {
                collection.getIteratorVariable().setName(lambdaArgument);
            }
        } else if (subject instanceof ObjectVariable) {
            ObjectVariable variable = (ObjectVariable) subject;
            variable.setName(lambdaArgument);
            context.pushVariable(variable);
        } else if (subject instanceof ObjectFilterExpression) {
            addLambdaVariable(((ObjectFilterExpression) subject).getObjectExpression(), context, lambdaArgument);
        } else if (subject instanceof CollectionFilterExpression) {
            addLambdaVariable(((CollectionFilterExpression) subject).getCollectionExpression(), context, lambdaArgument);
        } else if (subject instanceof SortExpression) {
            addLambdaVariable(((SortExpression) subject).getCollectionExpression(), context, lambdaArgument);
        } else {
            throw new IllegalArgumentException(String.format("Lambda variable cannot be created with type: %s", subject.getClass()));
        }
    }

    public ModelAdapter<NE, P, PTE, E, C, RTE, S, M, U> getModelAdapter() {
        return expressionBuilder.getModelAdapter();
    }

    public TypeName getEnumType(String enumType) {
        return expressionBuilder.getEnumTypeName(enumType);
    }

    public TypeName getTypeNameFromResource(QualifiedName qualifiedName) {
        return expressionBuilder.getTypeNameFromResource(qualifiedName);
    }

    public TypeName getTypeNameFromResource(String namespace, String name) {
        return expressionBuilder.getTypeName(namespace, name);
    }

    public MeasureName getMeasureName(String qualifiedName) {
        return expressionBuilder.getMeasureName(qualifiedName);
    }

    public Optional<? extends Binding> getBinding(C baseType, String feature) {
        return expressionBuilder.getBinding(baseType, feature);
    }

    public JqlExpressionBuilder<NE, P, PTE, E, C, RTE, S, M, U> getExpressionBuilder() {
        return expressionBuilder;
    }
}
