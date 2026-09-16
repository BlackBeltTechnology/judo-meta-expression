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

import java.util.*;

/**
 * Mock enumeration type with members.
 */
public class MockEnumeration extends MockPrimitive {

    private final Set<String> members;

    public MockEnumeration(String name) {
        this(name, (String) null);
    }

    public MockEnumeration(String name, String namespace) {
        super(name, namespace, PrimitiveType.ENUMERATION);
        this.members = new LinkedHashSet<>();
    }

    public MockEnumeration(String name, String namespace, Set<String> members) {
        super(name, namespace, PrimitiveType.ENUMERATION);
        this.members = new LinkedHashSet<>(members);
    }

    public Set<String> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public void addMember(String member) {
        members.add(member);
    }

    public boolean containsMember(String member) {
        return members.contains(member);
    }

    @Override
    public String toString() {
        return "MockEnumeration{" + getFqName() + ", members=" + members + "}";
    }
}
