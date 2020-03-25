package hu.blackbelt.judo.meta.expression.builder.jql;

import hu.blackbelt.judo.meta.expression.MeasureName;
import hu.blackbelt.judo.meta.jql.jqldsl.QualifiedName;

import java.util.Collection;
import java.util.Optional;

public interface ExpressionMeasureProvider {
    MeasureName getMeasureName(String qualifiedName);

    MeasureName getDurationMeasureName(QualifiedName qualifiedName);

    Collection<MeasureName> getDurationMeasures();

    Optional<MeasureName> getDefaultDurationMeasure();
}
