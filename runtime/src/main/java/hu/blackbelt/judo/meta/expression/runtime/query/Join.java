package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class Join implements Identifiable {

    /**
     * ASM reference of {@link Select#getFrom()} and {@link #getJoined()} entity types.
     */
    @NonNull
    private EReference reference;

    @lombok.Setter
    private Identifiable identifiable;

    /**
     * ASM entity type.
     */
    @NonNull
    private EClass joined;

    @lombok.Setter
    private String alias;

    // TODO - add filtering


    @Override
    public EClass getObjectType() {
        return joined;
    }
}
