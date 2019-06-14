package hu.blackbelt.judo.meta.expression.adapters.measure.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;

@Getter
@Builder
public class Measure {

    private String namespace;

    private String name;

    @NonNull
    private Collection<Unit> units;
}
