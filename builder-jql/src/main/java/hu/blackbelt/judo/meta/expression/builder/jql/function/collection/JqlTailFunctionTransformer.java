package hu.blackbelt.judo.meta.expression.builder.jql.function.collection;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.IntegerExpression;
import hu.blackbelt.judo.meta.expression.OrderedCollectionExpression;
import hu.blackbelt.judo.meta.expression.builder.jql.JqlTransformers;
import hu.blackbelt.judo.meta.expression.builder.jql.function.AbstractJqlFunctionTransformer;
import hu.blackbelt.judo.meta.expression.operator.IntegerOperator;
import hu.blackbelt.judo.meta.expression.operator.ObjectSelector;
import hu.blackbelt.judo.meta.expression.variable.ObjectVariable;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionCall;

import java.math.BigInteger;
import java.util.List;

import static hu.blackbelt.judo.meta.expression.collection.util.builder.CollectionBuilders.newSubCollectionExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.constant.util.builder.ConstantBuilders.newIntegerConstantBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newCountExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.numeric.util.builder.NumericBuilders.newIntegerAritmeticExpressionBuilder;
import static hu.blackbelt.judo.meta.expression.object.util.builder.ObjectBuilders.newObjectSelectorExpressionBuilder;
import static org.eclipse.emf.ecore.util.EcoreUtil.copy;

public class JqlTailFunctionTransformer extends AbstractJqlFunctionTransformer {

    public JqlTailFunctionTransformer(JqlTransformers jqlTransformers) {
        super(jqlTransformers);
    }

    @Override
    public Expression apply(Expression argument, FunctionCall functionCall, List<ObjectVariable> variables) {
        OrderedCollectionExpression collection = (OrderedCollectionExpression)argument;
        if (functionCall.getParameters() == null || functionCall.getParameters().isEmpty()) {
            return newObjectSelectorExpressionBuilder().withCollectionExpression((OrderedCollectionExpression) argument).withOperator(ObjectSelector.TAIL).build();
        } else {
            Expression parameter = jqlTransformers.transform(functionCall.getParameters().get(0).getExpression(), variables);
            if (!(parameter instanceof IntegerExpression)) {
                throw new IllegalArgumentException("Function parameter must be integer, got: " + parameter);
            }
            IntegerExpression length = newCountExpressionBuilder().withCollectionExpression(copy(collection)).build();
            IntegerExpression position = newIntegerAritmeticExpressionBuilder().withLeft(length).withRight((IntegerExpression)copy(parameter)).withOperator(IntegerOperator.SUBSTRACT).build();

            return newSubCollectionExpressionBuilder()
                    .withCollectionExpression(collection)
                    .withLength((IntegerExpression)parameter)
                    .withPosition(position).build();
        }
    }
}
