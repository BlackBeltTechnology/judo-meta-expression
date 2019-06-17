package hu.blackbelt.judo.meta.expression.adapters.measure;

import hu.blackbelt.judo.meta.expression.adapters.measure.model.Measure;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Unit;

import java.util.*;
import java.util.stream.Stream;

public class DummyMeasureProvider implements MeasureProvider<Measure, Unit> {

    private final Collection<Measure> measures;

    private final Measure time;
    private final Measure length;
    private final Measure mass;
    private final Measure velocity;

    public DummyMeasureProvider() {
        time = Measure.builder()
                .namespace("system")
                .name("Time")
                .units(Arrays.asList(
                        Unit.builder().name("millisecond").symbol("ms").build(),
                        Unit.builder().name("second").symbol("s").build(),
                        Unit.builder().name("minute").symbol("m").build(),
                        Unit.builder().name("hour").symbol("h").build(),
                        Unit.builder().name("day").symbol("d").build(),
                        Unit.builder().name("week").symbol("w").build()
                )).build();

        length = Measure.builder()
                .namespace("base")
                .name("Length")
                .units(Arrays.asList(
                        Unit.builder().name("millimetre").symbol("mm").build(),
                        Unit.builder().name("centimetre").symbol("cm").build(),
                        Unit.builder().name("decimetre").symbol("dm").build(),
                        Unit.builder().name("metre").symbol("m").build()
                )).build();

        mass = Measure.builder()
                .namespace("base")
                .namespace("Mass")
                .units(Arrays.asList(
                        Unit.builder().name("milligramm").symbol("mg").build(),
                        Unit.builder().name("gramm").symbol("g").build(),
                        Unit.builder().name("dekagramm").symbol("dkg").build(),
                        Unit.builder().name("kilogramm").symbol("kg").build()
                )).build();

        velocity = Measure.builder()
                .namespace("derived")
                .name("Velocity")
                .units(Arrays.asList(
                        Unit.builder().name("kilometrePerHour").symbol("km/h").build(),
                        Unit.builder().name("metrePerSecond").symbol("m/s").build()
                )).build();

        measures = Arrays.asList(time, length, mass, velocity);
    }

    @Override
    public String getMeasureNamespace(final Measure measure) {
        return measure.getNamespace();
    }

    @Override
    public String getMeasureName(final Measure measure) {
        return measure.getName();
    }

    @Override
    public Optional<Measure> getMeasure(final String namespace, final String name) {
        return measures.parallelStream()
                .filter(m -> Objects.equals(m.getNamespace(), namespace) && Objects.equals(m.getName(), name))
                .findAny();
    }

    @Override
    public Map<Measure, Integer> getBaseMeasures(final Measure measure) {
        if (velocity.equals(measure)) {
            final Map<Measure, Integer> base = new HashMap<>();
            base.put(length, 1);
            base.put(time, -1);
            return base;
        } else {
            return Collections.singletonMap(measure, 1);
        }
    }

    @Override
    public Collection<Unit> getUnits(final Measure measure) {
        return measure.getUnits();
    }

    @Override
    public boolean isDurationSupportingAddition(final Unit unit) {
        return time.getUnits().contains(unit);
    }

    @Override
    public Optional<Unit> getUnitByNameOrSymbol(final Optional<Measure> measure, final String nameOrSymbol) {
        if (measure.isPresent()) {
            return measure.get().getUnits().parallelStream()
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny();
        } else {
            return measures.parallelStream()
                    .map(m -> m.getUnits().parallelStream()
                            .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                            .findAny())
                    .filter(us -> us.isPresent()).map(us -> us.get())
                    .findAny();
        }
    }

    @Override
    public Stream<Measure> getMeasures() {
        return measures.stream();
    }

    public Measure getLength() {
        return length;
    }

    public Measure getTime() {
        return time;
    }

    public Measure getMass() {
        return mass;
    }

    Measure getVelocity() {
        return velocity;
    }
}
