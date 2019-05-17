package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    private List<Target> targets;

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
        return toString(0);
    }

    public boolean isContainment(final Feature feature) {
        if (feature instanceof Attribute) {
            return Objects.equals(((Attribute) feature).getSource(), this);
        } else if (feature instanceof IdAttribute) {
            return Objects.equals(((IdAttribute) feature).getSource(), this);
        } else {
            return false; // TODO - support functions
        }
    }

    public String toString(int level) {
        return pad(level) + "SELECT\n" +
                pad(level) + "  IDS=" + targets.stream().map(t -> t.getIdAttributes()).collect(Collectors.toList()) + "\n" +
                pad(level) + "  FEATURES=" + targets.stream().flatMap(t -> t.getFeatures().stream().filter(f -> isContainment(f))).collect(Collectors.toList()) + "\n" +
                pad(level) + "  FROM=" + from.getName() + " AS " + getSourceAlias() + "\n" +
                pad(level) + "  JOINING=" + joins + "\n" +
                pad(level) + "  TO=" + targets + "\n" +
                pad(level) + "  WHERE=" + filters + "\n" +
                pad(level) + "  TRAVERSE=" + subSelects.stream().map(s -> s.getJoins() + " AS " + s.getAlias() + "\n" + s.getSelect().toString(level + 1)).collect(Collectors.toList());
    }

    private static String pad(int level) {
        return StringUtils.leftPad("", level * 4, " ");
    }
}
