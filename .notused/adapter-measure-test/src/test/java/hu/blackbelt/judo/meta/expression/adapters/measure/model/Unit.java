package hu.blackbelt.judo.meta.expression.adapters.measure.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Unit {

    private String name;

    private String symbol;
}
