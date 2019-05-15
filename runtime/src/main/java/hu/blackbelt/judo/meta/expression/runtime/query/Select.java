package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.eclipse.emf.ecore.EClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@lombok.Getter
@lombok.Builder
public class Select extends Source {

    /**
     * ASM entity type.
     */
    @NonNull
    private EClass from;

    @NonNull
    private final List<Target> targets = new ArrayList<>();

    /**
     * Joined data sets for attributes of the same {@link #getTargets()}.
     */
    @NonNull
    private final List<Join> joins = new ArrayList<>();

    /**
     * Data sets retrieved in separate query.
     */
    @NonNull
    private final Collection<SubSelect> subSelects = new ArrayList<>();

    @NonNull
    private final Collection<Function> filters = new ArrayList<>();

    @Override
    public EClass getType() {
        return from;
    }

    // TODO - add filtering

    // TODO - add ordering

    // TODO - add windowing (head, tail)

    @Override
    public String toString() {
        return "SELECT\n" +
                "  IDS=" + targets.stream().map(t -> t.getIdAttributes()).collect(Collectors.toList()) + "\n" +
                "  FEATURES=" + targets.stream().flatMap(t -> t.getFeatures().stream()).collect(Collectors.toList()) + "\n" +
                "  FROM=" + from.getName() + " AS " + getSourceAlias() + "\n" +
                "  JOINING=" + joins + "\n" +
                "  TO=" + targets + "\n" +
                "  WHERE=" + filters + "\n" +
                " TRAVERSE=" + subSelects.stream().map(s -> s.getJoins() + " AS " + s.getAlias() + "\n" + s.getSelect()).collect(Collectors.toList());
    }
}
