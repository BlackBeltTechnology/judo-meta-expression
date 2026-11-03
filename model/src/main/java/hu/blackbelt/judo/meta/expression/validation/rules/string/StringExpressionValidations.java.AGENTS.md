# `StringExpressionValidations.java`

Zeta validation constraints + guards for `StringExpression` metamodel elements.
Exports `resolved()`, `attributeTypeIsString()`, `typeOfDefaultCaseIsString()`, `stringAggregatedExpressionResolved()`.
`StringAttribute` resolves to string type; `StringSwitchExpression` default expression is a `StringExpression`; `StringAggregatedExpression` variable resolves against collection object type.