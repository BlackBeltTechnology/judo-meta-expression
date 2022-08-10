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
