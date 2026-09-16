# `ExpressionZetaValidator.java`

Java alternative to the EVL run: registers the 16 rule classes listed in `VALIDATOR_CLASSES`, builds `ExpressionValidationContext`, and runs `ValidationExecutor` over every `EObject` of the model resource.
Exports static `validateExpression` in 3-, 5- and 6-arg (with `parallel`) forms.
Results are compared by constraint name; a null `expectedWarnings` means warnings are ignored, mismatch throws `ExpressionValidationException`.