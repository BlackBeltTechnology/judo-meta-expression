package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.DataExpression;
import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.constant.Instance;
import hu.blackbelt.judo.meta.expression.constant.InstanceId;
import hu.blackbelt.judo.meta.expression.constant.InstanceReference;
import hu.blackbelt.judo.meta.expression.variable.CollectionVariable;

import java.util.Map;

@lombok.Getter
@lombok.Builder
public class EvaluationNode<T extends Expression> {

    private T expression;

    private Map<AttributeRole, Expression> terminals;

    private Map<SetTransformation, EvaluationNode> navigations;

    private Map<String, DataExpression> operations;

    @Override
    public String toString() {
        final String expr;
        if (expression instanceof CollectionVariable) {
            if (((CollectionVariable) expression).getIteratorVariable() != null) {
                expr = expression + "#" + ((CollectionVariable) expression).getIteratorVariable().getName();
            } else {
                expr = String.valueOf(expression);
            }
        } else if (expression instanceof Instance) {
            final Instance instance = (Instance) expression;
            if (instance.getDefinition() instanceof InstanceId) {
                expr = "self(id=" + ((InstanceId) instance.getDefinition()).getId() + ")";
            } else if (instance.getDefinition() instanceof InstanceReference) {
                expr = ((InstanceReference) instance.getDefinition()).getVariableName();
            } else {
                expr = ((Instance) expression).getElementName().getName() + "!self";
            }
        } else {
            expr = String.valueOf(expression);
        }
        return "[[" + expr +
                "; terminals=" + terminals +
                "; navigations=" + navigations +
                "; operations=" + operations +
                "]]";
    }
}
