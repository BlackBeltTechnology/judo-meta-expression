package hu.blackbelt.judo.meta.expression.builder.jql;

/*-
 * #%L
 * JUDO :: Expression :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.judo.meta.expression.*;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.builder.jql.constant.*;
import hu.blackbelt.judo.meta.expression.builder.jql.expression.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlParameterizedFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.collection.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.collection.JqlBooleanAggregatorFunctionTransformer.BooleanAggregatorType;
import hu.blackbelt.judo.meta.expression.builder.jql.function.numeric.JqlRoundFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.object.JqlIsDefinedFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.temporal.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.variable.GetVariableFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.*;
import hu.blackbelt.judo.meta.expression.collection.CastCollection;
import hu.blackbelt.judo.meta.expression.logical.*;
import hu.blackbelt.judo.meta.expression.numeric.Position;
import hu.blackbelt.judo.meta.expression.object.CastObject;
import hu.blackbelt.judo.meta.expression.object.ContainerExpression;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.operator.SequenceOperator;
import hu.blackbelt.judo.meta.expression.string.PaddignType;
import hu.blackbelt.judo.meta.expression.string.TrimType;
import hu.blackbelt.judo.meta.expression.temporal.DatePart;
import hu.blackbelt.judo.meta.expression.temporal.TimestampPart;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.*;

import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static hu.blackbelt.judo.meta.expression.builder.jql.JqlExpressionBuilder.getTypeNameOf;
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

    private AtomicInteger atomicCounter = new AtomicInteger(1);
    private static String generatedIteratorPrefix = "_iterator_";

    public JqlTransformers(JqlExpressionBuilder<NE, P, E, C, PTE, RTE, TO, TA, TR, S, M, U> expressionBuilder) {
        this.expressionBuilder = expressionBuilder;
        literals();
        operations();
        primitiveFunctions();
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

        functionTransformers.put(
                "kindof",
                new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, InstanceOfExpression>(
                        this,
                        (expression, parameter) -> newInstanceOfExpressionBuilder()
                                .withObjectExpression(expression)
                                .withElementName(getKindOfParameterTypeName(expression, parameter))
                                .build(),
                        jqlExpression -> ((NavigationExpression) jqlExpression).getQName()));
        functionTransformers.put(
                "typeof",
                new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, TypeOfExpression>(
                        this,
                        (expression, parameter) -> newTypeOfExpressionBuilder()
                                .withObjectExpression(expression)
                                .withElementName(getTypeOfParameterTypeName(expression, parameter))
                                .build(),
                        jqlExpression -> ((NavigationExpression) jqlExpression).getQName()));
        functionTransformers.put(
                "astype",
                new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, CastObject>(
                        this,
                        (expression, parameter) -> newCastObjectBuilder()
                                .withObjectExpression(expression)
                                .withElementName(getAsTypeTypeName(expression, parameter))
                                .build(),
                        jqlExpression -> ((NavigationExpression) jqlExpression).getQName()));
        functionTransformers.put(
                "container",
                new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, ContainerExpression>(
                        this,
                        (expression, parameter) -> newContainerExpressionBuilder()
                                .withObjectExpression(expression)
                                .withElementName(getContainerTypeName(expression, parameter))
                                .build(),
                        jqlExpression -> ((NavigationExpression) jqlExpression).getQName()));
    }

    private TypeName getKindOfParameterTypeName(ObjectExpression expression, QualifiedName parameter) {
        return getParameterTypeName(expression, parameter, true);
    }

    private TypeName getTypeOfParameterTypeName(ObjectExpression expression, QualifiedName parameter) {
        return getParameterTypeName(expression, parameter, false);
    }

    private TypeName getParameterTypeName(ObjectExpression expression, QualifiedName parameter, boolean kindOf) {
        TypeName typeNameFromResource = getTypeNameOf(parameter, () -> expressionBuilder.getTypeNameFromResource(parameter));

        C objectType = (C) expression.getObjectType(getModelAdapter());
        C parameterType = (C) getModelAdapter().get(typeNameFromResource).get();
        if (!(getModelAdapter().getSuperTypes(parameterType).contains(objectType) || objectType.equals(parameterType))) {
            String objectName = getModelAdapter().getName(objectType).orElse(objectType.toString());
            String parameterName = getModelAdapter().getName(parameterType).orElse(parameterType.toString());
            throw new IllegalArgumentException("Element type " + parameterName + " is not " + (kindOf ? "compatible with " : "") + objectName);
        }

        return typeNameFromResource;
    }

    private TypeName getAsTypeTypeName(ObjectExpression expression, QualifiedName parameter) {
        TypeName typeNameFromResource = getTypeNameOf(parameter, () -> expressionBuilder.getTypeNameFromResource(parameter));

        C objectType = (C) expression.getObjectType(getModelAdapter());
        C parameterType = (C) getModelAdapter().get(typeNameFromResource).get();
        if (!getModelAdapter().getSuperTypes(parameterType).contains(objectType)) {
            String objectName = getModelAdapter().getName(objectType).orElse(objectType.toString());
            String parameterName = getModelAdapter().getName(parameterType).orElse(parameterType.toString());
            throw new IllegalArgumentException("Invalid casting type: " + objectName + " is not supertype of " + parameterName);
        }

        return typeNameFromResource;
    }

    private TypeName getContainerTypeName(ObjectExpression expression, QualifiedName parameter) {
        TypeName typeNameFromResource = getTypeNameOf(parameter, () -> expressionBuilder.getTypeNameFromResource(parameter));

        C objectType = (C) expression.getObjectType(getModelAdapter());
        C parameterType = (C) getModelAdapter().get(typeNameFromResource).get();
        if (!getModelAdapter().getContainerTypesOf(objectType).contains(parameterType)) {
            String objectName = getModelAdapter().getName(objectType).orElse(objectType.toString());
            String parameterName = getModelAdapter().getName(parameterType).orElse(parameterType.toString());
            throw new IllegalArgumentException(parameterName + " type is not a container of " + objectName);
        }

        return typeNameFromResource;
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
        functionTransformers.put("any", new JqlObjectSelectorToFilterTransformer(this,
                JqlObjectSelectorToFilterTransformer.ObjectSelector.ANY));
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
        functionTransformers.put("anytrue", new JqlBooleanAggregatorFunctionTransformer(this, BooleanAggregatorType.ANY_TRUE));
        functionTransformers.put("alltrue", new JqlBooleanAggregatorFunctionTransformer(this, BooleanAggregatorType.ALL_TRUE));
        functionTransformers.put("anyfalse", new JqlBooleanAggregatorFunctionTransformer(this, BooleanAggregatorType.ANY_FALSE));
        functionTransformers.put("allfalse", new JqlBooleanAggregatorFunctionTransformer(this, BooleanAggregatorType.ALL_FALSE));
        functionTransformers.put("empty", (expression, functionCall, variables) -> newEmptyBuilder()
                .withCollectionExpression((CollectionExpression) expression).build());
        functionTransformers.put("join", new JqlJoinFunctionTransformer(this));
        functionTransformers.put("sort", new JqlSortFunctionTransformer(this));
        functionTransformers.put("min", JqlAggregatedExpressionTransformer.createMinInstance(this));
        functionTransformers.put("max", JqlAggregatedExpressionTransformer.createMaxInstance(this));
        functionTransformers.put("sum", JqlAggregatedExpressionTransformer.createSumInstance(this));
        functionTransformers.put("avg", JqlAggregatedExpressionTransformer.createAvgInstance(this));
        functionTransformers.put(
                "contains",
                new JqlParameterizedFunctionTransformer<CollectionExpression, ObjectExpression, ContainsExpression>(
                        this,
                        (expression, parameter) -> {
                            checkContainsOrMemberOfTypeName(expression, parameter);
                            return newContainsExpressionBuilder()
                                    .withCollectionExpression(expression)
                                    .withObjectExpression(parameter)
                                    .build();
                        }));
        functionTransformers.put(
                "memberof",
                new JqlParameterizedFunctionTransformer<ObjectExpression, CollectionExpression, MemberOfExpression>(
                        this,
                        (expression, parameter) -> {
                            checkContainsOrMemberOfTypeName(expression, parameter);
                            return newMemberOfExpressionBuilder()
                                    .withCollectionExpression(parameter)
                                    .withObjectExpression(expression)
                                    .build();
                        }));
        functionTransformers.put(
                "ascollection",
                new JqlParameterizedFunctionTransformer<CollectionExpression, QualifiedName, CastCollection>(
                        this,
                        (expression, parameter) -> newCastCollectionBuilder()
                                .withElementName(getAsCollectionTypeName(expression, parameter))
                                .withCollectionExpression(expression)
                                .build(),
                        jqlExpression -> ((NavigationExpression) jqlExpression).getQName()));
    }

    private void checkContainsOrMemberOfTypeName(IterableExpression expression, IterableExpression parameter) {
        C objectType = (C) expression.getObjectType(getModelAdapter());
        C parameterType = (C) parameter.getObjectType(getModelAdapter());
        if (!(objectType.equals(parameterType) ||
              getModelAdapter().getSuperTypes(objectType).contains(parameterType) ||
              getModelAdapter().getSuperTypes(parameterType).contains(objectType))) {
            String objectName = getModelAdapter().getName(objectType).orElse(objectType.toString());
            String parameterName = getModelAdapter().getName(parameterType).orElse(parameterType.toString());
            throw new IllegalArgumentException("Types of collection '" + objectName + "' and object '" + parameterName + "' are not compatible");
        }
    }

    private TypeName getAsCollectionTypeName(CollectionExpression expression, QualifiedName parameter) {
        TypeName typeNameFromResource = getTypeNameOf(parameter, () -> expressionBuilder.getTypeNameFromResource(parameter));

        C objectType = (C) expression.getObjectType(getModelAdapter());
        C parameterType = (C) getModelAdapter().get(typeNameFromResource).get();
        if (!getModelAdapter().getSuperTypes(parameterType).contains(objectType)) {
            String objectName = getModelAdapter().getName(objectType).orElse(objectType.toString());
            String parameterName = getModelAdapter().getName(parameterType).orElse(parameterType.toString());
            throw new IllegalArgumentException("Invalid casting: " + objectName + " as " + parameterName + ". " + objectName + " is not supertype of " + parameterName);
        }

        return typeNameFromResource;
    }

    @Deprecated
    private void checkNumericExpression(Expression expression) {
        if (!(expression instanceof NumericExpression)) {
            throw new IllegalArgumentException("NumericExpression expected. Got: " + expression.getClass().getSimpleName());
        }
    }

    private void numericFunctions() {
        functionTransformers.put("round", new JqlRoundFunctionTransformer(this));
        functionTransformers.put("abs", (expression, functionCall, variables) -> {
            checkNumericExpression(expression);
            return newAbsoluteExpressionBuilder().withExpression((NumericExpression) expression).build();
        });
        functionTransformers.put("ceil", (expression, functionCall, variables) -> {
            checkNumericExpression(expression);
            return newCeilExpressionBuilder().withExpression((NumericExpression) expression).build();
        });
        functionTransformers.put("floor", (expression, functionCall, variables) -> {
            checkNumericExpression(expression);
            return newFloorExpressionBuilder().withExpression((NumericExpression) expression).build();
        });
    }

    private void environmentVariableFunctions() {
        functionTransformers.put("getvariable", new GetVariableFunctionTransformer(this, true));
        functionTransformers.put("now", new NowFunctionTransformer(this));
    }

    private void temporalFunctions() {
        // diff
        functionTransformers.put("elapsedtimefrom", new JqlDifferenceFunctionTransformer(this, this));

        // extract
        functionTransformers.put("year", new ExtractTransformer(this, ChronoUnit.YEARS));
        functionTransformers.put("month", new ExtractTransformer(this, ChronoUnit.MONTHS));
        functionTransformers.put("day", new ExtractTransformer(this, ChronoUnit.DAYS));
        functionTransformers.put("dayofweek", new ExtractFromDateTransformer(this, DatePart.DAY_OF_WEEK));
        functionTransformers.put("dayofyear", new ExtractFromDateTransformer(this, DatePart.DAY_OF_YEAR));
        functionTransformers.put("hour", new ExtractTransformer(this, ChronoUnit.HOURS));
        functionTransformers.put("minute", new ExtractTransformer(this, ChronoUnit.MINUTES));
        functionTransformers.put("second", new ExtractTransformer(this, ChronoUnit.SECONDS));
        functionTransformers.put("millisecond", new ExtractTransformer(this, ChronoUnit.MILLIS));
        functionTransformers.put("date", new ExtractFromTimestampTransformer(this, TimestampPart.DATE));
        functionTransformers.put("time", new ExtractFromTimestampTransformer(this, TimestampPart.TIME));

        // construct
        functionTransformers.put("of", new ConstructorTransformer(this));

        // convert
        functionTransformers.put("asmilliseconds", new TimestampAsMillisecondsTransformer(this));
        functionTransformers.put("frommilliseconds", new TimestampFromMillisecondsTransformer(this));
        functionTransformers.put("asseconds", new TimeAsSecondsTransformer(this));
        functionTransformers.put("fromseconds", new TimeFromSecondsTransformer(this));

        // arithmetics
        functionTransformers.put("plusyears", new TimestampArithmeticTransformer(this, TimestampPart.YEAR));
        functionTransformers.put("plusmonths", new TimestampArithmeticTransformer(this, TimestampPart.MONTH));
        functionTransformers.put("plusdays", new TimestampArithmeticTransformer(this, TimestampPart.DAY));
        functionTransformers.put("plushours", new TimestampArithmeticTransformer(this, TimestampPart.HOUR));
        functionTransformers.put("plusminutes", new TimestampArithmeticTransformer(this, TimestampPart.MINUTE));
        functionTransformers.put("plusseconds", new TimestampArithmeticTransformer(this, TimestampPart.SECOND));
        functionTransformers.put("plusmilliseconds", new TimestampArithmeticTransformer(this, TimestampPart.MILLISECOND));
    }

    private void primitiveFunctions() {
        functionTransformers.put("asstring", (expression, functionCall, variables) -> newAsStringBuilder()
                .withEnumType(expression instanceof EnumerationExpression ? ((EnumerationExpression) expression).getEnumeration(getModelAdapter()) : null)
                .withExpression((DataExpression) expression).build());
    }

    private void stringFunctions() {
        functionTransformers.put("length", (expression, functionCall, variables) -> newLengthBuilder()
                .withExpression((StringExpression) expression).build());
        functionTransformers.put("lowercase", (expression, functionCall, variables) -> newLowerCaseBuilder()
                .withExpression((StringExpression) expression).build());
        functionTransformers.put("lower", (expression, functionCall, variables) -> newLowerCaseBuilder()
                .withExpression((StringExpression) expression).build());
        functionTransformers.put("uppercase", (expression, functionCall, variables) -> newUpperCaseBuilder()
                .withExpression((StringExpression) expression).build());
        functionTransformers.put("upper", (expression, functionCall, variables) -> newUpperCaseBuilder()
                .withExpression((StringExpression) expression).build());
        functionTransformers.put("capitalize", new JqlCapitalizeFunctionTransformer(this));
        functionTransformers.put("trim", (expression, functionCall, variables) -> newTrimBuilder()
                .withExpression((StringExpression) expression).withTrimType(TrimType.BOTH).build());
        functionTransformers.put("ltrim", (expression, functionCall, variables) -> newTrimBuilder()
                .withExpression((StringExpression) expression).withTrimType(TrimType.LEFT).build());
        functionTransformers.put("rtrim", (expression, functionCall, variables) -> newTrimBuilder()
                .withExpression((StringExpression) expression).withTrimType(TrimType.RIGHT).build());
        functionTransformers.put("lpad", new JqlPaddingFunctionTransformer(this, PaddignType.LEFT));
        functionTransformers.put("rpad", new JqlPaddingFunctionTransformer(this, PaddignType.RIGHT));
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
        functionTransformers.put("like",
                new JqlParameterizedFunctionTransformer<StringExpression, StringExpression, Like>(this, (argument,
                                                                                                            parameter) -> newLikeBuilder().withExpression(argument).withPattern(parameter).withCaseInsensitive(false).build()));
        functionTransformers.put("ilike",
                new JqlParameterizedFunctionTransformer<StringExpression, StringExpression, Like>(this, (argument,
                                                                                                         parameter) -> newLikeBuilder().withExpression(argument).withPattern(parameter).withCaseInsensitive(true).build()));
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
        transformers.put(TimeLiteral.class, new JqlTimeLiteralTransformer());
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
            int lambdaDepth = 0;
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
                                context.pushVariableScope();
                                addLambdaVariable(subject, context, functionCall.getFunction().getLambdaArgument());
                                lambdaDepth++;
                            }
                            subject = functionTransformer.apply(subject, functionCall.getFunction(), context);
                            if (functionCall.getFunction().getLambdaArgument() != null) {
                                context.popVariable();
                                context.popVariableScope();
                            }
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
                while (lambdaDepth > 0) {
                    lambdaDepth--;
                }
            }
        }
        return subject;
    }

    private void addLambdaVariable(Expression subject, ExpressionBuildingVariableResolver context, String lambdaArgument) {
        if (!(subject instanceof IterableExpression)) {
            throw new IllegalArgumentException("Lambda variable cannot be created with type: " + subject.getClass().getName());
        }

        String generatedName = newGeneratedIteratorName();
        IterableExpression iterableExpression = (IterableExpression) subject;
        if (iterableExpression.getIteratorVariable() == null) {
            ObjectVariable variable = iterableExpression.createIterator(generatedName,
                                                                        expressionBuilder.getModelAdapter(),
                                                                        expressionBuilder.getExpressionResource());
            variable.setHumanName(lambdaArgument);
            context.pushVariable(variable);
        } else {
            iterableExpression.getIteratorVariable().setName(generatedName);
            context.pushVariable(iterableExpression.getIteratorVariable());
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

    public TypeName getTypeNameFromResource(String namespace, String name) {
        return expressionBuilder.getTypeNameFromResource(namespace, name);
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

    public String newGeneratedIteratorName() {
        return generatedIteratorPrefix + atomicCounter.getAndIncrement();
    }

}
