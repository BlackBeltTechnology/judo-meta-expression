package hu.blackbelt.judo.meta.expression.adapters.measure;

import hu.blackbelt.judo.meta.expression.adapters.measure.model.Unit;

import java.util.Optional;

public class DummyMeasureSupport implements MeasureSupport<Class, Unit> {

    @Override
    public Optional<Unit> getUnit(final Class clazz, final String attributeName) {
        return null;
    }
}
