package hu.blackbelt.judo.meta.expression.runtime.query;

@lombok.Getter
@lombok.Builder
@lombok.ToString(includeFieldNames = false)
public class Constant<T> implements Feature {

    private T value;
}
