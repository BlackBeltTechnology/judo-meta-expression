package hu.blackbelt.judo.meta.expression.adapters.measure.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Unit {

    private String name;

    private String symbol;
}
