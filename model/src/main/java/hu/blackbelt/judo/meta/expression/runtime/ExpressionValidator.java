package hu.blackbelt.judo.meta.expression.runtime;

import hu.blackbelt.judo.meta.expression.Expression;
import hu.blackbelt.judo.meta.expression.TypeName;
import hu.blackbelt.judo.meta.expression.VariableReference;
import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import org.eclipse.emf.ecore.resource.Resource;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.blackbelt.judo.meta.expression.runtime.ExpressionUtils.getValue;

public class ExpressionValidator {

    public static String format(Collection<String> unsatisfiedErrors, Collection<String> unsatisfiedWarnings) {
        if (isEmpty(unsatisfiedErrors) && isEmpty(unsatisfiedWarnings)) {
            return "All constraints have been satisfied";
        }

        // Generate the error and warning sections separately, then join them.
        return Stream.of(
                        formatSection("error", unsatisfiedErrors),
                        formatSection("warning", unsatisfiedWarnings)
                )
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining("\t\n"));
    }

    private static String formatSection(String type, Collection<String> items) {
        if (isEmpty(items)) {
            return "";
        }
        String header = "\t" + items.size() + " " + type + "(s)\n";
        return items.stream()
                .map(item -> "\t" + item)
                .collect(Collectors.joining("\n", header, "")); // Collector with prefix
    }

    public static void validate(Logger log, Collection<String> expectedErrors, Collection<String> expectedWarnings,
                         Collection<String> unsatisfiedErrors, Collection<String> unsatisfiedWarnings) throws ExpressionValidationException {

        // If no expectations are set, fail only if there are unsatisfied errors.
        if (isEmpty(expectedErrors) && isEmpty(expectedWarnings)) {
            if (!isEmpty(unsatisfiedErrors)) {
                throw new ExpressionValidationException(format(unsatisfiedErrors, unsatisfiedWarnings));
            }
            if (log != null && !isEmpty(unsatisfiedWarnings)) {
                log.warn(format(unsatisfiedErrors, unsatisfiedWarnings));
            }
            return; // Success
        }

        // Use sets for efficient difference operations (A - B).
        final Set<String> expectedErrSet = toSet(expectedErrors);
        final Set<String> unsatisfiedErrSet = toSet(unsatisfiedErrors);
        final Set<String> expectedWarnSet = toSet(expectedWarnings);
        final Set<String> unsatisfiedWarnSet = toSet(unsatisfiedWarnings);

        // Errors in the actual results but not in the expected results.
        Set<String> unexpectedErrors = new HashSet<>(unsatisfiedErrSet);
        unexpectedErrors.removeAll(expectedErrSet);

        // Errors in the expected results but not in the actual results.
        Set<String> errorsNotFound = new HashSet<>(expectedErrSet);
        errorsNotFound.removeAll(unsatisfiedErrSet);

        // Warnings in the actual results but not in the expected results.
        Set<String> unexpectedWarnings = new HashSet<>(unsatisfiedWarnSet);
        unexpectedWarnings.removeAll(expectedWarnSet);

        // Warnings in the expected results but not in the actual results.
        Set<String> warningsNotFound = new HashSet<>(expectedWarnSet);
        warningsNotFound.removeAll(unsatisfiedWarnSet);

        boolean failed = !unexpectedErrors.isEmpty() || !errorsNotFound.isEmpty() ||
                !warningsNotFound.isEmpty();

        if (failed) {
            throw new ExpressionValidationException(format(unsatisfiedErrors, unsatisfiedWarnings));
        } else if (!unexpectedWarnings.isEmpty()) {
            log.warn(format(unsatisfiedErrors, unsatisfiedWarnings));
        }
    }

    // Helper utilities to avoid NullPointerExceptions and reduce verbosity.
    private static boolean isEmpty(Collection<String> collection) {
        return collection == null || collection.isEmpty();
    }

    private static Set<String> toSet(Collection<String> collection) {
        return isEmpty(collection) ? Collections.emptySet() : new HashSet<>(collection);
    }

    public static void validateExpressionInJava(Logger log, ExpressionModel expressionModel, ModelAdapter modelAdapter, Collection<String> expectedErrors, Collection<String> expectedWarnings) throws ExpressionValidationException {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        Collection<String> errors = new ArrayList<>();
        Collection<String> warnings = new ArrayList<>();

        /*
            context EXPR!TypeName {

                // object type with name defined by ElementName must exists in the namespace
                constraint ObjectTypeIsValid {
                    check: self.get(modelAdapter).isDefined()
                    message: "Element named " + self.name + " not found in namespace " + self.namespace
                }
            }
         */
        errors.addAll(expressionModel.getExpressionModelResourceSupport().getStreamOf(TypeName.class)
                .filter(self -> self.get(modelAdapter) == null)
                .map(self -> "Element named " + self.getName() + " not found in namespace " + self.getNamespace()).toList());
        /*
            context EXPR!Expression {

                // variable reference in lambda expression is referencing to variable visible from its scope
                constraint LambdaVariableIsValid {
                    guard: evaluator.isLambdaFunction(self)

                    check: evaluator.getVariablesOfScope(self).containsAll(evaluator.getExpressionTerms(self).select(e | e.isKindOf(EXPR!VariableReference)).collect(e | e.variable))
                    message: "Invalid variable references: " + evaluator.getExpressionTerms(self).select(e | e.isKindOf(EXPR!VariableReference)).collect(e | e.variable).excludingAll(evaluator.getVariablesOfScope(self)) + " in expression: " + self
                }
            }
         */
        errors.addAll(expressionModel.getExpressionModelResourceSupport().getStreamOf(Expression.class)
                .filter(self -> evaluator.isLambdaFunction(self))
                .filter(self ->
                        evaluator.getVariablesOfScope(self).containsAll(
                                evaluator.getExpressionTerms(self).stream()
                                        .filter(e -> e instanceof VariableReference)
                                        .map(e -> getValue(e, "variable")).toList()))
                .map(self -> "Invalid variable references: " + evaluator.getExpressionTerms(self).stream()
                        .filter(e -> e instanceof VariableReference)
                        .map(e -> getValue(e, "variable")).toList()
                        .removeAll(evaluator.getVariablesOfScope(self)) + " in expression: " + self)
                .toList());

        validate(log, expectedErrors, expectedWarnings, errors, warnings);
    }

    public static void validateExpression(Logger log, ExpressionModel expressionModel, ModelAdapter modelAdapter, String adaptedName, Resource adapted, String measureName, Resource measure, Collection<String> expectedErrors, Collection<String> expectedWarnings) throws ExpressionValidationException {
        ExpressionValidatorExecutor expressionValidatorExecutor = ReflectiveExpressionValidatorFactory.createExecutor("hu.blackbelt.judo.meta.expression.runtime.ExpressionEpsilonValidatorExecutor", () -> {
            validateExpressionInJava(log, expressionModel, modelAdapter, expectedErrors, expectedWarnings);
        });
        expressionValidatorExecutor.setSource(log, expressionModel, modelAdapter, adaptedName, adapted, measureName, measure);
        expressionValidatorExecutor.execute(expectedErrors, expectedWarnings);


    }
}
