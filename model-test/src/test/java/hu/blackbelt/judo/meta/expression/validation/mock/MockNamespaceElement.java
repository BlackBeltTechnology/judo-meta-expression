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

/**
 * Base interface for all mock namespace elements.
 * Represents types that can be referenced by name in the expression model.
 */
public interface MockNamespaceElement {

    /**
     * Get the name of this element.
     * @return element name
     */
    String getName();

    /**
     * Get the namespace of this element.
     * @return namespace (may be null for default namespace)
     */
    String getNamespace();

    /**
     * Get the fully qualified name (namespace::name).
     * @return fully qualified name
     */
    default String getFqName() {
        if (getNamespace() == null || getNamespace().isEmpty()) {
            return getName();
        }
        return getNamespace() + "::" + getName();
    }
}
