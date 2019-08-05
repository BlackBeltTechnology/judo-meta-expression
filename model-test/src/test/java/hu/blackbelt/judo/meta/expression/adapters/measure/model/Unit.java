package hu.blackbelt.judo.meta.expression.adapters.measure.model;

public class Unit {

    private String name;

    private String symbol;

    Unit(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public static UnitBuilder builder() {
        return new UnitBuilder();
    }

    public String getName() {
        return this.name;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String toString() {
        return "Unit(name=" + this.getName() + ", symbol=" + this.getSymbol() + ")";
    }

    public static class UnitBuilder {
        private String name;
        private String symbol;

        UnitBuilder() {
        }

        public Unit.UnitBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Unit.UnitBuilder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Unit build() {
            return new Unit(name, symbol);
        }

        public String toString() {
            return "Unit.UnitBuilder(name=" + this.name + ", symbol=" + this.symbol + ")";
        }
    }
}
