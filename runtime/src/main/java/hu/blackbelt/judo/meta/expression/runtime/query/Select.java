package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class Select implements Identifiable {

    /**
     * ASM entity type.
     */
    @NonNull
    private EClass from;

    /**
     * ASM transfer object type.
     */
    @NonNull
    private EClass target;

    /**
     * Map of attributes, keys are attributes of {@link #getTarget()}, fields are attributes of {@link #getFrom()} and {@link Join#getJoined()} or function calls.
     */
    @NonNull
    private Map<EAttribute, Feature> features;

    /**
     * Joined data sets for attributes of the same {@link #getTarget()}.
     */
    @NonNull
    private List<Join> joins;

    /**
     * Data sets retrieved in separate query. Keys are references of {@link #getFrom()}.
     */
    @NonNull
    private Map<EReference, SubSelect> subSelects;

    @NonNull
    private Collection<Function> filters;

    @lombok.Setter
    private String alias;

    // TODO - add ordering

    // TODO - add windowing (head, tail)


    @Override
    public EClass getObjectType() {
        return from;
    }

    public Stream<IdentifiableFeature> getIdentifiableFeatures() {
        return Stream.concat(
                Stream.concat(
                        features.values().stream()
                                .filter(f -> f instanceof IdentifiableFeature)
                                .map(f -> (IdentifiableFeature) f),
                        features.values().stream()
                                .filter(f -> f instanceof Function)
                                .flatMap(f -> ((Function) f).getIdentifiableFeatures())
                ),
                filters.stream().flatMap(f -> f.getIdentifiableFeatures())
        );
    }
}
