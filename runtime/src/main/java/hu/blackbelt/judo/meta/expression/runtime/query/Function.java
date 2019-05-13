package hu.blackbelt.judo.meta.expression.runtime.query;

import hu.blackbelt.judo.meta.expression.runtime.query.function.FunctionSignature;
import hu.blackbelt.judo.meta.expression.runtime.query.function.Parameter;

import java.util.Map;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class Function implements Feature {

    private FunctionSignature signature;

    private Map<String, Parameter> parameters;

    private Class type;
}
