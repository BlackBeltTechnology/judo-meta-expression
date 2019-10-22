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
import hu.blackbelt.judo.meta.expression.builder.jql.function.JqlParameterizedFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.collection.*;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlMatchesFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlPositionFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlReplaceFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.string.JqlSubstringFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.function.temporal.JqlDifferenceFunctionTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlBinaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlTernaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.builder.jql.operation.JqlUnaryOperationTransformer;
import hu.blackbelt.judo.meta.expression.collection.CastCollection;
import hu.blackbelt.judo.meta.expression.logical.ContainsExpression;
import hu.blackbelt.judo.meta.expression.logical.InstanceOfExpression;
import hu.blackbelt.judo.meta.expression.logical.MemberOfExpression;
import hu.blackbelt.judo.meta.expression.logical.TypeOfExpression;
import hu.blackbelt.judo.meta.expression.object.CastObject;
import hu.blackbelt.judo.meta.expression.object.ContainerExpression;
import hu.blackbelt.judo.meta.expression.operator.DecimalAggregator;
import hu.blackbelt.judo.meta.expression.operator.IntegerAggregator;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.*;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;

import java.math.BigInteger;
import java.util.*;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCastCollectionBuilder;
import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newCollectionFilterExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.*;
import static hu.blackbelt.judo.meta.expression.logical.util.builder.LogicalBuilders.*;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.*;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newCastObjectBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newContainerExpressionBuilder;
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
        literals();
        operations();
        stringFunctions();
        numericFunctions();
        temporalFunctions();
        collectionFunctions();
        objectFunctions();
    }

    private static String createQNamespaceString(QualifiedName qName) {
        String qNamespaceString;
        if (qName.getNamespaceElements() != null && !qName.getNamespaceElements().isEmpty()) {
            qNamespaceString = String.join("::", qName.getNamespaceElements());
        } else {
            qNamespaceString = null;
        }
        return qNamespaceString;
    }

    private void objectFunctions() {
        functionTransformers.put("kindof", new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, InstanceOfExpression>(this,
                (expression, parameter) -> newInstanceOfExpressionBuilder().withObjectExpression(expression).withElementName(getTypeNameFromResource(parameter)).build(),
                (jqlExpression) -> ((NavigationExpression) jqlExpression).getBase()
        ));
        functionTransformers.put("typeof", new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, TypeOfExpression>(this,
                (expression, parameter) -> newTypeOfExpressionBuilder().withObjectExpression(expression).withElementName(getTypeNameFromResource(parameter)).build(),
                (jqlExpression -> ((NavigationExpression) jqlExpression).getBase())
        ));

        functionTransformers.put("astype", new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, CastObject>(this,
                (expression, parameter) -> newCastObjectBuilder().withElementName(getTypeNameFromResource(parameter)).withObjectExpression(expression).build(),
                jqlExpression -> ((NavigationExpression)jqlExpression).getBase()));

        functionTransformers.put("container", new JqlParameterizedFunctionTransformer<ObjectExpression, QualifiedName, ContainerExpression>(this,
                (expression, parameter) -> newContainerExpressionBuilder().withElementName(getTypeNameFromResource(parameter)).withObjectExpression(expression).build(),
                jqlExpression -> ((NavigationExpression)jqlExpression).getBase()));

    }

    private void collectionFunctions() {
        functionTransformers.put("count", (expression, functionCall, variables) -> newCountExpressionBuilder().withCollectionExpression((CollectionExpression) expression).build());
        functionTransformers.put("head", new JqlHeadFunctionTransformer(this));
        functionTransformers.put("tail", new JqlTailFunctionTransformer(this));
        functionTransformers.put("filter", new JqlParameterizedFunctionTransformer<CollectionExpression, LogicalExpression, FilteringExpression>(this,
                (expression, parameter) -> newCollectionFilterExpressionBuilder().withCollectionExpression(expression).withCondition(parameter).build()));
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
                (expression, parameter) -> newCastCollectionBuilder().withElementName(getTypeNameFromResource(parameter)).withCollectionExpression(expression).build(),
                jqlExpression -> ((NavigationExpression)jqlExpression).getBase()));

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
        functionTransformers.put("position", new JqlPositionFunctionTransformer(this));
        functionTransformers.put("replace", new JqlReplaceFunctionTransformer(this));
        functionTransformers.put("first", new JqlParameterizedFunctionTransformer<StringExpression, IntegerExpression, Expression>(this,
                (stringExpression, parameter) -> newSubStringBuilder()
                        .withExpression(stringExpression)
                        .withPosition(newIntegerConstantBuilder().withValue(BigInteger.ZERO).build())
                        .withLength(parameter).build()));
        functionTransformers.put("last", new JqlParameterizedFunctionTransformer<StringExpression, IntegerExpression, Expression>(this,
                (stringExpression, parameter) -> {
                    IntegerExpression stringLength = newLengthBuilder().withExpression(copy(stringExpression)).build();
                    IntegerExpression position = newIntegerAritmeticExpressionBuilder().withLeft(stringLength).withRight(copy(parameter)).withOperator(IntegerOperator.SUBSTRACT).build();
                    return newSubStringBuilder()
                            .withExpression(stringExpression)
                            .withPosition(position)
                            .withLength(parameter).build();
                }));
        functionTransformers.put("matches", new JqlMatchesFunctionTransformer(this));
    }

    private void operations() {
        transformers.put(BinaryOperation.class, new JqlBinaryOperationTransformer<>(this));
        transformers.put(UnaryOperation.class, new JqlUnaryOperationTransformer<>(this));
        transformers.put(TernaryOperation.class, new JqlTernaryOperationTransformer<>(this));
    }

    private void literals() {
        transformers.put(NavigationExpression.class, new JqlNavigationTransformer<>(this));
        transformers.put(BooleanLiteral.class, (jqlExpression, variables) -> newBooleanConstantBuilder().withValue(((BooleanLiteral) jqlExpression).isIsTrue()).build());
        transformers.put(DecimalLiteral.class, (jqlExpression, variables) -> newDecimalConstantBuilder().withValue(((DecimalLiteral) jqlExpression).getValue()).build());
        transformers.put(IntegerLiteral.class, (jqlExpression, variables) -> newIntegerConstantBuilder().withValue(((IntegerLiteral) jqlExpression).getValue()).build());
        transformers.put(StringLiteral.class, (jqlExpression, variables) -> newStringConstantBuilder().withValue(((StringLiteral) jqlExpression).getValue()).build());
        transformers.put(MeasuredLiteral.class, new JqlMeasuredLiteralTransformer<>(this));
        transformers.put(DateLiteral.class, new JqlDateLiteralTransformer());
        transformers.put(TimeStampLiteral.class, new JqlTimestampLiteralTransformer());
        transformers.put(EnumLiteral.class, new JqlEnumLiteralTransformer<>(this));
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

    public TypeName getTypeNameFromResource(QualifiedName qName) {
        String namespace = createQNamespaceString(qName);
        return JqlExpressionBuilder.all(expressionResource.getResourceSet(), TypeName.class).filter(tn -> Objects.equals(tn.getName(), qName.getName()) && Objects.equals(tn.getNamespace(), namespace)).findAny().orElse(null);
    }

    public Resource getExpressionResource() {
        return expressionResource;
    }
}
