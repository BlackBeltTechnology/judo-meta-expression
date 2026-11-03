package hu.blackbelt.judo.meta.expression.validation.mock;

/*-
 * #%L
 * Judo :: Expression :: Model :: Test
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

import java.util.Objects;

/**
 * Mock reference representing a relationship to another entity type.
 */
public class MockReference {

    private final String name;
    private final MockEntityType target;
    private final boolean collection;
    private boolean derived;
    private String getter;
    private String setter;
    private String range;
    private String defaultValue;

    public MockReference(String name, MockEntityType target, boolean collection) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.target = Objects.requireNonNull(target, "target must not be null");
        this.collection = collection;
    }

    public MockReference(String name, MockEntityType target, boolean collection,
                         boolean derived, String getter, String setter, 
                         String defaultValue, String range) {
        this(name, target, collection);
        this.derived = derived;
        this.getter = getter;
        this.setter = setter;
        this.defaultValue = defaultValue;
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public MockEntityType getTarget() {
        return target;
    }

    public boolean isCollection() {
        return collection;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    public String getGetter() {
        return getter;
    }

    public void setGetter(String getter) {
        this.getter = getter;
    }

    public String getSetter() {
        return setter;
    }

    public void setSetter(String setter) {
        this.setter = setter;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "MockReference{name='" + name + "', target=" + target.getName() + 
               ", collection=" + collection + ", derived=" + derived + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MockReference that = (MockReference) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
