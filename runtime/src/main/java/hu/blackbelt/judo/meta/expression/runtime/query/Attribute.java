package hu.blackbelt.judo.meta.expression.runtime.query;

import org.eclipse.emf.ecore.EAttribute;

@lombok.Getter
@lombok.Builder
public class Attribute implements Feature {

    /**
     * ASM entity type attribute.
     */
    private EAttribute targetAttribute;
}
