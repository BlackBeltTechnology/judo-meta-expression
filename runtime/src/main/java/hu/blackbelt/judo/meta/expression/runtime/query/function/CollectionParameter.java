package hu.blackbelt.judo.meta.expression.runtime.query.function;

import hu.blackbelt.judo.meta.expression.runtime.query.Feature;

import java.util.ArrayList;
import java.util.Collection;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class CollectionParameter implements Parameter {

    private final Collection<Feature> features = new ArrayList<>();
}
