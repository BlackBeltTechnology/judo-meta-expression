package hu.blackbelt.judo.meta.expression.runtime.query;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.util.List;
import java.util.Map;

@lombok.Getter
@lombok.Builder
public class Select {

    /**
     * ASM entity type.
     */
    private EClass from;

    /**
     * ASM transfer object type.
     */
    private EClass target;

    /**
     * Map of attributes, keys are attributes of {@link #getTarget()}, fields are attributes of {@link #getFrom()} and {@link Join#getJoined()} or function calls.
     */
    private Map<EAttribute, Feature> features;

    /**
     * Joined data sets for attributes of the same {@link #getTarget()}.
     */
    private List<Join> joins;

    /**
     * Data sets retrieved in separate query. Keys are references of {@link #getFrom()}.
     */
    private Map<EReference, SubSelect> subSelects;

    // TODO - add filtering

    // TODO - add ordering

    // TODO - add windowing (head, tail)
}
