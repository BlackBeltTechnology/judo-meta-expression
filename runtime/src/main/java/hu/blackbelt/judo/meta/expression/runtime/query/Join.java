package hu.blackbelt.judo.meta.expression.runtime.query;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

@lombok.Getter
@lombok.Builder
public class Join {

    /**
     * ASM reference of {@link Select#getFrom()} and {@link #getJoined()} entity types.
     */
    private EReference reference;

    /**
     * ASM entity type.
     */
    private EClass joined;

    // TODO - add filtering
}
