package hu.blackbelt.judo.meta.expression.builder.jql;

/*-
 * #%L
 * JUDO :: Expression :: Model :: JQL builder
 * %%
 * Copyright (C) 2018 - 2023 BlackBelt Technology
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

    /**
     * <p>Use {0} for <i>target</i> class's name in <i>errorMessage</i></p>
     * <p>Use {1} for actual class's name in <i>errorMessage</i></p>
     */
    static <T extends Expression> T castExpression(Class<T> target, Supplier<? extends Expression> expressionSupplier, String errorMessage) {
        Object result = expressionSupplier.get();
        if (target.isInstance(result)) {
            return target.cast(result);
        }

        String format = errorMessage.replaceAll("\\{0}", target.getSimpleName())
                                    .replaceAll("\\{1}", result.getClass().getSimpleName());
        throw new IllegalArgumentException(format);
    }

    static void validateParameterCount(String functionName, List<FunctionParameter> parameters, Integer possibleParameterCount, Integer... possibleParameterCountExt) {
        List<Integer> possibleParameterCounts = new ArrayList<>(List.of(possibleParameterCountExt));
        possibleParameterCounts.add(possibleParameterCount);

        if (!possibleParameterCounts.contains(parameters.size())) {
            throw new IllegalArgumentException(String.format("Invalid number of arguments for '%s': Expected: %s, Got: %s",
                                                             functionName,
                                                             possibleParameterCounts.stream().sorted().map(Object::toString).collect(Collectors.joining(", ")),
                                                             parameters.size()));
        }
    }

}
