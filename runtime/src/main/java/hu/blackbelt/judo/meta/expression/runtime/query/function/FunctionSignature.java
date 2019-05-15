package hu.blackbelt.judo.meta.expression.runtime.query.function;

import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.runtime.query.Function;

import java.util.Map;

public enum FunctionSignature {

    EQUALS(boolean.class, ImmutableMap.of(TwoOperandFunctionParameterName.left.name(), true, TwoOperandFunctionParameterName.right.name(), true)),
    CONTAINS(boolean.class, ImmutableMap.of("set", true, "element", true));

    private Map<String, Boolean> parameters;
    private Class type;

    FunctionSignature(final Class type, final Map<String, Boolean> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public Function create(final Map<String, Parameter> parameters) {
        if (!this.parameters.keySet().containsAll(parameters.keySet())) {
            throw new IllegalArgumentException("Invalid parameter(s)");
        }
        if (this.parameters.entrySet().stream().anyMatch(e -> e.getValue() && !parameters.containsKey(e.getKey()))) {
            throw new IllegalArgumentException("Missing required parameter(s)");
        }
        final Function function = Function.builder()
                .signature(this)
                .type(type)
                .build();
        function.getParameters().putAll(ImmutableMap.copyOf(parameters));
        return function;
    }

    public enum TwoOperandFunctionParameterName {
        left, right
    }
}
