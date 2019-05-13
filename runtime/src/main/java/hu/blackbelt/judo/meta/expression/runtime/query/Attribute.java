package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.eclipse.emf.ecore.EAttribute;

@lombok.Getter
@lombok.Builder
public class Attribute implements IdentifiableFeature {

    /**
     * ASM entity type attribute.
     */
    @NonNull
    private EAttribute sourceAttribute;

    @lombok.Setter
    private Identifiable identifiable;

    @Override
    public String toString() {
        return identifiable.getAlias() + "." + sourceAttribute.getName();
    }
}
