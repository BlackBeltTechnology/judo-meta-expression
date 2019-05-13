package hu.blackbelt.judo.meta.expression.runtime.query;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class Join implements Identifiable {

    /**
     * ASM reference of {@link Select#getFrom()} and {@link #getJoined()} entity types.
     */
    private EReference reference;

    private Identifiable identifiable;

    /**
     * ASM entity type.
     */
    private EClass joined;

    // TODO - add filtering

    @Override
    public String getAlias() {
        return joined.getName(); // TODO - return FQ name + reference name
    }
}
