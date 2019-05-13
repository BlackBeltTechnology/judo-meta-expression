package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.eclipse.emf.ecore.EAttribute;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class Attribute implements IdentifiableFeature {

    /**
     * ASM entity type attribute.
     */
    @NonNull
    private EAttribute sourceAttribute;

    @lombok.Setter
    private Identifiable identifiable;
}
