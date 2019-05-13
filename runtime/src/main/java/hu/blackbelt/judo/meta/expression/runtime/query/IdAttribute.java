package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;
import org.eclipse.emf.ecore.EClass;

@lombok.Getter
@lombok.Builder
public class IdAttribute implements IdentifiableFeature {

    @NonNull
    private EClass type;

    @lombok.Setter
    private Identifiable identifiable;

    @Override
    public String toString() {
        final String typeName;
        if (identifiable != null) {
            typeName = identifiable.getAlias() != null ? identifiable.getAlias() : identifiable.getObjectType().getName();
        } else {
            typeName = type.getName();
        }

        return "ID of " + typeName;
    }
}
