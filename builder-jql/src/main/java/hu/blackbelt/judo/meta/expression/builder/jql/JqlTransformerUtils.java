package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.jql.jqldsl.FunctionParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface JqlTransformerUtils {

    static <T extends Expression> T castExpression(Class<T> target, Supplier<? extends Expression> expressionSupplier) {
        return castExpression(target, expressionSupplier, "Invalid argument type: Expected: {0}, Got: {1}");
    }

    static <T extends Expression> T castExpression(Class<T> target, Supplier<? extends Expression> expressionSupplier, String errorMessage) {
        Object result = expressionSupplier.get();
        if (target.isInstance(result)) {
            return target.cast(result);
        }

        String format = errorMessage.replaceAll("\\{0}", target.getSimpleName())
                                    .replaceAll("\\{1}", result.getClass().getSimpleName());
        throw new IllegalArgumentException(format);
    }

    static void validateParameterCount(List<FunctionParameter> parameters, Integer possibleParameterCount, Integer... possibleParameterCountExt) {
        List<Integer> possibleParameterCounts = new ArrayList<>(List.of(possibleParameterCountExt));
        possibleParameterCounts.add(possibleParameterCount);

        if (!possibleParameterCounts.contains(parameters.size())) {
            throw new IllegalArgumentException(String.format("Invalid number of arguments: Expected: %s, Got: %s",
                                                             possibleParameterCounts.stream().sorted().map(Object::toString).collect(Collectors.joining(", ")),
                                                             parameters.size()));
        }
    }

}
