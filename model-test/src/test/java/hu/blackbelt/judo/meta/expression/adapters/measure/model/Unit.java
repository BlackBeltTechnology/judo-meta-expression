package hu.blackbelt.judo.meta.expression.adapters.measure.model;

/*-
 * #%L
 * JUDO :: Expression :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
