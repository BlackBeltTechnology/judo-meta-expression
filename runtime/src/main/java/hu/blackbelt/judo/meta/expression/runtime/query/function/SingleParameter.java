package hu.blackbelt.judo.meta.expression.runtime.query.function;

import hu.blackbelt.judo.meta.expression.runtime.query.Feature;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class SingleParameter implements Parameter {

    private Feature feature;
}
