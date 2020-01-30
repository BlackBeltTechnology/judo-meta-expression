package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.OrderedCollectionExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.ExpressionBuildingVariableResolver;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.operator.ObjectSelector;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newSubCollectionExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newCountExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerArithmeticExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectSelectorExpressionBuilder;
import static org.eclipse.emf.ecore.util.EcoreUtil.copy;

public class JqlTailFunctionTransformer extends AbstractJqlFunctionTransformer<OrderedCollectionExpression> {

    public JqlTailFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(OrderedCollectionExpression collection, FunctionCall functionCall, ExpressionBuildingVariableResolver context) {
        if (functionCall.getParameters() == null || functionCall.getParameters().isEmpty()) {
            return newObjectSelectorExpressionBuilder().withCollectionExpression(collection).withOperator(ObjectSelector.TAIL).build();
        } else {
            Expression parameter = jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), context);
            if (!(parameter instanceof IntegerExpression)) {
                throw new IllegalArgumentException("Function parameter must be integer, got: " + parameter);
            }
            IntegerExpression length = newCountExpressionBuilder().withCollectionExpression(copy(collection)).build();
            IntegerExpression position = newIntegerArithmeticExpressionBuilder().withLeft(length).withRight((IntegerExpression) copy(parameter)).withOperator(IntegerOperator.SUBSTRACT).build();

            return newSubCollectionExpressionBuilder()
                    .withCollectionExpression(collection)
                    .withLength((IntegerExpression) parameter)
                    .withPosition(position).build();
        }
    }
}
