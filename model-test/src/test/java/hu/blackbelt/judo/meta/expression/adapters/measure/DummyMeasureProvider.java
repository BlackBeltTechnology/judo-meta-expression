package hu.blackbelt.judo.meta.expression.adapters.measure;

import hu.blackbelt.judo.meta.expression.adapters.measure.model.Measure;
import hu.blackbelt.judo.meta.expression.adapters.measure.model.Unit;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

import java.util.*;
import java.util.stream.Stream;

public class DummyMeasureProvider implements MeasureProvider<Measure, Unit> {

    private final Collection<Measure> measures;

    private final Measure time;
    private final Measure length;
    private final Measure mass;
    private final Measure area;
    private final Measure volume;
    private final Measure velocity;

    public DummyMeasureProvider() {
        time = Measure.builder()
                .namespace("system")
                .name("Time")
                .units(Arrays.asList(
                        Unit.builder().name("millisecond").symbol("ms").build(),
                        Unit.builder().name("second").symbol("sec").build(),
                        Unit.builder().name("minute").symbol("min").build(),
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

        area = Measure.builder()
                .namespace("derived")
                .name("Area")
                .units(Arrays.asList(
                        Unit.builder().name("squaredMillimetre").symbol("mm²").build(),
                        Unit.builder().name("squaredCentimetre").symbol("cm²").build(),
                        Unit.builder().name("squaredDecimetre").symbol("dm²").build(),
                        Unit.builder().name("squaredMetre").symbol("m²").build()
                )).build();

        volume = Measure.builder()
                .namespace("derived")
                .name("Volume")
                .units(Arrays.asList(
                        Unit.builder().name("cubicMillimetre").symbol("mm³").build(),
                        Unit.builder().name("cubicCentimetre").symbol("cm³").build(),
                        Unit.builder().name("cubicDecimetre").symbol("dm³").build(),
                        Unit.builder().name("cubicMetre").symbol("m³").build(),
                        Unit.builder().name("millilitre").symbol("ml").build(),
                        Unit.builder().name("centilitre").symbol("cl").build(),
                        Unit.builder().name("decilitre").symbol("dl").build(),
                        Unit.builder().name("litre").symbol("l").build()
                )).build();

        mass = Measure.builder()
                .namespace("base")
                .name("Mass")
                .units(Arrays.asList(
                        Unit.builder().name("milligramm").symbol("mg").build(),
                        Unit.builder().name("gramm").symbol("g").build(),
                        Unit.builder().name("dekagramm").symbol("dkg").build(),
                        Unit.builder().name("kilogramm").symbol("kg").build(),
                        Unit.builder().name("quintal").symbol("q").build(),
                        Unit.builder().name("tonne").symbol("t").build()
                )).build();

        velocity = Measure.builder()
                .namespace("derived")
                .name("Velocity")
                .units(Arrays.asList(
                        Unit.builder().name("kilometrePerHour").symbol("km/h").build(),
                        Unit.builder().name("metrePerSecond").symbol("m/s").build()
                )).build();

        measures = Arrays.asList(time, length, area, volume, mass, velocity);
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
        return measures.stream()
                .filter(m -> Objects.equals(m.getNamespace(), namespace) && Objects.equals(m.getName(), name))
                .findAny();
    }

    @Override
    public EMap<Measure, Integer> getBaseMeasures(final Measure measure) {
        if (velocity.equals(measure)) {
            final Map<Measure, Integer> base = new HashMap<>();
            base.put(length, 1);
            base.put(time, -1);
            return ECollections.asEMap(base);
        } else if (area.equals(measure)) {
            return ECollections.singletonEMap(length, 2);
        } else if (volume.equals(measure)) {
            return ECollections.singletonEMap(length, 3);
        } else {
            return ECollections.singletonEMap(measure, 1);
        }
    }

    @Override
    public EList<Unit> getUnits(final Measure measure) {
        return new BasicEList<>(measure.getUnits());
    }

    @Override
    public boolean isDurationSupportingAddition(final Unit unit) {
        return time.getUnits().contains(unit);
    }

    @Override
    public Optional<Unit> getUnitByNameOrSymbol(final Optional<Measure> measure, final String nameOrSymbol) {
        if (measure.isPresent()) {
            return measure.get().getUnits().stream()
                    .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                    .findAny();
        } else {
            final Optional<Unit> unit = measures.stream()
                    .map(m -> m.getUnits().stream()
                            .filter(u -> Objects.equals(u.getName(), nameOrSymbol) || Objects.equals(u.getSymbol(), nameOrSymbol))
                            .findAny())
                    .filter(us -> us.isPresent()).map(us -> us.get())
                    .findAny();
            return unit;
        }
    }

    @Override
    public Stream<Measure> getMeasures() {
        return measures.stream();
    }

    @Override
    public Stream<Unit> getUnits() {
        return measures.stream().flatMap(m -> m.getUnits().stream());
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

    public Measure getArea() {
        return area;
    }

    public Measure getVolume() {
        return volume;
    }

    Measure getVelocity() {
        return velocity;
    }

    @Override
    public boolean isBaseMeasure(Measure measure) {
        return Arrays.asList(time, length, mass).contains(measure);
    }

    @Override
    public void setMeasureChangeHandler(final MeasureChangedHandler measureChangeHandler) {
        // not implemented yet
    }
}
