package hu.blackbelt.judo.meta.expression.adapters.asm;

import hu.blackbelt.judo.meta.asm.runtime.AsmUtils;
import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureProvider;
import hu.blackbelt.judo.meta.expression.adapters.measure.MeasureSupport;
import hu.blackbelt.judo.meta.measure.Unit;
import lombok.NonNull;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;

import static hu.blackbelt.judo.meta.expression.util.builder.ExpressionBuilders.newMeasureNameBuilder;

public class AsmMeasureSupport implements MeasureSupport<EClass, Unit> {

    private static final String MEASURE_NAME_KEY = "measure";
    private static final String UNIT_NAME_KEY = "unit";

    private final AsmUtils asmUtils;
    private final MeasureProvider measureProvider;

    public AsmMeasureSupport(final @NonNull ResourceSet asmResourceSet, final MeasureProvider measureProvider) {
        this.measureProvider = measureProvider;
        asmUtils = new AsmUtils(asmResourceSet);
    }

    @Override
    public Optional<Unit> getUnit(EClass type, String attributeName) {
        return type.getEAllAttributes().stream()
                .filter(a -> Objects.equals(a.getName(), attributeName))
                .map(a -> getUnit(a).orElse(null))
                .findAny();
    }

    private Optional<Unit> getUnit(final EAttribute attribute) {
        if (asmUtils.isNumeric(attribute.getEAttributeType())) {
            final Optional<EAnnotation> annotation = asmUtils.getExtensionAnnotation(attribute, false);
            if (annotation.isPresent()) {
                return measureProvider.getUnitByNameOrSymbol(parseMeasureName(annotation.get().getDetails().get(MEASURE_NAME_KEY)), annotation.get().getDetails().get(UNIT_NAME_KEY));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private static Optional<MeasureName> parseMeasureName(final String name) {
        if (name == null) {
            return Optional.empty();
        } else {
            final Matcher m = MEASURE_NAME_PATTERN.matcher(name);
            if (m.matches()) {
                return Optional.of(newMeasureNameBuilder()
                        .withNamespace(m.group(1))
                        .withName(m.group(2))
                        .build());
            } else {
                throw new IllegalArgumentException("Invalid measure name " + name);
            }
        }
    }
}
