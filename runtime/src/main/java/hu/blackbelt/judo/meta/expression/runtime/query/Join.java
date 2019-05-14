package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

@lombok.Getter
@lombok.Builder
public class Join implements Identifiable, IdentifiableFeature {

    /**
     * ASM reference of {@link Select#getFrom()}.
     */
    @NonNull
    private EReference reference;

    @lombok.Setter
    private Identifiable identifiable;

    @lombok.Setter
    private String alias;

    // TODO - add filtering

    @Override
    public EClass getObjectType() {
        return reference.getEReferenceType();
    }

    @Override
    public String toString() {
        return reference.getEReferenceType().getName() + "@" + (identifiable != null ? identifiable.getAlias() : reference.getEContainingClass().getName()) + "." + reference.getName() + " AS " + alias;
    }
}
