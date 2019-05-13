package hu.blackbelt.judo.meta.expression.runtime.query;

import hu.blackbelt.judo.meta.expression.runtime.query.function.Parameter;

import java.util.Map;

public interface Function extends Feature {

    Map<String, Parameter> getParameters();
}
