package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.constant.JqlDateLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.constant.JqlTimestampLiteralTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlParameterizedFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.collection.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.object.JqlIsDefinedFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlReplaceFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlSubstringFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.temporal.JqlDifferenceFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.variable.GetVariableFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlBinaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlTernaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlUnaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.collection.CastCollection;
import hu.blackbelt.judo.meta.expression.collection.CollectionFilterExpression;
import hu.blackbelt.judo.meta.expression.collection.CollectionVariableReference;
import hu.blackbelt.judo.meta.expression.collection.SortExpression;
import hu.blackbelt.judo.meta.expression.logical.*;
import hu.blackbelt.judo.meta.expression.numeric.Position;
import hu.blackbelt.judo.meta.expression.object.*;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.operator.SequenceOperator;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.*;
import org.eclipse.emf.common.util.EList;

import java.lang.Iterable;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCastCollectionBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionFilterExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.*;
import static hu.blackbelt.judo.meta.expression.string.util.builder.StringBuilders.*;
import static org.eclipse.emf.ecore.util.EcoreUtil.copy;

public class JqlTransformers<NE, P extends NE, E extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U>
        implements ExpressionTransformer, ExpressionMeasureProvider {

    private final JqlExpressionBuilder<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> expressionBuilder;
    private final Map<Class<? extends JqlExpression>, JqlExpressionTransformerFunction> transformers = new LinkedHashMap<>();
    private final Map<String, JqlFunctionTransformer> functionTransformers = new LinkedHashMap<>();
    private boolean resolveDerived = true;

    public JqlTransformers(JqlExpressionBuilder<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> expressionBuilder) {
        this.expressionBuilder = expressionBuilder;
        literals();
        operations();
        stringFunctions();
        numericFunctions();
        temporalFunctions();
        environmentVariableFunctions();
        collectionFunctions();
        objectFunctions();
        sequenceFunctions();
    }

    private void objectFunctions() {
        functionTransformers.put("isundefined", new JqlIsDefinedFunctionTransformer(this, false));
        functionTransformers.put("isdefined", new JqlIsDefinedFunctionTransformer(this, true));

        functionTransformers.put("kindof",
                new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, InstanceOfExpression>(this,
                        (expression, parameter) -> {
                            TypeName typeNameFromResource = expressionBuilder.getTypeNameFromResource(parameter);
                            if (typeNameFromResource == null) {
                                throw new IllegalArgumentException("Type not found: " + parameter);
                            }
                            return newInstanceOfExpressionBuilder().withObjectExpression(expression)
                                    .withElementName(typeNameFromResource).build();
                        }, (jqlExpression) -> ((NavigationExpression) jqlExpression).getQName()));
        functionTransformers.put("typeof",
                new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, TypeOfExpression>(this,
                        (expression, parameter) -> newTypeOfExpressionBuilder().withObjectExpression(expression)
                                .withElementName(expressionBuilder.getTypeNameFromResource(parameter)).build(),
                        (jqlExpression -> ((NavigationExpression) jqlExpression).getQName())));

        functionTransformers.put("astype",
                new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, CastObject>(this,
                        (expression, parameter) -> newCastObjectBuilder()
                                .withElementName(expressionBuilder.getTypeNameFromResource(parameter))
                                .withObjectExpression(expression).build(),
                        jqlExpression -> ((NavigationExpression) jqlExpression).getQName()));

        functionTransformers.put("container",
                new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, ContainerExpression>(this,
                        (expression, parameter) -> newContainerExpressionBuilder()
                                .withElementName(expressionBuilder.getTypeNameFromResource(parameter))
                                .withObjectExpression(expression).build(),
                        jqlExpression -> ((NavigationExpression) jqlExpression).getQName()));

    }

    private void collectionFunctions() {
        functionTransformers.put("count", (expression, functionCall, variables) -> newCountExpressionBuilder()
                .withCollectionExpression((CollectionExpression) expression).build());
        functionTransformers.put("head", new JqlObjectSelectorToFilterTransformer(this,
                JqlObjectSelectorToFilterTransformer.ObjectSelector.HEAD));
        functionTransformers.put("heads", new JqlObjectSelectorToFilterTransformer(this,
                JqlObjectSelectorToFilterTransformer.ObjectSelector.HEADS));
        functionTransformers.put("tail", new JqlObjectSelectorToFilterTransformer(this,
                JqlObjectSelectorToFilterTransformer.ObjectSelector.TAIL));
        functionTransformers.put("tails", new JqlObjectSelectorToFilterTransformer(this,
                JqlObjectSelectorToFilterTransformer.ObjectSelector.TAILS));
        functionTransformers.put("any", new JqlAnyFunctionTransformer(this));
        functionTransformers.put("filter",
                new JqlParameterizedFunctionTransformer<ReferenceExpression, LogicalExpression, FilteringExpression>(
                        this, (expression, parameter) -> {
                    if (expression instanceof ObjectExpression) {
                        return newObjectFilterExpressionBuilder()
                                .withObjectExpression((ObjectExpression) expression).withCondition(parameter)
                                .build();
                    } else {
                        return newCollectionFilterExpressionBuilder()
                                .withCollectionExpression((CollectionExpression) expression)
                                .withCondition(parameter).build();
                    }
                }));
        functionTransformers.put("exists",
                new JqlParameterizedFunctionTransformer<CollectionExpression, LogicalExpression, FilteringExpression>(
                        this, (expression, parameter) -> newExistsBuilder().withCollectionExpression(expression)
                        .withCondition(parameter).build()));
        functionTransformers.put("forall",
                new JqlParameterizedFunctionTransformer<CollectionExpression, LogicalExpression, FilteringExpression>(
                        this, (expression, parameter) -> newForAllBuilder().withCollectionExpression(expression)
                        .withCondition(parameter).build()));
        functionTransformers.put("empty", (expression, functionCall, variables) -> newEmptyBuilder()
                .withCollectionExpression((CollectionExpression) expression).build());
        functionTransformers.put("join", new JqlJoinFunctionTransformer(this));
        functionTransformers.put("sort", new JqlSortFunctionTransformer(this));
        functionTransformers.put("min", JqlAggregatedExpressionTransformer.createMinInstance(this));
        functionTransformers.put("max", JqlAggregatedExpressionTransformer.createMaxInstance(this));
        functionTransformers.put("sum", JqlAggregatedExpressionTransformer.createSumInstance(this));
        functionTransformers.put("avg", JqlAggregatedExpressionTransformer.createAvgInstance(this));
        functionTransformers.put("contains",
                new JqlParameterizedFunctionTransformer<CollectionExpression, ObjectExpression, ContainsExpression>(
                        this, (expression, parameter) -> newContainsExpressionBuilder()
                        .withCollectionExpression(expression).withObjectExpression(parameter).build()));
        functionTransformers.put("memberof",
                new JqlParameterizedFunctionTransformer<ObjectExpression, CollectionExpression, MemberOfExpression>(
                        this, (expression, parameter) -> newMemberOfExpressionBuilder()
                        .withCollectionExpression(parameter).withObjectExpression(expression).build()));

        functionTransformers.put("ascollection",
                new JqlParameterizedFunctionTransformer<CollectionExpression, QualifiedName, CastCollection>(this,
                        (expression, parameter) -> newCastCollectionBuilder()
                                .withElementName(expressionBuilder.getTypeNameFromResource(parameter))
                                .withCollectionExpression(expression).build(),
                        jqlExpression -> ((NavigationExpression) jqlExpression).getQName()));

    }

    private void numericFunctions() {
        functionTransformers.put("round", (expression, functionCall, variables) -> newRoundExpressionBuilder()
                .withExpression((DecimalExpression) expression).build());
    }

    private void environmentVariableFunctions() {
        functionTransformers.put("getvariable", new GetVariableFunctionTransformer(this));
    }

    private void temporalFunctions() {
        functionTransformers.put("elapsedtimefrom", new JqlDifferenceFunctionTransformer(this, this));
    }

    private void stringFunctions() {
        functionTransformers.put("length", (expression, functionCall, variables) -> newLengthBuilder()
                .withExpression((StringExpression) expression).build());
        functionTransformers.put("lowercase", (expression, functionCall, variables) -> newLowerCaseBuilder()
                .withExpression((StringExpression) expression).build());
        functionTransformers.put("uppercase", (expression, functionCall, variables) -> newUpperCaseBuilder()
                .withExpression((StringExpression) expression).build());
        functionTransformers.put("trim", (expression, functionCall, variables) -> newTrimBuilder()
                .withExpression((StringExpression) expression).build());
        functionTransformers.put("substring", new JqlSubstringFunctionTransformer(this));
        functionTransformers.put("position",
                new JqlParameterizedFunctionTransformer<StringExpression, StringExpression, Position>(this, (argument,
                                                                                                             parameter) -> newPositionBuilder().withContainer(argument).withContainment(parameter).build()));
        functionTransformers.put("replace", new JqlReplaceFunctionTransformer(this));
        functionTransformers.put("first",
                new JqlParameterizedFunctionTransformer<StringExpression, IntegerExpression, Expression>(this,
                        (stringExpression, parameter) -> newSubStringBuilder().withExpression(stringExpression)
                                .withPosition(newIntegerConstantBuilder().withValue(BigInteger.ONE).build())
                                .withLength(parameter).build()));
        functionTransformers.put("last",
                new JqlParameterizedFunctionTransformer<StringExpression, IntegerExpression, Expression>(this,
                        (stringExpression, parameter) -> {
                            IntegerExpression stringLength = newLengthBuilder().withExpression(copy(stringExpression))
                                    .build();
                            IntegerExpression position = newIntegerArithmeticExpressionBuilder().withLeft(stringLength)
                                    .withRight(copy(parameter)).withOperator(IntegerOperator.SUBSTRACT).build();
                            position = newIntegerArithmeticExpressionBuilder().withLeft(position)
                                    .withOperator(IntegerOperator.ADD)
                                    .withRight(newIntegerConstantBuilder().withValue(BigInteger.ONE).build()).build();
                            return newSubStringBuilder().withExpression(stringExpression).withPosition(position)
                                    .withLength(parameter).build();
                        }));
        functionTransformers.put("matches",
                new JqlParameterizedFunctionTransformer<StringExpression, StringExpression, Matches>(this, (argument,
                                                                                                            parameter) -> newMatchesBuilder().withExpression(argument).withPattern(parameter).build()));
    }

    private void sequenceFunctions() {
        functionTransformers.put("next", (expression, functionCall, variables) -> newSequenceExpressionBuilder()
                .withSequence((Sequence) expression).withOperator(SequenceOperator.NEXT).build());
        functionTransformers.put("current", (expression, functionCall, variables) -> newSequenceExpressionBuilder()
                .withSequence((Sequence) expression).withOperator(SequenceOperator.CURRENT).build());
    }

    private void operations() {
        transformers.put(BinaryOperation.class, new JqlBinaryOperationTransformer<>(this));
        transformers.put(UnaryOperation.class, new JqlUnaryOperationTransformer<>(this));
        transformers.put(TernaryOperation.class, new JqlTernaryOperationTransformer<>(this));
    }

    private void literals() {
        transformers.put(NavigationExpression.class, new JqlNavigationTransformer<>(this));
        transformers.put(FunctionedExpression.class, new JqlFunctionedExpressionTransformer<>(this));
        transformers.put(BooleanLiteral.class, (jqlExpression, context) -> newBooleanConstantBuilder()
                .withValue(((BooleanLiteral) jqlExpression).isIsTrue()).build());
        transformers.put(DecimalLiteral.class, (jqlExpression, context) -> newDecimalConstantBuilder()
                .withValue(((DecimalLiteral) jqlExpression).getValue()).build());
        transformers.put(IntegerLiteral.class, (jqlExpression, context) -> newIntegerConstantBuilder()
                .withValue(((IntegerLiteral) jqlExpression).getValue()).build());
        transformers.put(StringLiteral.class, (jqlExpression, context) -> newStringConstantBuilder()
                .withValue(((StringLiteral) jqlExpression).getValue()).build());
        transformers.put(MeasuredLiteral.class, new JqlMeasuredLiteralTransformer<>(this));
        transformers.put(DateLiteral.class, new JqlDateLiteralTransformer());
        transformers.put(TimeStampLiteral.class, new JqlTimestampLiteralTransformer());
    }

    @Override
    public Expression transform(JqlExpression jqlExpression, ExpressionBuildingVariableResolver context) {
        Expression transformedExpression = findTransformer(jqlExpression.getClass())
                .orElseThrow(() -> new UnsupportedOperationException(
                        "Not implemented transformation for " + jqlExpression.getClass()))
                .apply(jqlExpression, context);
        return transformedExpression;
    }

    private Optional<JqlExpressionTransformerFunction> findTransformer(
            Class<? extends JqlExpression> jqlExpressionClass) {
        Optional<JqlExpressionTransformerFunction> foundTransformer = transformers.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(jqlExpressionClass)).findAny()
                .map(Map.Entry::getValue);
        return foundTransformer;
    }

    public Expression applyFunctions(JqlExpression jqlExpression, Expression baseExpression, C objectType,
                                     ExpressionBuildingVariableResolver context) {
        Expression subject = baseExpression;
        if (jqlExpression instanceof FunctionedExpression) {
            FunctionedExpression functionedExpression = (FunctionedExpression) jqlExpression;
            FunctionCall functionCall = functionedExpression.getFunctionCall();
            int i = 0;
            try {
                while (functionCall != null) {
                	if (functionCall.getFunction().getName() == null) {
                		throw new JqlExpressionBuildException(subject, Arrays.asList(
                                new JqlExpressionBuildingError("Function name required", functionCall)));
                	}
                    String functionName = functionCall.getFunction().getName().toLowerCase();
                    JqlFunctionTransformer functionTransformer = functionTransformers.get(functionName);
                    if (functionTransformer != null) {
                        try {
                            if (functionCall.getFunction().getLambdaArgument() != null) {
                                addLambdaVariable(subject, context, functionCall.getFunction().getLambdaArgument());
                                i++;
                            }
                            subject = functionTransformer.apply(subject, functionCall.getFunction(), context);
                        } catch (Exception e) {
                            throw new JqlExpressionBuildException(baseExpression,
                                    Arrays.asList(new JqlExpressionBuildingError(e.getMessage(), functionCall)), e);
                        }
                        if (subject instanceof CastCollection) {
                            CastCollection castCollection = (CastCollection) subject;
                            objectType = (C) castCollection.getElementName().get(getModelAdapter());
                        } else if (subject instanceof CastObject) {
                            CastObject castObject = (CastObject) subject;
                            objectType = (C) castObject.getElementName().get(getModelAdapter());
                        } else if (subject instanceof ContainerExpression) {
                            ContainerExpression containerObject = (ContainerExpression) subject;
                            objectType = (C) containerObject.getElementName().get(getModelAdapter());
                        }
                        // might be overridden via overrideTransformer, so we need to ask for it
                        JqlNavigationTransformer jqlNavigationTransformer = (JqlNavigationTransformer) findTransformer(
                                NavigationExpression.class).get();
                        JqlNavigationFeatureTransformer<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> jqlNavigationFeatureTransformer = jqlNavigationTransformer
                                .getFeatureTransformer();
                        JqlNavigationFeatureTransformer.JqlFeatureTransformResult<C> transformResult = jqlNavigationFeatureTransformer
                                .transform(functionCall.getFeatures(), subject, objectType, context);
                        subject = transformResult.baseExpression;
                        objectType = transformResult.navigationBase;
                    } else {
                        throw new JqlExpressionBuildException(subject, Arrays.asList(
                                new JqlExpressionBuildingError("Unknown function: " + functionName, functionCall)));
                    }
                    functionCall = (FunctionCall) functionCall.getCall();
                }
            } finally {
                while (i > 0) {
                    context.popVariable();
                    i--;
                }
            }
        }
        return subject;
    }

    private void addLambdaVariable(Expression subject, ExpressionBuildingVariableResolver context,
                                   String lambdaArgument) {
        if (subject instanceof IterableExpression) {
            IterableExpression iterableExpression = (IterableExpression) subject;
            if (iterableExpression.getIteratorVariable() == null) {
                ObjectVariable variable = iterableExpression.createIterator(lambdaArgument,
                        expressionBuilder.getModelAdapter(), expressionBuilder.getExpressionResource());
                context.pushVariable(variable);
            } else {
                iterableExpression.getIteratorVariable().setName(lambdaArgument);
                context.pushVariable(iterableExpression.getIteratorVariable());
            }
        } else if (subject instanceof ObjectSelectorExpression) {
            addLambdaVariable(((ObjectSelectorExpression) subject).getCollectionExpression(), context, lambdaArgument);
        } else if (subject instanceof ObjectFilterExpression) {
            addLambdaVariable(((ObjectFilterExpression) subject).getObjectExpression(), context, lambdaArgument);
        } else if (subject instanceof CollectionFilterExpression) {
            addLambdaVariable(((CollectionFilterExpression) subject).getCollectionExpression(), context,
                    lambdaArgument);
        } else if (subject instanceof SortExpression) {
            addLambdaVariable(((SortExpression) subject).getCollectionExpression(), context, lambdaArgument);
        } else {
            throw new IllegalArgumentException(
                    String.format("Lambda variable cannot be created with type: %s", subject.getClass()));
        }
    }

    public ModelAdapter<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> getModelAdapter() {
        return expressionBuilder.getModelAdapter();
    }

    public TypeName getEnumType(String enumType) {
        return expressionBuilder.getEnumTypeName(enumType);
    }

    public TypeName getTypeNameFromResource(QualifiedName qualifiedName) {
        return expressionBuilder.getTypeNameFromResource(qualifiedName);
    }

    public TypeName buildTypeName(String namespace, String name) {
        return expressionBuilder.buildTypeName(namespace, name);
    }

    public TypeName buildTypeName(QualifiedName qualifiedName) {
        return expressionBuilder.buildTypeName(qualifiedName);
    }

    @Override
    public MeasureName getMeasureName(String qualifiedName) {
        return expressionBuilder.getMeasureName(qualifiedName);
    }

    @Override
    public MeasureName getDurationMeasureName(QualifiedName qualifiedName) {
        return expressionBuilder.getDurationMeasureName(
                JqlExpressionBuilder.getFqString(qualifiedName.getNamespaceElements(), qualifiedName.getName()));
    }

    @Override
    public Collection<MeasureName> getDurationMeasures() {
        return expressionBuilder.getDurationMeasures();
    }

    @Override
    public Optional<MeasureName> getDefaultDurationMeasure() {
        Collection<MeasureName> durationMeasures = expressionBuilder.getDurationMeasures();
        if (durationMeasures.size() == 1) {
            return Optional.of(durationMeasures.iterator().next());
        } else {
            return Optional.empty();
        }
    }

    public JqlExpressionBuilder<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> getExpressionBuilder() {
        return expressionBuilder;
    }

    public void overrideTransformer(Class<? extends JqlExpression> jqlType,
                                    Function<JqlTransformers<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U>, ? extends JqlExpressionTransformerFunction> transformer) {
        transformers.put(jqlType, transformer.apply(this));
    }

    public void addFunctionTransformer(String functionName,
                                       Function<JqlTransformers, JqlFunctionTransformer<? extends Expression>> transformer) {
        functionTransformers.put(functionName, transformer.apply(this));
    }

    public void setResolveDerived(boolean resolveDerived) {
        this.resolveDerived = resolveDerived;
    }

    public boolean isResolveDerived() {
        return resolveDerived;
    }

}
