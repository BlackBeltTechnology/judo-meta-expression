package hu.blackbelt.judo.meta.expression.runtime.query;

import java.util.Collection;

@lombok.Getter
@lombok.Builder
public class Function implements Feature {

    /**
     * ASM entity type attribute.
     */
    private Collection<Feature> parameters;
}
