package hu.blackbelt.judo.meta.expression.adapters.measure.model;

import java.util.Collection;

public class Measure {

    private String namespace;

    private String name;

    private Collection<Unit> units;

    Measure(String namespace, String name, Collection<Unit> units) {
        this.namespace = namespace;
        this.name = name;
        this.units = units;
    }

    public static MeasureBuilder builder() {
        return new MeasureBuilder();
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getName() {
        return this.name;
    }

    public Collection<Unit> getUnits() {
        return this.units;
    }

    public String toString() {
        return "Measure(namespace=" + this.getNamespace() + ", name=" + this.getName() + ")";
    }

    public static class MeasureBuilder {
        private String namespace;
        private String name;
        private Collection<Unit> units;

        MeasureBuilder() {
        }

        public Measure.MeasureBuilder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Measure.MeasureBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Measure.MeasureBuilder units(Collection<Unit> units) {
            this.units = units;
            return this;
        }

        public Measure build() {
            return new Measure(namespace, name, units);
        }

        public String toString() {
            return "Measure.MeasureBuilder(namespace=" + this.namespace + ", name=" + this.name + ", units=" + this.units + ")";
        }
    }
}
