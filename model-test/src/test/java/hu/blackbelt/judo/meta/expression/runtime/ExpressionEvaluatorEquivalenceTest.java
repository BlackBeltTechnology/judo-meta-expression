package hu.blackbelt.judo.meta.expression.runtime;

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

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.ReferenceExpression;
import hu.blackbelt.judo.meta.expression.VariableReference;
import hu.blackbelt.judo.meta.expression.collection.CollectionVariableReference;
import hu.blackbelt.judo.meta.expression.object.ObjectVariableReference;
import hu.blackbelt.judo.meta.expression.variable.Variable;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Opt-in check that {@link ExpressionEvaluator#init(Collection)} computes the same leaves, lambda containers and
 * reference sources as the former O(n^2) algorithm (copied verbatim below as {@link #oldInit}) on a real model.
 * Runs only with {@code -Dexpression.equivalence.model=<path to *-expression.model>} (the ASM model it references must
 * lie next to it), e.g. the {@code target/generated-resources/model/<name>-expression.model} of an application build.
 */
class ExpressionEvaluatorEquivalenceTest {

    private static final String PROPERTY = "expression.equivalence.model";

    @Test
    void newInitMatchesFormerAlgorithm() throws Exception {
        String path = System.getProperty(PROPERTY);
        assumeTrue(path != null && !path.isBlank(), "set -D" + PROPERTY + " to run");

        File file = new File(path).getAbsoluteFile();
        ExpressionModel model = ExpressionModel.loadExpressionModel(ExpressionModel.LoadArguments.expressionLoadArgumentsBuilder()
                .file(file).uri(URI.createFileURI(file.getPath())).name("equivalence").validateModel(false));
        // the ASM model referenced by the expressions is resolved relative to the expression model file
        model.getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap()
                .putIfAbsent("*", new XMIResourceFactoryImpl());
        Resource resource = model.getResource();
        List<Expression> roots = resource.getContents().stream()
                .filter(Expression.class::isInstance).map(Expression.class::cast).collect(Collectors.toList());

        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        long start = System.nanoTime();
        evaluator.init(roots);
        long newMs = (System.nanoTime() - start) / 1_000_000;

        Old old = new Old();
        start = System.nanoTime();
        old.oldInit(roots);
        long oldMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("expressions=%d leaves=%d lambdaContainers=%d referenceSources=%d; init new %d ms, old %d ms%n",
                old.allExpressions.size(), old.leaves.size(), old.lambdaContainers.size(), old.referenceSources.size(),
                newMs, oldMs);

        assertEquals(identity(old.allExpressions), identity(field(evaluator, "allExpressions")), "allExpressions");
        assertEquals(identity(old.leaves), identity(field(evaluator, "leaves")), "leaves");
        assertEquals(identityMap(old.lambdaContainers), identityMap(field(evaluator, "lambdaContainers")), "lambdaContainers");
        assertEquals(identityMap(old.referenceSources), identityMap(field(evaluator, "referenceSources")), "referenceSources");
    }

    @SuppressWarnings("unchecked")
    private static <T> T field(Object target, String name) throws Exception {
        Field f = ExpressionEvaluator.class.getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(target);
    }

    private static Set<Integer> identity(Collection<?> objects) {
        return objects.stream().map(System::identityHashCode).collect(Collectors.toSet());
    }

    private static Map<Integer, Integer> identityMap(Map<?, ?> map) {
        Map<Integer, Integer> result = new HashMap<>();
        map.forEach((k, v) -> result.put(System.identityHashCode(k), System.identityHashCode(v)));
        return result;
    }

    /** The former algorithm of ExpressionEvaluator.init, verbatim (only the field owner changed). */
    private static final class Old {
        final Set<Expression> allExpressions = new HashSet<>();
        final Map<Expression, ReferenceExpression> lambdaContainers = new HashMap<>();
        final Map<ReferenceExpression, ReferenceExpression> referenceSources = new HashMap<>();
        final Set<Expression> leaves = new HashSet<>();

        void oldInit(final Collection<Expression> expressions) {
            expressions.forEach(this::addToAllExpressions);
            Set<Expression> allPlainExpression = getAllInstances(Expression.class).collect(Collectors.toSet());
            Set<ReferenceExpression> allReferenceExpression = getAllInstances(ReferenceExpression.class).collect(Collectors.toSet());
            allPlainExpression.forEach(expression -> {
                final Optional<ReferenceExpression> lambdaContainer = allReferenceExpression.stream()
                        .filter(referenceExpression -> referenceExpression.getLambdaFunctions().contains(expression))
                        .findAny();
                lambdaContainer.ifPresent(c -> lambdaContainers.put(expression, c));
            });
            allReferenceExpression.forEach(referenceExpression -> {
                final Optional<ReferenceExpression> referenceSource = allReferenceExpression.stream()
                        .filter(referenceSourceExpression -> referenceExpression.getOperands().contains(referenceSourceExpression))
                        .findAny();
                if (referenceSource.isPresent()) {
                    ReferenceExpression referenceSourceExpression = referenceSource.get();
                    while (referenceSourceExpression != null && referenceSourceExpression instanceof VariableReference) {
                        final Variable resolved;
                        if (referenceSourceExpression instanceof ObjectVariableReference) {
                            resolved = ((ObjectVariableReference) referenceSourceExpression).getVariable();
                        } else if (referenceSourceExpression instanceof CollectionVariableReference) {
                            resolved = ((CollectionVariableReference) referenceSourceExpression).getVariable();
                        } else {
                            throw new IllegalStateException("Unsupported variable reference");
                        }
                        if (resolved instanceof ReferenceExpression) {
                            referenceSourceExpression = (ReferenceExpression) resolved;
                        } else {
                            throw new IllegalStateException("Object variable is not a reference");
                        }
                    }
                    referenceSources.put(referenceExpression, referenceSourceExpression);
                }
            });
            leaves.addAll(getAllInstances(Expression.class)
                    .filter(expression -> !getAllInstances(Expression.class)
                            .anyMatch(e -> (e.getOperands().contains(expression) || e.getLambdaFunctions().contains(expression))))
                    .collect(Collectors.toSet()));
        }

        private void addToAllExpressions(final Expression expression) {
            if (!allExpressions.contains(expression)) {
                allExpressions.add(expression);
                expression.getOperands().forEach(this::addToAllExpressions);
                expression.getLambdaFunctions().forEach(this::addToAllExpressions);
            }
        }

        @SuppressWarnings("unchecked")
        private <T> java.util.stream.Stream<T> getAllInstances(final Class<T> clazz) {
            return allExpressions.stream().filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
        }
    }
}
