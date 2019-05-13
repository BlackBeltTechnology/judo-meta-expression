package hu.blackbelt.judo.meta.expression.runtime.query.function;

import hu.blackbelt.judo.meta.expression.runtime.query.Feature;
import lombok.NonNull;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class SingleParameter implements Parameter {

    @NonNull
    private Feature feature;
}
