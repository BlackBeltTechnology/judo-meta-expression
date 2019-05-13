package hu.blackbelt.judo.meta.expression.runtime.query;

import hu.blackbelt.judo.meta.expression.runtime.query.function.CollectionParameter;
import hu.blackbelt.judo.meta.expression.runtime.query.function.FunctionSignature;
import hu.blackbelt.judo.meta.expression.runtime.query.function.Parameter;
import hu.blackbelt.judo.meta.expression.runtime.query.function.SingleParameter;
import lombok.NonNull;

import java.util.Map;
import java.util.stream.Stream;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class Function implements Feature {

    @NonNull
    private FunctionSignature signature;

    @NonNull
    private Map<String, Parameter> parameters;

    @NonNull
    private Class type;

    public Stream<IdentifiableFeature> getIdentifiableFeatures() {
        // TODO - add nested functions

        return Stream.concat(parameters.values().stream()
                        .filter(p -> p instanceof SingleParameter)
                        .map(p -> ((SingleParameter) p).getFeature())
                        .filter(f -> f instanceof IdentifiableFeature)
                        .map(f -> (IdentifiableFeature) f),
                parameters.values().stream()
                        .filter(p -> p instanceof CollectionParameter)
                        .map(p -> ((CollectionParameter) p).getFeatures())
                        .flatMap(f -> f.stream())
                        .filter(f -> f instanceof IdentifiableFeature)
                        .map(f -> (IdentifiableFeature) f));
    }
}
