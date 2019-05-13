package hu.blackbelt.judo.meta.expression.runtime.query;

import hu.blackbelt.judo.meta.expression.runtime.query.function.LogicalFunction;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class Select implements Identifiable {

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

    private Collection<LogicalFunction> filters;

    // TODO - add ordering

    // TODO - add windowing (head, tail)


    @Override
    public String getAlias() {
        return from.getName(); // TODO - return FQ name
    }
}
