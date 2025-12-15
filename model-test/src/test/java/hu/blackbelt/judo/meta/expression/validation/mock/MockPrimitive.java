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
 * Mock primitive type representing basic data types (Integer, Decimal, Boolean, String, etc.).
 */
public class MockPrimitive implements MockNamespaceElement {

    private final String name;
    private final String namespace;
    private final PrimitiveType primitiveType;
    private MockMeasure measure;
    private MockUnit unit;

    public MockPrimitive(String name, PrimitiveType primitiveType) {
        this(name, null, primitiveType);
    }

    public MockPrimitive(String name, String namespace, PrimitiveType primitiveType) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.namespace = namespace;
        this.primitiveType = Objects.requireNonNull(primitiveType, "primitiveType must not be null");
    }

    public MockPrimitive(String name, String namespace, PrimitiveType primitiveType, 
                         MockMeasure measure, MockUnit unit) {
        this(name, namespace, primitiveType);
        this.measure = measure;
        this.unit = unit;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    public MockMeasure getMeasure() {
        return measure;
    }

    public void setMeasure(MockMeasure measure) {
        this.measure = measure;
    }

    public MockUnit getUnit() {
        return unit;
    }

    public void setUnit(MockUnit unit) {
        this.unit = unit;
    }

    public boolean isMeasured() {
        return measure != null;
    }

    public boolean isNumeric() {
        return primitiveType == PrimitiveType.INTEGER || primitiveType == PrimitiveType.DECIMAL;
    }

    public boolean isInteger() {
        return primitiveType == PrimitiveType.INTEGER;
    }

    public boolean isDecimal() {
        return primitiveType == PrimitiveType.DECIMAL;
    }

    public boolean isBoolean() {
        return primitiveType == PrimitiveType.BOOLEAN;
    }

    public boolean isString() {
        return primitiveType == PrimitiveType.STRING;
    }

    public boolean isDate() {
        return primitiveType == PrimitiveType.DATE;
    }

    public boolean isTimestamp() {
        return primitiveType == PrimitiveType.TIMESTAMP;
    }

    public boolean isTime() {
        return primitiveType == PrimitiveType.TIME;
    }

    public boolean isCustom() {
        return primitiveType == PrimitiveType.CUSTOM;
    }

    public boolean isEnumeration() {
        return primitiveType == PrimitiveType.ENUMERATION;
    }

    @Override
    public String toString() {
        return "MockPrimitive{" + getFqName() + ", type=" + primitiveType + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MockPrimitive that = (MockPrimitive) o;
        return Objects.equals(name, that.name) && 
               Objects.equals(namespace, that.namespace) && 
               primitiveType == that.primitiveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, namespace, primitiveType);
    }
}
