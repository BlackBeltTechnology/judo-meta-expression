package hu.blackbelt.judo.meta.expression.builder.jql;

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

import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * Extract all JQL expressions from a given model and create expression resource set.
 */
public interface JqlExtractor {

    String JQL_DIALECT = "JQL";

    /**
     * Extract all JQL expressions that have no binding yet to a given resource set (created by constructor).
     *
     * @return expression resource set
     */
    ResourceSet extractExpressions();
}
