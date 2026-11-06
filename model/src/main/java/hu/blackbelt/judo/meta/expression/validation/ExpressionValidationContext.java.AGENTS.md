# `ExpressionValidationContext.java`

Carries adapter, evaluator and inter-constraint state to every Zeta rule.
Extends `ValidationContext` with `getExpressionModel`, `getModelAdapter`, `getEvaluator`, `satisfies(element, constraintName)`, `satisfiesAll`, `markSatisfied`, `clearSatisfiesCache`.
The `satisfiedConstraints` `ConcurrentHashMap` is empty until a rule calls `markSatisfied`, so a guard on an unrecorded constraint reads false.