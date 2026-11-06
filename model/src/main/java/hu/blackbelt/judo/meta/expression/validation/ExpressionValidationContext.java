package hu.blackbelt.judo.meta.expression.validation;

/*-
 * #%L
 * Judo :: Expression :: Model
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

import hu.blackbelt.judo.meta.expression.adapters.ModelAdapter;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionEvaluator;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.zeta.common.ExtensionMethodRegistry;
import hu.blackbelt.judo.zeta.common.ModelProvider;
import hu.blackbelt.judo.zeta.validation.core.ValidationContext;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validation context for Expression model validation.
 *
 * <p>Provides access to the model adapter and expression evaluator
 * needed by validation rules.</p>
 */
public class ExpressionValidationContext extends ValidationContext {

    private final ExpressionModel expressionModel;
    private final ModelAdapter modelAdapter;
    private final ExpressionEvaluator evaluator;
    
    // Cache for satisfies checks
    private final Map<String, Set<EObject>> satisfiedConstraints = new ConcurrentHashMap<>();

    public ExpressionValidationContext(
            ExpressionModel expressionModel,
            ModelAdapter modelAdapter,
            ExpressionEvaluator evaluator
    ) {
        super(
                createModelProvider(expressionModel),
                expressionModel.getResourceSet(),
                new ExtensionMethodRegistry()
        );
        this.expressionModel = expressionModel;
        this.modelAdapter = modelAdapter;
        this.evaluator = evaluator;
    }
    
    private static ModelProvider createModelProvider(ExpressionModel model) {
        return new ModelProvider() {
            @Override
            public <T extends EObject> Collection<T> getAllContents(ResourceSet resourceSet, Class<T> type) {
                Collection<T> result = new ArrayList<>();
                TreeIterator<?> iterator = resourceSet.getAllContents();
                while (iterator.hasNext()) {
                    Object obj = iterator.next();
                    if (type.isInstance(obj)) {
                        result.add(type.cast(obj));
                    }
                }
                return result;
            }
        };
    }

    /**
     * Get the Expression model being validated.
     */
    public ExpressionModel getExpressionModel() {
        return expressionModel;
    }

    /**
     * Get the model adapter for type resolution and other operations.
     */
    public ModelAdapter getModelAdapter() {
        return modelAdapter;
    }

    /**
     * Get the expression evaluator for lambda and scope checks.
     */
    public ExpressionEvaluator getEvaluator() {
        return evaluator;
    }

    /**
     * Check if an element satisfies a constraint.
     *
     * @param element the element to check
     * @param constraintName the constraint name
     * @return true if the constraint is satisfied
     */
    public boolean satisfies(EObject element, String constraintName) {
        Set<EObject> satisfied = satisfiedConstraints.get(constraintName);
        return satisfied != null && satisfied.contains(element);
    }

    /**
     * Check if an element satisfies all specified constraints.
     *
     * @param element the element to check
     * @param constraintNames the constraint names
     * @return true if all constraints are satisfied
     */
    public boolean satisfiesAll(EObject element, String... constraintNames) {
        for (String constraintName : constraintNames) {
            if (!satisfies(element, constraintName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Mark a constraint as satisfied for an element.
     *
     * @param element the element
     * @param constraintName the constraint name
     */
    public void markSatisfied(EObject element, String constraintName) {
        satisfiedConstraints
                .computeIfAbsent(constraintName, k -> ConcurrentHashMap.newKeySet())
                .add(element);
    }

    /**
     * Clear the satisfies cache.
     */
    @Override
    public void clearSatisfiesCache() {
        satisfiedConstraints.clear();
    }
}
