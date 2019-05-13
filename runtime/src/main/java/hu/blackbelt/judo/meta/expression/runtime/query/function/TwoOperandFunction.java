package hu.blackbelt.judo.meta.expression.runtime.query.function;

import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.expression.runtime.query.Function;

import java.util.Map;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class TwoOperandFunction implements Function {

    private final Parameter left;
    private final Parameter right;

    @Override
    public Map<String, Parameter> getParameters() {
        return ImmutableMap.of("left", left, "right", right);
    }
}
