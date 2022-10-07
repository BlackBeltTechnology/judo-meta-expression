package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.Expression;

import java.util.function.Supplier;

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

}
