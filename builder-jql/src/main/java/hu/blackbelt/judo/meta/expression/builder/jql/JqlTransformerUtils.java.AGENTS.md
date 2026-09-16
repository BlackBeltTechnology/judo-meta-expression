# `JqlTransformerUtils.java`

Two static argument guards shared by function transformers.
`castExpression(Class<T>, Supplier)` returns the built expression cast to `T` or throws `IllegalArgumentException`, substituting `{0}` with the target and `{1}` with the actual simple class name in the message template.
`validateParameterCount(functionName, parameters, count, moreCounts...)` throws unless the parameter list size matches one of the allowed counts, and returns that size.